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
use opentelemetry::{Context, KeyValue, global};
use opentelemetry_otlp::ExporterBuildError;
use opentelemetry_otlp::{WithExportConfig, WithHttpConfig};
use opentelemetry_sdk::Resource;
use opentelemetry_sdk::propagation::TraceContextPropagator;
use opentelemetry_sdk::trace::{RandomIdGenerator, Sampler, SdkTracerProvider};
use reqwest::header;
use rumqttc::v5::mqttbytes::v5::Publish;

use crate::client::configuration::telemetry_configuration::TelemetryConfiguration;

/// Runs a closure outside any active Tokio runtime context.
///
/// `reqwest::blocking::Client` internally manages its own Tokio runtime, which
/// panics if created or dropped inside an existing runtime.  When a Tokio
/// runtime is active we use [`tokio::task::block_in_place`] to temporarily move
/// the current worker thread into a blocking context; otherwise we just call the
/// closure directly.
fn run_outside_runtime<F, R>(f: F) -> R
where
    F: FnOnce() -> R,
{
    if tokio::runtime::Handle::try_current().is_ok() {
        tokio::task::block_in_place(f)
    } else {
        f()
    }
}

/// Registers a global TracerProvider with HTTP exporter
pub fn init_tracer(
    configuration: &TelemetryConfiguration,
    service_name: &'static str,
) -> Result<(), ExporterBuildError> {
    let tracer_provider = run_outside_runtime(|| {
        let path = if configuration.path.starts_with('/') {
            configuration.path.as_str()[1..].to_string()
        } else {
            configuration.path.clone()
        };

        // FIXME manage HTTPS
        let endpoint = format!(
            "http{}://{}:{}/{}",
            if configuration.use_tls { "s" } else { "" },
            configuration.host,
            configuration.port,
            path
        );

        let http_client = match configuration.basic_auth_header() {
            Some(header) => {
                let mut headers = header::HeaderMap::new();
                let mut auth_value =
                    header::HeaderValue::try_from(header).expect("Failed to create header value");
                auth_value.set_sensitive(true);
                headers.insert(header::AUTHORIZATION, auth_value);
                reqwest::blocking::ClientBuilder::new()
                    .default_headers(headers)
                    .build()
                    .expect("Failed to create telemetry HTTP client")
            }
            None => reqwest::blocking::Client::new(),
        };

        let http_exporter = opentelemetry_otlp::SpanExporter::builder()
            .with_http()
            .with_http_client(http_client)
            .with_endpoint(endpoint)
            .with_timeout(Duration::from_secs(3))
            .build()?;

        // Override service.name.
        let resource = Resource::builder().with_service_name(service_name).build();

        Ok::<SdkTracerProvider, ExporterBuildError>(
            SdkTracerProvider::builder()
                .with_batch_exporter(http_exporter)
                .with_sampler(Sampler::AlwaysOn)
                .with_id_generator(RandomIdGenerator::default())
                .with_max_events_per_span(16)
                .with_max_attributes_per_span(16)
                .with_resource(resource)
                .build(),
        )
    })?;

    // Store the provider so examples/binaries can later call `shutdown()`.
    let _ = TRACER_PROVIDER.set(tracer_provider.clone());
    let _ = global::set_tracer_provider(tracer_provider);

    Ok(())
}

static TRACER_PROVIDER: std::sync::OnceLock<SdkTracerProvider> = std::sync::OnceLock::new();

/// Shuts down the tracer provider created by `init_tracer` (if any), forcing a final export.
pub fn shutdown_tracer() {
    if let Some(provider) = TRACER_PROVIDER.get() {
        let provider = provider.clone();
        run_outside_runtime(move || {
            let _ = provider.shutdown();
        });
    }
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

    let topic = from_utf8(&publish.topic).unwrap_or_default().to_string();
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
        match &self.0.properties {
            Some(properties) => properties
                .user_properties
                .iter()
                .find(|(k, _)| key == k)
                .map(|(_, value)| value.as_str()),
            _ => None,
        }
    }

    fn keys(&self) -> Vec<&str> {
        match &self.0.properties {
            Some(properties) => properties
                .user_properties
                .iter()
                .map(|(key, _)| key.as_str())
                .collect::<Vec<&str>>(),
            _ => Vec::new(),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Read;
    use std::net::TcpListener;
    use std::sync::Arc;
    use std::sync::atomic::{AtomicUsize, Ordering};

    /// Verifies that `run_outside_runtime` prevents the panic that occurs when
    /// `reqwest::blocking::Client` is created or dropped inside a Tokio runtime.
    ///
    /// Without `block_in_place`, this would panic with:
    ///   "Cannot drop a runtime in a context where blocking is not allowed"
    #[tokio::test(flavor = "multi_thread")]
    async fn run_outside_runtime_allows_blocking_client_in_tokio() {
        let client = run_outside_runtime(reqwest::blocking::Client::new);
        assert!(client.get("http://localhost").build().is_ok());
    }

    /// Verifies `run_outside_runtime` works without a Tokio runtime (plain thread).
    #[test]
    fn run_outside_runtime_works_without_tokio() {
        let result = std::thread::spawn(|| run_outside_runtime(|| 42))
            .join()
            .expect("thread panicked");
        assert_eq!(result, 42);
    }

    /// Full integration test reproducing the original crash scenario:
    ///
    /// Inside a `#[tokio::main]`-like runtime (multi-thread flavour), we:
    ///   1. `init_tracer` — builds a `reqwest::blocking::Client` and a
    ///      `BatchSpanProcessor` (which spawns its own OS export thread).
    ///   2. Create several spans via `execute_in_span` / `get_span`.
    ///   3. `shutdown_tracer` — flushes the batch processor, triggering
    ///      an HTTP POST to our mock collector.
    ///
    /// Before the fix the `BatchSpanProcessor` thread panicked with
    ///   *"there is no reactor running, must be called from the context
    ///   of a Tokio 1.x runtime"*
    /// and shutdown failed with
    ///   *"channel is empty and sending half is closed"*.
    ///
    /// A local TCP listener acts as a mock OTLP collector so the test
    /// does not require Docker / Jaeger.  We verify that actual OTLP
    /// bytes are received (non-zero payload).
    ///
    /// Uses `TRACER_PROVIDER` (a process-wide `OnceLock`), so this test
    /// must run alone in a single process.
    /// The `#[tokio::test]` macro creates a new runtime for each test,
    /// so we can run this test in isolation.
    #[tokio::test(flavor = "multi_thread")]
    async fn full_tracer_lifecycle_does_not_panic_in_tokio_runtime() {
        // --- mock OTLP collector -------------------------------------------
        let listener = TcpListener::bind("127.0.0.1:0").expect("bind failed");
        let port = listener.local_addr().unwrap().port();
        let bytes_received = Arc::new(AtomicUsize::new(0));

        let collector_bytes = bytes_received.clone();
        let collector = std::thread::Builder::new()
            .name("mock-otlp-collector".into())
            .spawn(move || {
                listener
                    .set_nonblocking(false)
                    .expect("set_nonblocking failed");
                // Accept connections until the listener is dropped / times out.
                while let Ok((mut stream, _)) = listener.accept() {
                    stream.set_read_timeout(Some(Duration::from_secs(2))).ok();
                    let mut buf = [0u8; 8192];
                    // Read whatever the client sends (HTTP request).
                    while let Ok(n) = stream.read(&mut buf) {
                        if n == 0 {
                            break;
                        }
                        collector_bytes.fetch_add(n, Ordering::Relaxed);
                    }
                    // Respond with a minimal HTTP 200 so the exporter is happy.
                    let response =
                        "HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";
                    let _ = std::io::Write::write_all(&mut stream, response.as_bytes());
                }
            })
            .expect("failed to spawn mock collector thread");

        // --- init ----------------------------------------------------------
        let config = TelemetryConfiguration {
            host: "127.0.0.1".into(),
            port,
            use_tls: false,
            path: "v1/traces".into(),
            batch_size: 2048,
            username: None,
            password: None,
        };

        init_tracer(&config, "telemetry-test").expect("init_tracer must not fail");

        // --- create spans (mirrors the telemetry example) ------------------
        execute_in_span(
            "test/tracer",
            "test/root_span",
            Some(SpanKind::Internal),
            None::<&DummyExtractor>,
            || {
                execute_in_span(
                    "test/tracer",
                    "test/child_span",
                    Some(SpanKind::Internal),
                    None::<&DummyExtractor>,
                    || {},
                );
            },
        );

        let _span = get_span("test/tracer", "test/standalone_span", None);

        // --- shutdown (this was the crash point) ---------------------------
        shutdown_tracer();

        // Give the mock collector a moment to receive the data.
        std::thread::sleep(Duration::from_millis(200));

        // --- assertions ----------------------------------------------------
        let received = bytes_received.load(Ordering::Relaxed);
        assert!(
            received > 0,
            "mock collector should have received OTLP data, got 0 bytes"
        );

        // Ensure the collector thread can terminate.
        drop(collector);
    }

    struct DummyExtractor;
    impl Extractor for DummyExtractor {
        fn get(&self, _key: &str) -> Option<&str> {
            None
        }
        fn keys(&self) -> Vec<&str> {
            Vec::new()
        }
    }
}
