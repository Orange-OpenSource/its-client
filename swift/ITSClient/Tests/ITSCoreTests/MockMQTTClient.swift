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

actor MockMQTTClient: MQTTClient {
    var isConnected: Bool = false
    private var topics: [String] = []
    private var messageReceivedHandler: (@Sendable (MQTTMessage) -> Void)?
    var setMessageReceivedHandlerCallsCount: Int = 0
    var connectCallsCount: Int = 0
    var subscribeCallsCount: Int = 0
    var unsubscribeCallsCount: Int = 0
    var disconnectCallsCount: Int = 0
    var publishCallsCount: Int = 0
    nonisolated(unsafe) var throwsConnectError: Bool = false
    nonisolated(unsafe) var throwsSubscribeError: Bool = false
    nonisolated(unsafe) var throwsUnsubscribeError: Bool = false
    nonisolated(unsafe) var throwsPublishError: Bool = false

    init(configuration: MQTTClientConfiguration) {}

    func setMessageReceivedHandler(messageReceivedHandler handler: @escaping (@Sendable (MQTTMessage) -> Void)) {
        messageReceivedHandler = handler
        setMessageReceivedHandlerCallsCount += 1
    }

    func connect() async throws(MQTTClientError) {
        if throwsConnectError {
            throw .connectionFailed
        }

        isConnected = true
        connectCallsCount += 1
    }

    func subscribe(to topic: String) async throws(MQTTClientError) {
        if throwsSubscribeError {
            throw .subscriptionFailed
        }

        topics.append(topic)
        subscribeCallsCount += 1
    }

    func unsubscribe(from topic: String) async throws(MQTTClientError) {
        if throwsUnsubscribeError {
            throw .unsubscriptionFailed
        }

        topics.removeAll(where: { $0 == topic })
        unsubscribeCallsCount += 1
    }

    func disconnect() async throws(MQTTClientError) {
        isConnected = false
        disconnectCallsCount += 1
    }

    func publish(_ message: MQTTMessage) async throws(MQTTClientError) {
        if throwsPublishError {
            throw .sendPayloadFailed
        }

        publishCallsCount += 1
    }

    func simulateMessageReceived(_ message: MQTTMessage) {
        guard topics.contains(message.topic) else { return }

        Task { [weak self] in
            await self?.messageReceivedHandler?(message)
        }
    }
}
