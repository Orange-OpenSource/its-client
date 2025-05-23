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

/// An object that manages a MQTT client and a telemetry client.
/// Depending the configuration, MQTT message publishing and reception might be automatically traced.
public actor Core {
    private var mqttClient: MQTTClient?
    private var telemetryClient: TelemetryClient?
    private var messageReceivedHandler: (@Sendable (CoreMQTTMessage) -> Void)?
    private let spanName = "IoT3 Core MQTT Message"
    private let traceParentProperty = "traceparent"

    /// Initializes a `Core`.
    public init() {}

    /// Sets the message received handler.
    /// - Parameter messageReceivedHandler: A handler called when a message `CoreMQTTMessage` is received.
    public func setMessageReceivedHandler(messageReceivedHandler: (@escaping @Sendable (CoreMQTTMessage) -> Void)) {
        self.messageReceivedHandler = messageReceivedHandler
    }

    /// Starts the `Core` with a configuration to connect to a MQTT server and initialize the telemetry client.
    /// - Parameter coreConfiguration: The configuration used to start the MQTT client and the telemetry client.
    /// - Throws: A `CoreError` if the MQTT connection fails.
    public func start(coreConfiguration: CoreConfiguration) async throws(CoreError) {
        let telemetryClient = coreConfiguration.telemetryClientConfiguration.map {
            OpenTelemetryClient(configuration: $0)
        }
        let mqttClient = MQTTNIOClient(configuration: coreConfiguration.mqttClientConfiguration)

        try await start(mqttClient: mqttClient, telemetryClient: telemetryClient)
    }

    /// Subscribes to a MQTT topic.
    /// If the `TelemetryClientConfiguration`is set, a linked span is created.
    /// - Parameter topic: The topic to subscribe.
    /// - Throws: A `CoreError` if the MQTT subscription fails or the `Core` is not started.
    public func subscribe(to topic: String) async throws(CoreError) {
        guard let mqttClient else {
            throw .notStarted
        }

        do {
            try await mqttClient.subscribe(to: topic)
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }
    }

    /// Unsubscribes from a MQTT topic.
    /// - Parameter topic: The topic to unsubscribe.
    /// - Throws: A `CoreError` if the MQTT unsubscription fails or the `Core` is not started.
    public func unsubscribe(from topic: String) async throws(CoreError) {
        guard let mqttClient else {
            throw .notStarted
        }

        do {
            try await mqttClient.unsubscribe(from: topic)
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }
    }

    /// Publishes a MQTT message on a topic.
    /// If the `TelemetryClientConfiguration`is set, a span is created.
    /// - Parameter message: The message to publish.
    /// - Throws: A `CoreError` if the MQTT publishing fails or the `Core` is not started.
    public func publish(message: CoreMQTTMessage) async throws(CoreError) {
        guard let mqttClient else {
            throw .notStarted
        }

        let spanID = await startSentMessageSpan(message)
        var traceParent: String?
        if let spanID {
            let context = await telemetryClient?.updateContext(withSpanID: spanID)
            traceParent = context?[traceParentProperty]
        }

        do {
            let userProperty = traceParent.map { MQTTMessageUserProperty(key: traceParentProperty, value: $0) }
            let message = MQTTMessage(payload: message.payload,
                                      topic: message.topic,
                                      userProperty: userProperty)
            try await mqttClient.publish(message)
            await stopSpan(spanID: spanID)
        } catch {
            await stopSpan(spanID: spanID, errorMessage: error.localizedDescription)
            throw .mqttError(EquatableError(wrappedError: error))
        }
    }

    /// Stops the `Core` disconnecting the MQTT client and stopping the telemetry client.
    public func stop() async {
        do {
            try await mqttClient?.disconnect()
        } catch {}
        mqttClient = nil

        await telemetryClient?.stop()
        telemetryClient = nil
    }

    func start(mqttClient: MQTTClient, telemetryClient: TelemetryClient?) async throws(CoreError) {
        guard self.mqttClient == nil && self.telemetryClient == nil else {
            return
        }

        self.mqttClient = mqttClient
        await mqttClient.setMessageReceivedHandler(messageReceivedHandler: { [weak self] message in
            guard let self else { return }

            Task {
                let spanID = await startReceivedMessageSpan(message)
                let message = CoreMQTTMessage(payload: message.payload, topic: message.topic)
                await messageReceivedHandler?(message)
                await stopSpan(spanID: spanID)
            }
        })
        self.telemetryClient = telemetryClient

        do {
            try await self.mqttClient?.connect()
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }

        await self.telemetryClient?.start()
    }

    private func startReceivedMessageSpan(_ message: MQTTMessage) async -> String? {
        let traceParent = traceParent(from: message)
        let attributes = buildAttributes(payload: message.payload, topic: message.topic)
        let context = traceParent.map { [traceParentProperty: $0] } ?? [:]
        return await telemetryClient?.startSpan(name: spanName,
                                                type: .consumer,
                                                attributes: attributes,
                                                fromContext: context)
    }

    private func startSentMessageSpan(_ message: CoreMQTTMessage) async -> String? {
        let attributes = buildAttributes(payload: message.payload, topic: message.topic)
        return await telemetryClient?.startSpan(name: spanName,
                                                type: .producer,
                                                attributes: attributes)
    }

    private func traceParent(from message: MQTTMessage) -> String? {
        guard let userProperty = message.userProperty else { return nil }

        return userProperty.key == traceParentProperty ? userProperty.value : nil
    }

    private func buildAttributes(payload: Data, topic: String) -> [String: Sendable] {
        [
            "iot3.core.mqtt.topic": topic,
            "iot3.core.mqtt.payload_size": payload.count,
            "iot3.core.sdk_language": "swift",
        ]
    }

    private func stopSpan(spanID: String?, errorMessage: String? = nil) async {
        guard let spanID else { return }

        await telemetryClient?.stopSpan(spanID: spanID, errorMessage: errorMessage)
    }
}
