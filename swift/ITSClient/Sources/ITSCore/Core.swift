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

public actor Core {
    private var mqttClient: MQTTClient?
    private var telemetryClient: TelemetryClient?
    private var continuationsByTopic: [String: AsyncStream<CoreMQTTMessage>.Continuation]
    private let spanName = "IoT3 Core MQTT Message"
    private let traceParentProperty = "traceparent"

    public init() {
        continuationsByTopic = [:]
    }

    public func start(coreConfiguration: CoreConfiguration) async throws(CoreError) {
        let telemetryClient = coreConfiguration.telemetryClientConfiguration.map {
            OpenTelemetryClient(configuration: $0)
        }
        let mqttClient = MQTTNIOClient(configuration: coreConfiguration.mqttClientConfiguration)

        try await start(mqttClient: mqttClient, telemetryClient: telemetryClient)
    }

    public func subscribe(to topic: String) async throws(CoreError) -> AsyncStream<CoreMQTTMessage> {
        guard let mqttClient else {
            throw .notStarted
        }

        do {
            try await mqttClient.subscribe(to: topic)
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }

        return AsyncStream { continuation in
            continuationsByTopic[topic] = continuation
            continuation.onTermination = { @Sendable _ in
                Task { [weak self] in
                    try await self?.unsubscribe(from: topic)
                }
            }
        }
    }

    public func unsubscribe(from topic: String) async throws(CoreError) {
        guard let mqttClient else {
            throw .notStarted
        }

        defer {
            let continuation = continuationsByTopic[topic]
            continuation?.finish()
            continuationsByTopic.removeValue(forKey: topic)
        }

        do {
            try await mqttClient.unsubscribe(from: topic)
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }
    }

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

    public func stop() async throws(CoreError) {
        for topic in continuationsByTopic.keys {
            try await unsubscribe(from: topic)
        }

        do {
            try await mqttClient?.disconnect()
        } catch {
            throw .mqttError(EquatableError(wrappedError: error))
        }
        mqttClient = nil

        await telemetryClient?.stop()
        telemetryClient = nil
    }

    func start(mqttClient: MQTTClient, telemetryClient: TelemetryClient?) async throws(CoreError) {
        guard self.mqttClient == nil && self.telemetryClient == nil else {
            return
        }

        self.mqttClient = mqttClient
        await mqttClient.setMessageReceivedHandler(messageReceivedHandler: { message in
            Task { [weak self] in
                guard let self else { return }

                let spanID = await startReceivedMessageSpan(message)
                await stopSpan(spanID: spanID)

                let topicContinuation = await continuationsByTopic[message.topic]
                topicContinuation?.yield(CoreMQTTMessage(payload: message.payload, topic: message.topic))
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
        ["iot3.core.mqtt.topic": topic,
         "iot3.core.mqtt.payload_size": payload.count,
         "iot3.core.sdk_language": "swift"]
    }

    private func stopSpan(spanID: String?, errorMessage: String? = nil) async {
        guard let spanID else { return }

        await telemetryClient?.stopSpan(spanID: spanID, errorMessage: errorMessage)
    }
}
