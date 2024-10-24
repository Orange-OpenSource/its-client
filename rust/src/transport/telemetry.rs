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

use log::debug;
use std::str::from_utf8;
use std::time::Duration;

use opentelemetry::global::BoxedSpan;
use opentelemetry::propagation::{Extractor, TextMapPropagator};
use opentelemetry::trace::{Link, Span, SpanKind, TraceContextExt, Tracer};
use opentelemetry::{global, Context, KeyValue};
use opentelemetry_otlp::WithExportConfig;
use opentelemetry_sdk::propagation::TraceContextPropagator;
use opentelemetry_sdk::runtime;
use opentelemetry_sdk::trace::{
    BatchConfigBuilder, BatchSpanProcessor, RandomIdGenerator, Sampler, TracerProvider,
};
use opentelemetry_sdk::Resource;
use reqwest::header;
use rumqttc::v5::mqttbytes::v5::Publish;

use crate::client::configuration::telemetry_configuration::TelemetryConfiguration;

/// Registers a global TracerProvider with HTTP exporter
pub fn init_tracer(
    configuration: &TelemetryConfiguration,
    service_name: &'static str,
) -> Result<(), opentelemetry::trace::TraceError> {
    let path = if configuration.path.starts_with('/') {
        configuration.path.clone().as_str()[1..].to_string()
    } else {
        configuration.path.clone()
    };

    // FIXME manage HTTPS
    let endpoint = format!(
        "http://{}:{}/{}",
        configuration.host, configuration.port, path
    );

    let http_client = match configuration.basic_auth_header() {
        Some(header) => {
            let mut headers = header::HeaderMap::new();
            let mut auth_value =
                header::HeaderValue::try_from(header).expect("Failed to create header value");
            auth_value.set_sensitive(true);
            headers.insert(header::AUTHORIZATION, auth_value);
            reqwest::ClientBuilder::new()
                .default_headers(headers)
                .build()
                .expect("Failed to create telemetry HTTP client")
        }
        None => reqwest::Client::new(),
    };

    let http_exporter = opentelemetry_otlp::new_exporter()
        .http()
        .with_http_client(http_client)
        .with_endpoint(endpoint)
        .with_timeout(Duration::from_secs(3))
        .build_span_exporter()?;

    let batch_processor = BatchSpanProcessor::builder(http_exporter, runtime::Tokio)
        .with_batch_config(
            BatchConfigBuilder::default()
                .with_max_export_batch_size(configuration.batch_size)
                .build(),
        )
        .build();

    let tracer_provider = TracerProvider::builder()
        .with_span_processor(batch_processor)
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

pub fn get_span(
    tracer_name: &'static str,
    span_name: &'static str,
    span_kind: Option<SpanKind>,
) -> BoxedSpan {
    let tracer = global::tracer(tracer_name);
    let mut span_builder = tracer.span_builder(span_name);

    if let Some(kind) = span_kind {
        span_builder = span_builder.with_kind(kind)
    }

    span_builder.start(&tracer)
}

pub fn get_linked_span<E>(
    tracer_name: &'static str,
    span_name: &'static str,
    span_kind: Option<SpanKind>,
    from: &E,
) -> BoxedSpan
where
    E: Extractor,
{
    let tracer = global::tracer(tracer_name);

    let propagator = TraceContextPropagator::new();
    let trace_cx = propagator.extract(from);
    let span_cx = trace_cx.span().span_context().clone();

    let mut span_builder = tracer
        .span_builder(span_name)
        .with_links(vec![Link::with_context(span_cx)]);

    if let Some(kind) = span_kind {
        span_builder = span_builder.with_kind(kind)
    }

    span_builder.start(&tracer)
}

pub fn execute_in_span<F, E, R>(
    tracer_name: &'static str,
    span_name: &'static str,
    span_kind: Option<SpanKind>,
    from: Option<&E>,
    block: F,
) -> R
where
    F: FnOnce() -> R,
    E: Extractor,
{
    let span = if let Some(from) = from {
        get_linked_span(tracer_name, span_name, span_kind, from)
    } else {
        get_span(tracer_name, span_name, span_kind)
    };

    let cx = Context::current_with_span(span);
    let _guard = cx.attach();

    block()
}

pub fn add_link<E>(linked_entity: &E, span: &mut BoxedSpan)
where
    E: Extractor,
{
    let propagator = TraceContextPropagator::new();
    let trace_cx = propagator.extract(linked_entity);
    let span_cx = trace_cx.span().span_context().clone();

    span.add_link(span_cx, Vec::new());
}

pub(crate) fn get_mqtt_span(span_kind: SpanKind, topic: &str, payload_size: i64) -> BoxedSpan {
    debug!("Starting MQTT span...");
    let tracer = global::tracer("iot3.core");

    tracer
        .span_builder("IoT3 Core MQTT Message")
        .with_kind(span_kind)
        .with_attributes(vec![
            KeyValue::new("iot3.core.mqtt.topic", topic.to_string()),
            KeyValue::new("iot3.core.mqtt.payload_size", payload_size),
            KeyValue::new("iot3.core.sdk_language", "rust"),
        ])
        .start(&tracer)
}

pub(crate) fn get_reception_mqtt_span(publish: &Publish) -> BoxedSpan {
    let tracer = global::tracer("iot3.core");

    let topic = from_utf8(&publish.topic.to_vec())
        .unwrap_or_default()
        .to_string();
    let size = publish.payload.len();

    let propagator = TraceContextPropagator::new();
    let trace_cx = propagator.extract(&ExtractWrapper(publish));
    let span_cx = trace_cx.span().span_context().clone();

    tracer
        .span_builder("IoT3 Core MQTT Message")
        .with_kind(SpanKind::Consumer)
        .with_attributes(vec![
            KeyValue::new("iot3.core.mqtt.topic", topic),
            KeyValue::new("iot3.core.mqtt.payload_size", size as i64),
            KeyValue::new("iot3.core.sdk_language", "rust"),
        ])
        .with_links(vec![Link::with_context(span_cx)])
        .start(&tracer)
}

struct ExtractWrapper<'p>(&'p Publish);
impl Extractor for ExtractWrapper<'_> {
    fn get(&self, key: &str) -> Option<&str> {
        if let Some(properties) = &self.0.properties {
            properties
                .user_properties
                .iter()
                .find(|(k, _)| key == k)
                .map(|(_, value)| value.as_str())
        } else {
            None
        }
    }

    fn keys(&self) -> Vec<&str> {
        if let Some(properties) = &self.0.properties {
            properties
                .user_properties
                .iter()
                .map(|(key, _)| key.as_str())
                .collect::<Vec<&str>>()
        } else {
            Vec::new()
        }
    }
}
