/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

public class OpenTelemetryClient {

    private static final String INSTRUMENTATION_NAME = "com.orange.iot3core";
    private Tracer tracer;
    private SdkTracerProvider tracerProvider;
    private final Scheme scheme;
    private final String host;

    public OpenTelemetryClient(Scheme scheme, String host) {
        this.scheme = scheme;
        this.host = host;
        initialize();
    }

    private void initialize() {
        String endpoint = scheme.getScheme() + "://" + host + ":" + scheme.getDefaultPort(); // to adapt for HTTPS
        OpenTelemetry openTelemetry = initOpenTelemetry(endpoint);
        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
        this.tracerProvider = (SdkTracerProvider) openTelemetry.getTracerProvider();
    }

    private OpenTelemetry initOpenTelemetry(String endpoint) {
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        GlobalOpenTelemetry.set(openTelemetry);
        return openTelemetry;
    }

    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }

    public Span startSpanWithLink(String spanName, Span linkedSpan) {
        return startSpanWithLink(spanName, getTraceId(linkedSpan), getSpanId(linkedSpan));
    }

    public Span startSpanWithLink(String spanName, String linkedTraceId, String linkedSpanId) {
        SpanContext spanContext = SpanContext.createFromRemoteParent(
                linkedTraceId, linkedSpanId, TraceFlags.getDefault(), TraceState.getDefault()
        );
        return tracer.spanBuilder(spanName).addLink(spanContext).startSpan();
    }

    public void endSpan(Span span, boolean success, String event) {
        try (Scope scope = span.makeCurrent()) {
            span.addEvent(event);
            if (!success) {
                span.addEvent("Operation failed");
            }
        } catch (Exception e) {
            span.recordException(e);
        } finally {
            span.end();
        }
    }

    public void addEvent(Span span, String event) {
        span.addEvent(event);
    }

    public String getSpanId(Span span) {
        return span.getSpanContext().getSpanId();
    }

    public String getTraceId(Span span) {
        return span.getSpanContext().getTraceId();
    }

    public void disconnect() {
        tracerProvider.shutdown();
    }

    public void connect() {
        initialize();
    }

    public enum Scheme {
        HTTP("http", "4318"),
        HTTPS("https", "4318"),
        GRPC("grpc", "4317");

        private final String scheme;
        private final String defaultPort;

        Scheme(String scheme, String defaultPort) {
            this.scheme = scheme;
            this.defaultPort = defaultPort;
        }

        public String getScheme() {
            return scheme;
        }

        public String getDefaultPort() {
            return defaultPort;
        }
    }

}
