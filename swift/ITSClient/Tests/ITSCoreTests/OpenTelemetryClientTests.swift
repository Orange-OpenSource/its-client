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
@testable import ITSCore
import Testing

struct OpenTelemetryClientTests {
    private let telemetryClientConfiguration: TelemetryClientConfiguration

    init() throws {
        let url = try #require(URL(string: "http://localhost:4318"))
        telemetryClientConfiguration = TelemetryClientConfiguration(url: url,
                                                                    serviceName: "its-tests-service")
    }

    @Test("Send several consumer and producer spans")
    func send_several_consumer_and_producer_spans() async throws {
        let openTelemetryClient = await OpenTelemetryClient(configuration: telemetryClientConfiguration)

        await openTelemetryClient.start()
        await withThrowingTaskGroup(of: Void.self) { group in
            for taskIndex in 0..<20 {
                group.addTask {
                    if taskIndex.isMultiple(of: 2) {
                        try await startConsumerSpan(telemetryClient: openTelemetryClient)
                    } else {
                        try await startProducerSpan(telemetryClient: openTelemetryClient)
                    }
                }
            }
        }

        await openTelemetryClient.stop()
        try await Task.sleep(seconds: 0.1)
    }

    @Test("Send a producer and a child consumer span")
    func send_producer_and_child_consumer_span() async throws {
        let openTelemetryClient = await OpenTelemetryClient(configuration: telemetryClientConfiguration)

        await openTelemetryClient.start()
        let traceParent = try await startProducerSpan(telemetryClient: openTelemetryClient)
        try await startConsumerSpan(telemetryClient: openTelemetryClient,
                                    traceParent: traceParent)

        await openTelemetryClient.stop()
        try await Task.sleep(seconds: 0.1)
    }

    @Test("Send a producer span with error")
    func send_producer_span_with_error() async throws {
        let openTelemetryClient = await OpenTelemetryClient(configuration: telemetryClientConfiguration)

        await openTelemetryClient.start()
        try await startProducerSpan(telemetryClient: openTelemetryClient,
                                    errorMessage: "Test error")

        await openTelemetryClient.stop()
        try await Task.sleep(seconds: 0.1)
    }

    private func startConsumerSpan(telemetryClient: TelemetryClient, traceParent: String? = nil) async throws {
        let consumerAttributes = ["testAtribute": "consumer"]
        let context = traceParent.map { ["traceparent": $0] } ?? [:]
        let consumerSpanID = try #require(await telemetryClient.startSpan(name: "Consumer span",
                                                                          type: .consumer,
                                                                          attributes: consumerAttributes,
                                                                          fromContext: context))
        try await Task.sleep(seconds: 0.1)
        await telemetryClient.stopSpan(spanID: consumerSpanID)
    }

    @discardableResult
    private func startProducerSpan(
        telemetryClient: TelemetryClient,
        errorMessage: String? = nil
    ) async throws -> String? {
        let producerAttributes = ["testAtribute": "producer"]
        let producerSpanID = try #require(await telemetryClient.startSpan(name: "Producer span",
                                                                          type: .producer,
                                                                          attributes: producerAttributes))
        let context = await telemetryClient.updateContext(withSpanID: producerSpanID)
        try await Task.sleep(seconds: 0.1)
        await telemetryClient.stopSpan(spanID: producerSpanID, errorMessage: errorMessage)

        return context["traceparent"]
    }
}
