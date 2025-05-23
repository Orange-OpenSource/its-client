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
        try await core.subscribe(to: topic)
        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.publish(message: incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
            await core.stop() // Stop to flush spans
        }

        try await confirmation(expectedCount: 1) { confirmation in
            await core.setMessageReceivedHandler(messageReceivedHandler: { message in
                // Then
                #expect(message.payload == incomingMessage.payload)
                #expect(message.topic == incomingMessage.topic)
                confirmation()
                // Wait a bit to simulate a task that takes time
                Thread.sleep(forTimeInterval: 0.25)
            })
            try await Task.sleep(for: .seconds(3.0))
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
            await core.stop() // Stop to flush spans
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
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: coreConfiguration.mqttClientConfiguration)
        try await core.start(coreConfiguration: coreConfiguration)
        let message = CoreMQTTMessage(payload: Data(), topic: topic)
        try await core.publish(message: message)
        await core.stop() // Stop as you want to flush spans
        // Wait a bit for the spans flush
        try await Task.sleep(for: .seconds(0.5))
    }

    @Test(.bug("https://github.com/Orange-OpenSource/its-client/issues/387",
               "Start Core twice should throw an error each time"))
    func start_core_twice_should_throw_an_error_each_time() async {
        // Given
        let core = Core()

        // When
        let mqttClientConfiguration = MQTTClientConfiguration(host: "badmqtthost.com",
                                                              port: 1883,
                                                              clientIdentifier: UUID().uuidString,
                                                              useSSL: false)
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration)

        for _ in 0..<2 {
            do {
                try await core.start(coreConfiguration: coreConfiguration)
                Issue.record("start should throw an error")
            } catch {
                // Then
                if case .mqttError(let wrappedError) = error {
                    #expect(wrappedError == EquatableError(wrappedError: MQTTClientError.connectionFailed))
                }
            }
        }
    }

#if os(macOS)

    @Test("MQTT connection should be resumed after a network disconnection")
    func mqtt_connection_should_be_resumed_after_network_disconnection() async throws {
        let core = Core()
        let networkManager = NetworkManager()

        let topic = "its-topic-test"
        let incomingMessage = CoreMQTTMessage(payload: Data(), topic: topic)
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: coreConfiguration.mqttClientConfiguration)
        try await core.start(coreConfiguration: coreConfiguration)
        try await core.subscribe(to: topic)
        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.publish(message: incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
        }

        nonisolated(unsafe) var messagesReceivedCount = 0
        await core.setMessageReceivedHandler(messageReceivedHandler: { _ in
            messagesReceivedCount += 1
        })

        try await Task.sleep(for: .seconds(1))

        // Expect one message is received
        #expect(messagesReceivedCount == 1)

        messagesReceivedCount = 0
        // Disable network
        networkManager.disableNetwork()
        try await Task.sleep(for: .seconds(1))

        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.publish(message: incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
        }

        try await Task.sleep(for: .seconds(1))

        // Expect that no message is received
        #expect(messagesReceivedCount == 0)

        // Enable the network
        networkManager.enableNetwork()
        try await Task.sleep(for: .seconds(5))

        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.publish(message: incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
        }

        try await Task.sleep(for: .seconds(1))

        // Expect that one message is received
        #expect(messagesReceivedCount == 1)

        await core.stop()
    }
#endif
}
