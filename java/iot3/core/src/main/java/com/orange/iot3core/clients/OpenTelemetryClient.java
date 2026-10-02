/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.time.Duration;
import java.util.Base64;

/**
 * Wraps an OpenTelemetry SDK instance (tracer, propagators, OTLP/HTTP exporter) that is entirely
 * self-contained within this object — it deliberately does <b>not</b> register itself with the
 * process-wide {@code GlobalOpenTelemetry} singleton.
 *
 * <p>{@code GlobalOpenTelemetry.set(...)} may only be called once per JVM and throws
 * {@link IllegalStateException} on any subsequent call with a different instance. Relying on it
 * made stopping and restarting the SDK (or running several {@code IoT3Core}/{@code IoT3Mobility}
 * instances in the same JVM) fragile and error-prone. Callers that need trace propagation
 * (see {@link #getPropagators()}) should use the instance returned by this class directly instead
 * of going through {@code GlobalOpenTelemetry}.
 */
public class OpenTelemetryClient {

    private final String serviceName;
    private Tracer tracer;
    private SdkTracerProvider tracerProvider;
    private ContextPropagators propagators;
    private final String scheme;
    private final String host;
    private final int port;
    private final String endpoint;
    private final String username;
    private final String password;

    public OpenTelemetryClient(String scheme,
                               String host,
                               int port,
                               String endpoint,
                               String serviceName,
                               String username,
                               String password) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.endpoint = endpoint;
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
        initialize();
    }

    private void initialize() {
        String url = scheme + "://" + host + ":" + port + endpoint;
        OpenTelemetry openTelemetry = initOpenTelemetry(url, username, password);
        this.tracer = openTelemetry.getTracer(serviceName);
    }

    private OpenTelemetry initOpenTelemetry(String endpoint, String username, String password) {
        // Encoding the username and password in Base64 for the Basic Authentication header
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        if (!endpoint.endsWith("/v1/traces")) {
            if(!endpoint.endsWith("/")) endpoint += "/";
            endpoint += "v1/traces";
        }

        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(endpoint)
                .addHeader("Authorization", "Basic " + credentials)  // Add Basic Auth header
                .build();

        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
                .setMaxExportBatchSize(50)
                .setScheduleDelay(Duration.ofMillis(5000))
                .build();

        Resource resource = Resource.builder()
                .put("service.name", serviceName)
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setResource(resource)
                .build();

        ContextPropagators propagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance());

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(propagators)
                .build();

        // Deliberately NOT calling GlobalOpenTelemetry.set(openTelemetry) here: see class javadoc.
        // This SdkTracerProvider/ContextPropagators pair is kept as instance state instead and
        // exposed via getPropagators(), so callers (e.g. MqttClient) can propagate trace context
        // without depending on process-wide static state.

        this.tracerProvider = tracerProvider;
        this.propagators = propagators;

        return openTelemetry;
    }

    public Span startSpan(String spanName, SpanKind spanKind) {
        return tracer.spanBuilder(spanName).setSpanKind(spanKind).startSpan();
    }

    public Span startSpanWithLink(String spanName, SpanKind spanKind, Span linkedSpan) {
        return startSpanWithLink(spanName, spanKind, getTraceId(linkedSpan), getSpanId(linkedSpan));
    }

    public Span startSpanWithLink(String spanName, SpanKind spanKind, String linkedTraceId, String linkedSpanId) {
        SpanContext spanContext = SpanContext.createFromRemoteParent(
                linkedTraceId, linkedSpanId, TraceFlags.getDefault(), TraceState.getDefault()
        );
        return tracer.spanBuilder(spanName).setSpanKind(spanKind).addLink(spanContext).startSpan();
    }

    public String getSpanId(Span span) {
        return span.getSpanContext().getSpanId();
    }

    public String getTraceId(Span span) {
        return span.getSpanContext().getTraceId();
    }

    /**
     * Returns the {@link ContextPropagators} (W3C trace context) bound to this instance, for
     * injecting/extracting the {@code traceparent} header without depending on
     * {@code GlobalOpenTelemetry}.
     */
    public ContextPropagators getPropagators() {
        return propagators;
    }

    /**
     * Forces an immediate export of any spans that are still buffered in the
     * {@link BatchSpanProcessor}, instead of waiting for the regular scheduled delay.
     *
     * <p>Mainly useful for tests that need to assert on exported spans synchronously.
     */
    public void forceFlush() {
        tracerProvider.forceFlush().join(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void close() {
        tracerProvider.shutdown();
    }

}
