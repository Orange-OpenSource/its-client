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

use opentelemetry::global::BoxedSpan;
use opentelemetry::propagation::{Extractor, TextMapPropagator};
use opentelemetry::trace::{Link, TraceContextExt, Tracer};
use opentelemetry::{global, Context, KeyValue};
use opentelemetry_otlp::WithExportConfig;
use opentelemetry_sdk::propagation::TraceContextPropagator;
use opentelemetry_sdk::trace::{RandomIdGenerator, Sampler, TracerProvider};
use opentelemetry_sdk::Resource;
use std::time::Duration;

/// Registers a global TracerProvider with HTTP exporter
pub fn init_tracer(
    service_name: &'static str,
    host: &'static str,
    port: u16,
) -> Result<(), opentelemetry::trace::TraceError> {
    let endpoint = format!("http://{}:{}/v1/traces", host, port);

    let http_exporter = opentelemetry_otlp::new_exporter()
        .http()
        .with_http_client(reqwest::Client::new())
        .with_endpoint(endpoint)
        .with_timeout(Duration::from_secs(3))
        .build_span_exporter()?;

    let tracer_provider = TracerProvider::builder()
        .with_simple_exporter(http_exporter)
        .with_config(
            opentelemetry_sdk::trace::config()
                .with_sampler(Sampler::AlwaysOn)
                .with_id_generator(RandomIdGenerator::default())
                .with_max_events_per_span(64)
                .with_max_attributes_per_span(16)
                .with_max_events_per_span(16)
                .with_resource(Resource::new(vec![KeyValue::new(
                    "service.name",
                    service_name,
                )])),
        )
        .build();

    let _ = global::set_tracer_provider(tracer_provider);

    Ok(())
}

pub fn execute_in_span<F, E, R>(
    tracer_name: &'static str,
    span_name: &'static str,
    from: Option<&E>,
    block: F,
) -> R
where
    F: FnOnce() -> R,
    E: Extractor,
{
    let span = get_span(tracer_name, span_name, from);
    let cx = Context::current_with_span(span);
    let _guard = cx.attach();

    block()
}

pub fn get_span<E>(
    tracer_name: &'static str,
    span_name: &'static str,
    from: Option<&E>,
) -> BoxedSpan
where
    E: Extractor,
{
    let tracer = global::tracer(tracer_name);

    let span_builder = if let Some(packet) = from {
        let propagator = TraceContextPropagator::new();
        let trace_cx = propagator.extract(packet);
        let span_cx = trace_cx.span().span_context().clone();

        tracer
            .span_builder(span_name)
            .with_links(vec![Link::with_context(span_cx)])
    } else {
        tracer.span_builder(span_name)
    };

    span_builder.start(&tracer)
}
