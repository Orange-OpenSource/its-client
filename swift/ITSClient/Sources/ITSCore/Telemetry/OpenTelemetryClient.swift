/*
 * Software Name : ITSClient
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Software description: Swift ITS client.
 */

import Foundation
@preconcurrency import OpenTelemetryApi
@preconcurrency import OpenTelemetryProtocolExporterCommon
@preconcurrency import OpenTelemetryProtocolExporterHttp
@preconcurrency import OpenTelemetrySdk

actor OpenTelemetryClient: TelemetryClient {
    private let configuration: TelemetryClientConfiguration
    private var tracer: Tracer?
    private var tracerProvider: TracerProviderSdk?
    private var spans = [SpanID: Span]()

    init(configuration: TelemetryClientConfiguration) {
        self.configuration = configuration
    }

    func start() {
        guard tracer == nil else { return }

        var credentials: String?
        if let user = configuration.user, let password = configuration.password {
            credentials = "\(user):\(password)".data(using: .utf8)?.base64EncodedString()
        }
        let headers = credentials.map { [("Authorization", "Basic \($0)")] }
        let otlpConfiguration = OtlpConfiguration(headers: headers)
        let url = configuration.url.appendingPathComponent("v1/traces")
        let httpTraceExporter = OtlpHttpTraceExporter(endpoint: url, config: otlpConfiguration)
        let batchSpanProcessor = BatchSpanProcessor(spanExporter: httpTraceExporter,
                                                    scheduleDelay: configuration.scheduleDelay,
                                                    maxExportBatchSize: configuration.batchSize)
        let resource = Resource(attributes: [ResourceAttributes.serviceName.rawValue: .string(configuration.serviceName)])
        let tracerProvider = TracerProviderSdk(resource: resource,
                                               spanProcessors: [batchSpanProcessor])

        OpenTelemetry.registerTracerProvider(tracerProvider: tracerProvider)
        OpenTelemetry.registerPropagators(textPropagators: [W3CTraceContextPropagator()],
                                          baggagePropagator: W3CBaggagePropagator())
        tracer = OpenTelemetry.instance.tracerProvider.get(instrumentationName: configuration.serviceName,
                                                           instrumentationVersion: nil)
        self.tracerProvider = tracerProvider
    }

    func stop() {
        tracerProvider?.shutdown()
        spans.removeAll()
        tracerProvider = nil
        tracer = nil
    }

    func startSpan(name: String, type: SpanType, attributes: [String: Any]) -> SpanID? {
        guard let tracer else { return nil }

        let kind = spanKind(from: type)

        let span = tracer.spanBuilder(spanName: name).setSpanKind(spanKind: kind).startSpan()
        attributes.forEach {
            span.setAttribute(key: $0.key, value: AttributeValue($0.value))
        }

        let spanID = save(span)

        return spanID
    }

    func startSpan(
        name: String,
        type: SpanType,
        attributes: [String: Any],
        fromContext context: [String: String]
    ) -> SpanID? {
        guard let tracer else { return nil }

        let getter = OpenTelemetryGetter()
        let extractedSpanContext = OpenTelemetry.instance
            .propagators
            .textMapPropagator
            .extract(carrier: context, getter: getter)

        let spanBuilder = tracer.spanBuilder(spanName: name)
            .setSpanKind(spanKind: spanKind(from: type))
        if let extractedSpanContext {
            let spanContext = SpanContext.createFromRemoteParent(traceId: extractedSpanContext.traceId,
                                                                 spanId: extractedSpanContext.spanId,
                                                                 traceFlags: TraceFlags(),
                                                                 traceState: TraceState())
            spanBuilder.addLink(spanContext: spanContext)
        }
        let span = spanBuilder.startSpan()

        attributes.forEach {
            span.setAttribute(key: $0.key, value: AttributeValue($0.value))
        }

        let spanID = save(span)

        return spanID
    }

    func stopSpan(spanID: SpanID, errorMessage: String?) {
        guard let span = spans[spanID] else { return }

        if let errorMessage {
            span.status = .error(description: errorMessage)
        }

        span.end()

        spans.removeValue(forKey: spanID)
    }

    func updateContext(withSpanID spanID: SpanID) -> [String: String] {
        guard let span = spans[spanID] else { return [:] }

        var context = [String: String]()
        let setter = OpenTelemetrySetter()
        OpenTelemetry.instance.propagators.textMapPropagator.inject(spanContext: span.context,
                                                                    carrier: &context,
                                                                    setter: setter)
        return context
    }

    private func spanKind(from spanType: SpanType) -> SpanKind {
        switch spanType {
        case .consumer: return .consumer
        case .producer: return .producer
        }
    }

    private func save(_ span: Span) -> SpanID {
        let spanID = span.context.spanId.hexString
        spans[spanID] = span
        return spanID
    }
}

struct OpenTelemetrySetter: Setter {
    func set(carrier: inout [String: String], key: String, value: String) {
        carrier[key] = value
    }
}

struct OpenTelemetryGetter: Getter {
    func get(carrier: [String: String], key: String) -> [String]? {
        guard let value = carrier[key] else { return nil }

        return [value]
    }
}
