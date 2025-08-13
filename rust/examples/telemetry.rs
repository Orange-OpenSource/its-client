/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 */

use std::collections::HashMap;
use std::fmt::Debug;
use std::path::Path;
use std::thread;

use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::Configuration;
use libits::client::logger::create_stdout_logger;
use libits::transport::telemetry::{execute_in_span, get_span, init_tracer};
use log::{info, warn};
use opentelemetry::propagation::{Extractor, Injector, TextMapPropagator};
use opentelemetry::trace::{SpanKind, TraceContextExt, mark_span_as_active};
use opentelemetry::{Context, global};
use opentelemetry_sdk::propagation::TraceContextPropagator;

const TRACER_NAME: &str = "telemetry/example";

macro_rules! trace_span_context_info {
    ($name: expr, $cxt: expr) => {
        let span = $cxt.span();
        let span_context = span.span_context();
        info!(
            "{: <24} trace_id: {}, span_id: {}",
            $name,
            span_context.trace_id(),
            span_context.span_id()
        );
    };
}

/// Data container that will be used to carry [W3C context][1] to link spans across traces
///
/// - See [Injector]
/// - See [Extractor]
/// - See [TraceContextPropagator]
///
/// [1]: https://www.w3.org/TR/trace-context/
#[derive(Debug, Default)]
struct Data {
    dict: HashMap<String, String>,
}
impl Injector for Data {
    fn set(&mut self, key: &str, value: String) {
        self.dict.set(key, value)
    }
}
impl Extractor for Data {
    fn get(&self, key: &str) -> Option<&str> {
        self.dict.get(key).map(|string| string.as_str())
    }

    fn keys(&self) -> Vec<&str> {
        self.dict.keys().map(|string| string.as_str()).collect()
    }
}

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("Telemetry example")
        .version("0.1.0")
        .author("Nicolas Buffon <nicolas.buffon@orange.com>")
        .about("Sends spans to OTLP collector")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .default_value("examples/config.ini")
                .value_name("CONFIG_FILE_PATH")
                .help("Path to the configuration file"),
        )
        .get_matches();

    let configuration = Configuration::try_from(
        Ini::load_from_file(Path::new(
            matches.get_one::<String>("config-file-path").unwrap(),
        ))
        .expect("Failed to load config file as Ini"),
    )
    .expect("Failed to create Configuration from loaded Ini");

    let _logger = create_stdout_logger().expect("Logger initialization failed");

    #[cfg(feature = "mobility")]
    init_tracer(
        &configuration.telemetry,
        Box::<str>::leak(configuration.mobility.source_uuid.into_boxed_str()),
    )
    .expect("Failed to configure telemetry");
    #[cfg(not(feature = "mobility"))]
    init_tracer(&configuration.telemetry, "iot3").expect("Failed to configure telemetry");

    info!("Send a trace with a single span 'ping' root span");
    let ping_data = execute_in_span(
        TRACER_NAME,
        "example/ping",
        Some(SpanKind::Producer),
        None::<&Data>,
        || {
            let context = Context::current();
            trace_span_context_info!("└─ Ping", context);

            let mut data = Data::default();

            let propagator = TraceContextPropagator::new();
            propagator.inject(&mut data);

            data
        },
    );

    info!("Send a trace with a single span 'pong' root span linked with the previous one 'ping'");
    execute_in_span(
        TRACER_NAME,
        "example/pong",
        Some(SpanKind::Consumer),
        Some(&ping_data),
        || {
            let context = Context::current();
            trace_span_context_info!("└─ Pong", context);
        },
    );

    info!("Send a single trace with two spans");
    execute_in_span(
        TRACER_NAME,
        "example/nested_root",
        None,
        None::<&Data>,
        || {
            let context = Context::current();
            trace_span_context_info!("└─ Root", context);

            execute_in_span(
                TRACER_NAME,
                "example/nested_child",
                None,
                None::<&Data>,
                || {
                    let context = Context::current();
                    trace_span_context_info!("   └─ Child", context);
                },
            )
        },
    );

    info!("Send a trace with 3 spans from 3 threads");
    let root_span = get_span(TRACER_NAME, "main_thread", None);
    let guard = mark_span_as_active(root_span);
    let cxt = Context::current();
    trace_span_context_info!("└─ Main thread", &cxt);

    let (listener_tx, recv_handle) = {
        let (tx, rx) = crossbeam_channel::unbounded();

        let recv_handle = thread::Builder::new()
            .name("listener".to_string())
            .spawn(move || {
                let cxt: Context = rx.recv().unwrap();
                let _guard = cxt.attach();

                execute_in_span(TRACER_NAME, "listener_thread", None, None::<&Data>, || {
                    let cxt = Context::current();
                    trace_span_context_info!("   └─ Listener thread", cxt);
                });
            })
            .unwrap();

        (tx, recv_handle)
    };

    let (send_rx, send_handle) = {
        let (tx, rx) = crossbeam_channel::unbounded();
        let send_handle = thread::Builder::new()
            .name("listener".to_string())
            .spawn(move || {
                let cxt: Context = rx.recv().unwrap();
                let _guard = cxt.clone().attach();

                execute_in_span(TRACER_NAME, "sender_thread", None, None::<&Data>, || {
                    let inner_cxt = Context::current();
                    trace_span_context_info!("   ├─ Sender thread", inner_cxt);
                    listener_tx
                        .send(cxt)
                        .expect("Failed to send context through channel");
                });
            })
            .unwrap();

        (tx, send_handle)
    };

    send_rx
        .send(cxt)
        .expect("Failed to send context through channel");
    drop(guard);

    if let Err(e) = send_handle.join() {
        warn!("Sender thread failed to join: {e:?}")
    }
    if let Err(e) = recv_handle.join() {
        warn!("Listener thread failed to join: {e:?}")
    }

    // Trace export is batched, shutting down the tracer provider will force the export
    global::shutdown_tracer_provider();
}
