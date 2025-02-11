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
import MQTTNIO
import NIOCore

actor MQTTNIOClient: MQTTClient {
    private let client: MQTTNIO.MQTTClient
    private let listenerName = "MQTTNIOClientListener"
    private var messageReceivedHandler: (@Sendable (MQTTMessage) -> Void)?
    private var topicSubscriptions = [String]()
    private var isReconnecting = false
    private var reconnectionTask: Task<Void, Never>?
    private var isDisconnectedFromUser = false

    var isConnected: Bool {
        client.isActive()
    }

    init(configuration: MQTTClientConfiguration) {
        client = MQTTNIO.MQTTClient(
            host: configuration.host,
            port: configuration.port,
            identifier: configuration.clientIdentifier,
            eventLoopGroupProvider: .createNew,
            configuration: .init(version: .v5_0,
                                 userName: configuration.userName,
                                 password: configuration.password,
                                 useSSL: configuration.useSSL,
                                 useWebSockets: configuration.useWebSockets)
        )
        client.addPublishListener(named: listenerName) { result in
            switch result {
            case .success(let publishInfo):
                let receivedData = Data(buffer: publishInfo.payload)
                let userProperty = publishInfo.properties.compactMap({
                    switch $0 {
                    case .userProperty(let key, let value):
                        return MQTTMessageUserProperty(key: key, value: value)
                    default:
                        return nil
                    }
                }).first
                let message = MQTTMessage(payload: receivedData,
                                          topic: publishInfo.topicName,
                                          userProperty: userProperty)
                Task { [weak self] in
                    await self?.messageReceivedHandler?(message)
                }
            default:
                break
            }
        }
        client.addCloseListener(named: listenerName) { _ in
            Task { [weak self] in
                // For now, there's no disconnect reason to check if it's an event triggered
                // by a disconnect or a lost connection with the server.
                // https://github.com/swift-server-community/mqtt-nio/issues/163
                // Use of `isDisconnectedFromUser` to workaround this.
                guard let self, await !isDisconnectedFromUser else { return }

                await startReconnectionTask()
            }
        }
    }

    deinit {
        client.removePublishListener(named: listenerName)
        client.removeCloseListener(named: listenerName)

        do {
            try client.syncShutdownGracefully()
        } catch {}
    }

    func setMessageReceivedHandler(messageReceivedHandler: @escaping (@Sendable (MQTTMessage) -> Void)) {
        self.messageReceivedHandler = messageReceivedHandler
    }

    func connect() async throws(MQTTClientError) {
        guard !isConnected else { return }

        do {
            isDisconnectedFromUser = false
            _ = try await client.v5.connect(cleanStart: true)
        } catch {
            throw .connectionFailed
        }
    }

    func subscribe(to topic: String) async throws(MQTTClientError) {
        // Only save subscription when reconnecting
        guard !isReconnecting else {
            addTopicToSubscriptions(topic)
            return
        }

        do {
            _ = try await client.v5.subscribe(to: [MQTTSubscribeInfoV5(topicFilter: topic,
                                                                       qos: .atLeastOnce)])
            addTopicToSubscriptions(topic)
        } catch {
            throw .subscriptionFailed
        }
    }

    func unsubscribe(from topic: String) async throws(MQTTClientError) {
        // Only remove subsription when reconnecting
        guard !isReconnecting else {
            removeTopicFromSubscriptions(topic)
            return
        }

        do {
            _ = try await client.v5.unsubscribe(from: [topic])
            removeTopicFromSubscriptions(topic)
        } catch {
            throw .unsubscriptionFailed
        }
    }

    func disconnect() async throws(MQTTClientError) {
        stopReconnectionTask()

        guard isConnected else {
            removeAllSubscriptions()
            return
        }

        do {
            try await client.v5.disconnect()
            isDisconnectedFromUser = true
            removeAllSubscriptions()
        } catch {
            throw .disconnectionFailed
        }
    }

    func publish(_ message: MQTTMessage) async throws(MQTTClientError) {
        // Skip message when reconnecting
        guard !isReconnecting else { return }

        do {
            let mqttProperties = message.userProperty.map {
                MQTTProperties([.userProperty($0.key, $0.value)])
            }
            _ = try await client.v5.publish(to: message.topic,
                                            payload: ByteBufferAllocator().buffer(data: message.payload),
                                            qos: .atLeastOnce,
                                            properties: mqttProperties ?? .init())
        } catch {
            throw .sendPayloadFailed
        }
    }

    private func addTopicToSubscriptions(_ topic: String) {
        topicSubscriptions.append(topic)
    }

    private func removeTopicFromSubscriptions(_ topic: String) {
        topicSubscriptions.removeAll(where: { $0 == topic })
    }

    private func removeAllSubscriptions() {
        topicSubscriptions.removeAll()
    }

    private func startReconnectionTask() {
        stopReconnectionTask()
        reconnectionTask = Task { [weak self] in
            guard let self else { return }

            var reconnectionSuccess = false
            while !reconnectionSuccess && !Task.isCancelled {
                do {
                    try await reconnect()
                    reconnectionSuccess = true
                } catch {}
            }
        }
    }

    private func stopReconnectionTask() {
        reconnectionTask?.cancel()
    }

    private func reconnect() async throws {
        isReconnecting = true
        try await connect()
        isReconnecting = false

        // Resume current subscriptions
        for topic in topicSubscriptions {
            try await subscribe(to: topic)
        }
    }
}
