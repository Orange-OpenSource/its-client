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
import Testing
@testable import ITSCore

struct CoreTests {
    private let coreConfiguration: CoreConfiguration

    init() throws {
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: UUID().uuidString,
                                                              useSSL: false)
        let url = try #require(URL(string: "http://localhost:4318"))
        let telemetryClientConfiguration = TelemetryClientConfiguration(url: url,
                                                                        serviceName: "its-tests-service")
        coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration,
                                              telemetryClientConfiguration: telemetryClientConfiguration)
    }

    @Test("A message sent to a subscribed topic should be received and create a linked span")
    func message_sent_to_subscribed_topic_should_be_received_and_create_linked_span() async throws {
        // Given
        let core = Core()

        // When
        let topic = "its-topic-test"
        let incomingMessage = CoreMQTTMessage(payload: Data(), topic: topic)
        try await core.start(coreConfiguration: coreConfiguration)
        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.publish(message: incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
            try await core.stop() // Stop to flush spans
        }

        try await confirmation(expectedCount: 1) { confirmation in
            for await message in try await core.subscribe(to: topic) {
                // Then
                #expect(message.payload == incomingMessage.payload)
                #expect(message.topic == incomingMessage.topic)
                confirmation()
            }
        }

        // Wait a bit for the spans flush
        try await Task.sleep(for: .seconds(0.5))
    }

    @Test("A message sent with an error should create a span with an error")
    func message_sent_with_error_should_create_span_with_error() async throws {
        // Given
        let core = Core()

        // When
        let topic = "#"
        try await core.start(coreConfiguration: coreConfiguration)
        let message = CoreMQTTMessage(payload: Data(), topic: topic)
        do {
            try await core.publish(message: message)
        } catch {
            try await core.stop() // Stop to flush spans
            // Wait a bit for the spans flush
            try await Task.sleep(for: .seconds(0.5))
        }
    }

    @Test("Send a message without a telemetry configuration should not create a span")
    func send_message_without_telemetry_should_not_create_span() async throws {
        // Given
        let core = Core()

        // When
        let topic = "its-topic-test"
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: coreConfiguration.mqttClientConfiguration,
                                                  telemetryClientConfiguration: nil)
        try await core.start(coreConfiguration: coreConfiguration)
        let message = CoreMQTTMessage(payload: Data(), topic: topic)
        try await core.publish(message: message)
        try await core.stop() // Stop as you want to flush spans
        // Wait a bit for the spans flush
        try await Task.sleep(for: .seconds(0.5))
    }
}
