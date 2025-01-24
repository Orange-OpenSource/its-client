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
    private let mockMQTTClient: MockMQTTClient
    private let mockTelemetryClient: MockTelemetryClient

    init() {
        let mqttClientConfiguration = MQTTClientConfiguration(host: "",
                                                              port: 0,
                                                              clientIdentifier: "",
                                                              useSSL: false)
        mockMQTTClient = MockMQTTClient(configuration: mqttClientConfiguration)
        let telemetryClientConfiguration = TelemetryClientConfiguration(url: URL(string: "http://foo.com")!,
                                                                        serviceName: "")
        mockTelemetryClient = MockTelemetryClient(configuration: telemetryClientConfiguration)
    }

    @Test("Core start with telemetry configuration should connect to MQTT and start telemetry")
    func core_start_with_telemetry_should_connect_to_MQTT_and_start_telemetry() async throws {
        // Given
        let core = Core()

        // When
        try await core.start(mqttClient: mockMQTTClient, telemetryClient: mockTelemetryClient)

        // Then
        #expect(await mockMQTTClient.isConnected)
        #expect(await mockMQTTClient.setMessageReceivedHandlerCallsCount == 1)
        #expect(await mockMQTTClient.connectCallsCount == 1)
        #expect(await mockTelemetryClient.startCallsCount == 1)
    }

    @Test("Core stop should finish subscribe stream and telemetry")
    func core_stop_should_finish_subscribe_stream_and_telemetry() async throws {
        // Given
        let core = Core()

        // When
        try await core.start(mqttClient: mockMQTTClient, telemetryClient: mockTelemetryClient)
        Task {
            try await Task.sleep(for: .seconds(0.5))
            try await core.stop()
        }

        // Then
        try await confirmation(expectedCount: 1) { confirmation in
            for await _ in try await core.subscribe(to: "topic") {
            }
            confirmation()
        }
        #expect(await !mockMQTTClient.isConnected)
        #expect(await mockTelemetryClient.stopCallsCount == 1)
    }

    @Test("Core should forward received messages and send a telemetry span")
    func core_should_forward_received_messages_and_send_telemetry_span() async throws {
        // Given
        let core = Core()
        let topic = "topic"

        // When
        try await core.start(mqttClient: mockMQTTClient, telemetryClient: mockTelemetryClient)
        let incomingMessage = MQTTMessage(payload: Data(), topic: topic, userProperty: nil)
        Task {
            try await Task.sleep(for: .seconds(0.5))
            await mockMQTTClient.simulateMessageReceived(incomingMessage)
            await mockMQTTClient.simulateMessageReceived(incomingMessage)
            try await Task.sleep(for: .seconds(0.5))
            try await core.unsubscribe(from: topic)
        }

        // Then
        try await confirmation(expectedCount: 2) { confirmation in
            for await message in try await core.subscribe(to: topic) {
                #expect(message.payload == incomingMessage.payload)
                #expect(message.topic == incomingMessage.topic)
                confirmation()
            }
        }
        #expect(await mockTelemetryClient.startSpanWithContextCallsCount == 2)
        #expect(await mockTelemetryClient.stopSpanCallsCount == 2)
    }

    @Test("Core start should throw an error when MQTT client fails to connect")
    func core_start_should_throw_error_when_mqtt_client_fails_to_connect() async throws {
        // Given
        let core = Core()

        do {
            // When
            mockMQTTClient.throwsConnectError = true
            try await core.start(mqttClient: mockMQTTClient, telemetryClient: nil)
            Issue.record("start should throw an error")
        } catch {
            // Then
            if case .mqttError(let wrappedError) = error {
                #expect(wrappedError == EquatableError(wrappedError: MQTTClientError.connectionFailed))
            }
        }
    }

    @Test("Core subscribe stream should throw an error when not started")
    func core_subscribe_stream_should_throw_error_when_not_started() async throws {
        // Given
        let core = Core()

        // When
        let task = Task {
            for await _ in try await core.subscribe(to: "topic") {
            }
        }

        // Then
        await #expect(throws: CoreError.notStarted) {
            try await task.value
        }
    }

    @Test("Core subscribe stream should throw an error when MQTT error occurs")
    func core_subscribe_stream_should_throw_error_when_mqtt_error_occurs() async throws {
        // Given
        let core = Core()

        // When
        try await core.start(mqttClient: mockMQTTClient, telemetryClient: nil)
        mockMQTTClient.throwsSubscribeError = true
        let task = Task {
            for await _ in try await core.subscribe(to: "topic") {
            }
        }

        // Then
        do {
            try await task.value
        } catch {
            if case .mqttError(let wrappedError) = error as? CoreError {
                #expect(wrappedError == EquatableError(wrappedError: MQTTClientError.subscriptionFailed))
            }
        }
    }

    @Test("Core unsubscribe should throw an error when not started")
    func core_unsubscribe_should_throw_error_when_not_started() async throws {
        // Given
        let core = Core()

        // Then
        await #expect(throws: CoreError.notStarted) {
            // When
            try await core.unsubscribe(from: "topic")
        }
    }

    @Test("Core unsubscribe should throw an error when MQTT error occurs")
    func core_unsubscribe_should_throw_error_when_mqtt_error_occurs() async throws {
        // Given
        let core = Core()

        do {
            // When
            try await core.start(mqttClient: mockMQTTClient, telemetryClient: nil)
            mockMQTTClient.throwsUnsubscribeError = true
            try await core.unsubscribe(from: "topic")
            Issue.record("unsubscribe should throw an error")
        } catch {
            // Then
            if case .mqttError(let wrappedError) = error {
                #expect(wrappedError == EquatableError(wrappedError: MQTTClientError.unsubscriptionFailed))
            }
        }
    }

    @Test("Core publish should send a message and send a telemetry span")
    func core_publish_should_send_message_and_send_telemetry_span() async throws {
        // Given
        let core = Core()

        // When
        try await core.start(mqttClient: mockMQTTClient, telemetryClient: mockTelemetryClient)
        let message = CoreMQTTMessage(payload: Data(), topic: "topic")
        try await core.publish(message: message)

        // Then
        #expect(await mockMQTTClient.publishCallsCount == 1)
        #expect(await mockTelemetryClient.startSpanCallsCount == 1)
        #expect(await mockTelemetryClient.stopSpanCallsCount == 1)
    }

    @Test("Core publish should throw an error when not started")
    func core_publish_should_throw_error_when_not_started() async throws {
        // Given
        let core = Core()

        // Then
        await #expect(throws: CoreError.notStarted) {
            // When
            let message = CoreMQTTMessage(payload: Data(), topic: "topic")
            try await core.publish(message: message)
        }
    }

    @Test("Core publish should throw an error when MQTT error occurs")
    func core_publish_should_throw_error_when_mqtt_error_occurs() async throws {
        // Given
        let core = Core()

        do {
            // When
            try await core.start(mqttClient: mockMQTTClient, telemetryClient: nil)
            mockMQTTClient.throwsPublishError = true
            let message = CoreMQTTMessage(payload: Data(), topic: "topic")
            try await core.publish(message: message)
            Issue.record("publish should throw an error")
        } catch {
            // Then
            if case .mqttError(let wrappedError) = error {
                #expect(wrappedError == EquatableError(wrappedError: MQTTClientError.sendPayloadFailed))
            }
        }
    }
}
