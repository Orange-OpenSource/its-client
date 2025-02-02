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
    private var subscribedTopics = [String]()
    
    var isConnected: Bool {
        client.isActive()
    }
    
    init(configuration: MQTTClientConfiguration, messageReceivedHandler: (@Sendable @escaping (MQTTMessage) -> Void)) {
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
        client.addPublishListener(named: listenerName) { [messageReceivedHandler] result in
            switch result {
            case .success(let publishInfo):
                let receivedData = Data(buffer: publishInfo.payload)
                let message = MQTTMessage(payload: receivedData, topic: publishInfo.topicName)
                messageReceivedHandler(message)
            default:
                break
            }
        }
    }
    
    deinit {
        client.removePublishListener(named: listenerName)
        
        do {
            try client.syncShutdownGracefully()
        } catch {}
    }
    
    func connect() async throws(MQTTClientError) {
        guard !isConnected else { return }
        
        do {
            _ = try await client.v5.connect(cleanStart: true)
        } catch {
            throw .connectionFailed
        }
    }
    
    func subscribe(to topic: String) async throws(MQTTClientError) {
        guard isConnected else {
            throw .clientNotConnected
        }
        
        do {
            _ = try await client.v5.subscribe(to: [MQTTSubscribeInfoV5(topicFilter: topic,
                                                                       qos: .atLeastOnce)])
            subscribedTopics.append(topic)
        } catch {
            throw .subscriptionFailed
        }
    }
    
    func disconnect() async throws(MQTTClientError) {
        guard isConnected else { return }
        
        do {
            if !subscribedTopics.isEmpty {
                _ = try await client.v5.unsubscribe(from: subscribedTopics)
            }
            try await client.v5.disconnect()
        } catch {
            throw .disconnectionFailed
        }
    }
    
    func publish(_ message: MQTTMessage) async throws(MQTTClientError) {
        guard isConnected else {
            throw .clientNotConnected
        }
        
        do {
            _ = try await client.v5.publish(to: message.topic,
                                            payload: ByteBufferAllocator().buffer(data: message.payload),
                                            qos: .atLeastOnce)
        } catch {
            throw .sendPayloadFailed
        }
    }
}

