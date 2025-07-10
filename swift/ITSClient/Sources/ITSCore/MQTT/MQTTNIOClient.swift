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
import NIOTransportServices

actor MQTTNIOClient: MQTTClient {
    private let client: MQTTNIO.MQTTClient
    private let listenerName = "MQTTNIOClientListener"
    private var messageReceivedHandler: (@Sendable (MQTTMessage) -> Void)?
    private var topicSubscriptions = Set<String>()
    private var isReconnecting = false
    private var reconnectionTask: Task<Void, Never>?
    private var networkMonitoringTask: Task<Void, Never>?
    private var isDisconnectedFromUser = false
    private let networkMonitor: NetworkMonitor

    var isConnected: Bool {
        client.isActive()
    }

    init(configuration: MQTTClientConfiguration) async {
        client = MQTTNIO.MQTTClient(
            host: configuration.host,
            port: configuration.port,
            identifier: configuration.clientIdentifier,
            eventLoopGroupProvider: .shared(NIOTSEventLoopGroup()),
            configuration: .init(version: .v5_0,
                                 userName: configuration.userName,
                                 password: configuration.password,
                                 useSSL: configuration.useSSL,
                                 useWebSockets: configuration.useWebSockets)
        )
        networkMonitor = NetworkMonitor()
        client.addPublishListener(named: listenerName) { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let publishInfo):
                let receivedData = Data(buffer: publishInfo.payload)
                let userProperty = publishInfo.properties.compactMap {
                    switch $0 {
                    case .userProperty(let key, let value):
                        return MQTTMessageUserProperty(key: key, value: value)
                    default:
                        return nil
                    }
                }.first
                let message = MQTTMessage(payload: receivedData,
                                          topic: publishInfo.topicName,
                                          userProperty: userProperty)
                Task {
                    await messageReceivedHandler?(message)
                }
            default:
                break
            }
        }
        client.addCloseListener(named: listenerName) { [weak self] _ in
            guard let self else { return }

            Task {
                // For now, there's no disconnect reason to check if it's an event triggered
                // by a disconnect or a lost connection with the server.
                // https://github.com/swift-server-community/mqtt-nio/issues/163
                // Use of `isDisconnectedFromUser` to workaround this.
                guard await !isDisconnectedFromUser else { return }

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
            // As MQTTNIO close connection event is not triggered quickly when network is lost,
            // monitor the network to be more reactive.
            startNetworkMonitoring()
        } catch {
            throw .connectionFailed
        }
    }

    func subscribe(to topic: String) async throws(MQTTClientError) {
        // Save subscription even if an error occurs or we're reconnecting
        addTopicToSubscriptions(topic)

        guard !isReconnecting else { return }

        do {
            let subscriptions = [MQTTSubscribeInfoV5(topicFilter: topic, qos: .atLeastOnce)]
            _ = try await client.v5.subscribe(to: subscriptions)
        } catch {
            throw .subscriptionFailed
        }
    }

    func unsubscribe(from topic: String) async throws(MQTTClientError) {
        // Remove subscription even if an error occurs or we're reconnecting
        removeTopicFromSubscriptions(topic)

        guard !isReconnecting else { return }

        do {
            _ = try await client.v5.unsubscribe(from: [topic])
        } catch {
            throw .unsubscriptionFailed
        }
    }

    func disconnect() async throws(MQTTClientError) {
        stopReconnectionTask()
        stopNetworkMonitoring()

        // Remove all subscriptions
        removeAllSubscriptions()

        guard isConnected else { return }

        do {
            try await client.v5.disconnect()
            isDisconnectedFromUser = true
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
        topicSubscriptions.insert(topic)
    }

    private func removeTopicFromSubscriptions(_ topic: String) {
        topicSubscriptions.remove(topic)
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

    private func startNetworkMonitoring() {
        guard networkMonitoringTask == nil else { return }

        networkMonitoringTask = Task { [weak self] in
            guard let self else { return }

            var previousNetworkStatus: NetworkStatus?
            for await networkStatus in networkMonitor.start() {
                if await isConnected && networkStatus != previousNetworkStatus &&
                    networkStatus == .disconnected {
                    // Disconnect to close connection early when the network is disconnected
                    try? await client.v5.disconnect()
                }
                previousNetworkStatus = networkStatus
            }
        }
    }

    private func stopNetworkMonitoring() {
        networkMonitoringTask?.cancel()
    }
}
