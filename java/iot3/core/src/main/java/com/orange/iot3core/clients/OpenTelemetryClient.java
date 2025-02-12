/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import io.opentelemetry.api.GlobalOpenTelemetry;
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

public class OpenTelemetryClient {

    private final String serviceName;
    private Tracer tracer;
    private SdkTracerProvider tracerProvider;
    private final Scheme scheme;
    private final String host;
    private final String endpoint;
    private final String username;
    private final String password;

    public OpenTelemetryClient(Scheme scheme,
                               String host,
                               String endpoint,
                               String serviceName,
                               String username,
                               String password) {
        this.scheme = scheme;
        this.host = host;
        this.endpoint = endpoint;
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
        initialize();
    }

    private void initialize() {
        String url = scheme.getScheme() + "://" + host + ":" + scheme.getPort() + endpoint;
        OpenTelemetry openTelemetry = initOpenTelemetry(url, username, password);
        this.tracer = openTelemetry.getTracer(serviceName);
    }

    private OpenTelemetry initOpenTelemetry(String endpoint, String username, String password) {
        // Encoding the username and password in Base64 for the Basic Authentication header
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        if (!endpoint.endsWith("/v1/traces")) endpoint += "/v1/traces";

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

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        GlobalOpenTelemetry.set(openTelemetry);

        this.tracerProvider = tracerProvider;

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

    public void disconnect() {
        tracerProvider.shutdown();
    }

    public void connect() {
        initialize();
    }

    public enum Scheme {
        HTTP("http", 4318),
        HTTPS("https", 4318),
        GRPC("grpc", 4317);

        private final String scheme;
        private final int defaultPort;
        private int customPort;

        Scheme(String scheme, int defaultPort) {
            this.scheme = scheme;
            this.defaultPort = defaultPort;
            this.customPort = -1;
        }

        public String getScheme() {
            return scheme;
        }

        public int getPort() {
            if(customPort < 0) return defaultPort;
            else return customPort;
        }

        public void setCustomPort(int customPort) {
            this.customPort = customPort;
        }
    }

    public static Scheme getScheme(String scheme) {
        return switch (scheme) {
            case "https" -> Scheme.HTTPS;
            case "grpc" -> Scheme.GRPC;
            default -> Scheme.HTTP;
        };
    }

}
