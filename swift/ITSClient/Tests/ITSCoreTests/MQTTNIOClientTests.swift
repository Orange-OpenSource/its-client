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

import Testing
@testable import ITSCore

struct MQTTNIOClientTests {
    private let clientIdentifier = "MQTTNIOClientTests"
    
    @Test("MQTT anonymous connection should succeed")
    func mqtt_anonymous_connection_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        
        // Then
        #expect(await mqttClient.isConnected)
    }
    
    @Test("MQTT anonymous connection with SSL should succeed")
    func mqtt_anonymous_connection_with_SSL_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 8886,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: true)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        
        // Then
        #expect(await mqttClient.isConnected)
    }
    
    @Test("MQTT authenticated connection should succeed")
    func mqtt_authenticated_connection_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1884,
                                                              clientIdentifier: clientIdentifier,
                                                              userName: "rw",
                                                              password: "readwrite",
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        
        // Then
        #expect(await mqttClient.isConnected)
    }
    
    @Test("MQTT websocket connection should succeed")
    func mqtt_websocket_connection_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 8081,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: true,
                                                              useWebSockets: true)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        
        // Then
        #expect(await mqttClient.isConnected)
    }
    
    @Test("MQTT message should be received if published on a subscribed topic")
    func mqtt_message_should_be_received_if_published_on_a_subscribed_topic() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let topic = "its-test-topic"
        let payload = "payload"
        
        try await confirmation(expectedCount: 1) { confirmation in
            let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { message in
                // Then
                #expect(message.topic == topic)
                #expect(message.payload == payload.data(using: .utf8))
                confirmation()
            }
            
            // When
            try await mqttClient.connect()
            try await mqttClient.subscribe(to: topic)
            try await mqttClient.publish(MQTTMessage(payload: payload.data(using: .utf8)!,
                                                     topic: topic))
            // Wait the message
            try await Task.sleep(for: .seconds(0.5))
        }
    }
    
    @Test("MQTT disconnect after connection should succeed")
    func mqtt_disconnect_after_connection_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        #expect(await mqttClient.isConnected)
        try await mqttClient.disconnect()
        
        // Then
        #expect(await !mqttClient.isConnected)
    }
    
    @Test("MQTT disconnect after connection and subscription should succeed")
    func mqtt_disconnect_after_connection_subscription_should_succeed() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        try await mqttClient.connect()
        #expect(await mqttClient.isConnected)
        try await mqttClient.subscribe(to: "test")
        try await mqttClient.disconnect()
        
        // Then
        #expect(await !mqttClient.isConnected)
    }
    
    @Test("MQTT subscription without connection should fail")
    func mqtt_subscription_without_connection_should_fail() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        do {
            try await mqttClient.subscribe(to: "test")
        } catch {
            // Then
            #expect(error == .clientNotConnected)
        }
    }
    
    @Test("MQTT publication without connection should fail")
    func mqtt_publication_without_connection_should_fail() async throws {
        // Given
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: clientIdentifier,
                                                              useSSL: false)
        let mqttClient = MQTTNIOClient(configuration: mqttClientConfiguration) { _ in }
        
        // When
        do {
            let payload = "payload"
            try await mqttClient.publish(MQTTMessage(payload: payload.data(using: .utf8)!,
                                                     topic: "test"))
        } catch {
            // Then
            #expect(error == .clientNotConnected)
        }
    }
}