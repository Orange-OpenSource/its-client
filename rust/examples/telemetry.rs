/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use std::collections::HashMap;
use std::fmt::Debug;
use std::path::Path;
use std::{fs, thread};

use clap::{Arg, Command};
use flexi_logger::{with_thread, Cleanup, Criterion, FileSpec, Logger, Naming, WriteMode};
use ini::Ini;
use log::{info, warn};
use opentelemetry::propagation::{Extractor, Injector, TextMapPropagator};
use opentelemetry::trace::{mark_span_as_active, TraceContextExt};
use opentelemetry::Context;
use opentelemetry_sdk::propagation::TraceContextPropagator;

use libits::client::configuration::Configuration;
use libits::transport::telemetry::{execute_in_span, get_span, init_tracer};

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

    let log_path = &configuration
        .get::<String>(Some("log"), "path")
        .unwrap_or("log".to_string());
    let log_path = Path::new(log_path);
    if !log_path.is_dir() {
        if let Err(error) = fs::create_dir(log_path) {
            panic!("Unable to create the log directory: {}", error);
        }
    }
    let _logger = match Logger::try_with_env_or_str("info") {
        Ok(logger) => {
            match logger
                .log_to_file(FileSpec::default().directory(log_path).suppress_timestamp())
                .log_to_stdout()
                .write_mode(WriteMode::Async)
                .format_for_files(with_thread)
                .append()
                .rotate(
                    Criterion::Size(2_000_000),
                    Naming::Timestamps,
                    Cleanup::KeepLogAndCompressedFiles(5, 30),
                )
                .print_message()
                .start()
            {
                Ok(logger_handle) => {
                    info!("logger ready on {}", log_path.to_str().unwrap());
                    logger_handle
                }
                Err(error) => panic!("Logger starting failed with {:?}", error),
            }
        }
        Err(error) => panic!("Logger initialization failed with {:?}", error),
    };

    init_tracer(&configuration.telemetry, "iot3").expect("Failed to configure telemetry");

    info!("Send a trace with a single span 'ping' root span");
    let ping_data = execute_in_span(TRACER_NAME, "example/ping", None::<&Data>, || {
        let context = Context::current();
        trace_span_context_info!("└─ Ping", context);

        let mut data = Data::default();

        let propagator = TraceContextPropagator::new();
        propagator.inject(&mut data);

        data
    });

    info!("Send a trace with a single span 'pong' root span linked with the previous one 'ping'");
    execute_in_span(TRACER_NAME, "example/pong", Some(&ping_data), || {
        let context = Context::current();
        trace_span_context_info!("└─ Pong", context);
    });

    info!("Send a single trace with two spans");
    execute_in_span(TRACER_NAME, "example/nested_root", None::<&Data>, || {
        let context = Context::current();
        trace_span_context_info!("└─ Root", context);

        execute_in_span(TRACER_NAME, "example/nested_child", None::<&Data>, || {
            let context = Context::current();
            trace_span_context_info!("   └─ Child", context);
        })
    });

    info!("Send a trace with 3 spans from 3 threads");
    let root_span = get_span(TRACER_NAME, "main_thread", None::<&Data>);
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

                execute_in_span(TRACER_NAME, "listener_thread", None::<&Data>, || {
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

                execute_in_span(TRACER_NAME, "sender_thread", None::<&Data>, || {
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
        warn!("Sender thread failed to join: {:?}", e)
    }
    if let Err(e) = recv_handle.join() {
        warn!("Listener thread failed to join: {:?}", e)
    }
}
