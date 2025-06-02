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
import ITSCommonTests
@testable import ITSCore
import Testing

struct BootstrapTests {
    private let bootstrapService: BootstrapService
    private let bootstrapConfiguration: BootstrapConfiguration
    private let telemetryServiceName: String
    private let expectedClientIdentifier = "external-app-swift"
    private let expectedMQTTHost = "mybroker.com"
    private let expectedMQTTRootTopic = "test"
    private let expectedUserName = "psk_login"
    private let expectedPassword = "psk_password"

    init() throws {
        let urlSessionConfiguration = URLSessionConfiguration.default
        urlSessionConfiguration.protocolClasses = [MockURLProtocol.self]
        let url = try #require(URL(string: "https://foo.com"))
        let repository = ITSBootstrapRepository(url: url,
                                                urlSessionConfiguration: urlSessionConfiguration)
        bootstrapService = BootstrapService(repository: repository)
        bootstrapConfiguration = BootstrapConfiguration(identifier: "test-identifier",
                                                        user: "foo",
                                                        password: "foo",
                                                        role: "external-app")
        telemetryServiceName = "its-tests-service"
    }

    @Test("Bootstrap should use MQTTS and telemetry over HTTPS")
    func bootstrap_should_use_MQTTS_and_telemetry_over_HTTPS() async throws {
        // Given
        let mockData = try FileLoader.loadJSONFile("Bootstrap-MQTTS", from: Bundle.module)
        let mockHandler: MockURLProtocol.MockHandler = { _ in
            MockResponse(httpCode: 200, data: mockData)
        }
        MockURLProtocol.setMock(mockHandler: mockHandler)

        // When
        let bootstrap = try await bootstrapService.bootstrap(bootstrapConfiguration: bootstrapConfiguration)

        // Then
        let mqttClientConfiguration = try #require(bootstrap?.mqttClientConfiguration())
        #expect(mqttClientConfiguration.clientIdentifier == expectedClientIdentifier)
        #expect(mqttClientConfiguration.host == expectedMQTTHost)
        #expect(mqttClientConfiguration.port == 8885)
        #expect(mqttClientConfiguration.userName == expectedUserName)
        #expect(mqttClientConfiguration.password == expectedPassword)
        #expect(mqttClientConfiguration.useSSL)
        #expect(!mqttClientConfiguration.useWebSockets)
        #expect(bootstrap?.mqttRootTopic == expectedMQTTRootTopic)
        let telemetryClientConfiguration = try #require(bootstrap?.telemetryClientConfiguration(
            serviceName: telemetryServiceName
        ))
        let url = URL(string: "https://myopentelemetry.com/telemetry/default/")
        #expect(telemetryClientConfiguration.url == url)
        #expect(telemetryClientConfiguration.user == expectedUserName)
        #expect(telemetryClientConfiguration.password == expectedPassword)
        #expect(telemetryClientConfiguration.serviceName == telemetryServiceName)
    }

    @Test("Bootstrap should use MQTTWSS and telemetry over HTTP")
    func bootstrap_should_use_MQTTWSS_and_telemetry_over_HTTP() async throws {
        // Given
        let mockData = try FileLoader.loadJSONFile("Bootstrap-MQTTWSS", from: Bundle.module)
        let mockHandler: MockURLProtocol.MockHandler = { _ in
            MockResponse(httpCode: 200, data: mockData)
        }
        MockURLProtocol.setMock(mockHandler: mockHandler)

        // When
        let bootstrap = try await bootstrapService.bootstrap(bootstrapConfiguration: bootstrapConfiguration)

        // Then
        let mqttClientConfiguration = try #require(bootstrap?.mqttClientConfiguration())
        #expect(mqttClientConfiguration.clientIdentifier == expectedClientIdentifier)
        #expect(mqttClientConfiguration.host == expectedMQTTHost)
        #expect(mqttClientConfiguration.port == 443)
        #expect(mqttClientConfiguration.userName == expectedUserName)
        #expect(mqttClientConfiguration.password == expectedPassword)
        #expect(mqttClientConfiguration.useSSL)
        #expect(mqttClientConfiguration.useWebSockets)
        #expect(bootstrap?.mqttRootTopic == expectedMQTTRootTopic)
        let telemetryClientConfiguration = try #require(bootstrap?.telemetryClientConfiguration(
            serviceName: telemetryServiceName
        ))
        let url = URL(string: "http://myopentelemetry.com/telemetry/default/")
        #expect(telemetryClientConfiguration.url == url)
        #expect(telemetryClientConfiguration.user == expectedUserName)
        #expect(telemetryClientConfiguration.password == expectedPassword)
        #expect(telemetryClientConfiguration.serviceName == telemetryServiceName)
    }

    @Test("Bootstrap should use MQTT and no telemetry")
    func bootstrap_should_use_MQTT_and_no_telemetry() async throws {
        // Given
        let mockData = try FileLoader.loadJSONFile("Bootstrap-MQTT", from: Bundle.module)
        let mockHandler: MockURLProtocol.MockHandler = { _ in
            MockResponse(httpCode: 200, data: mockData)
        }
        MockURLProtocol.setMock(mockHandler: mockHandler)

        // When
        let bootstrap = try await bootstrapService.bootstrap(bootstrapConfiguration: bootstrapConfiguration)

        // Then
        let mqttClientConfiguration = try #require(bootstrap?.mqttClientConfiguration())
        #expect(mqttClientConfiguration.clientIdentifier == expectedClientIdentifier)
        #expect(mqttClientConfiguration.host == expectedMQTTHost)
        #expect(mqttClientConfiguration.port == 8886)
        #expect(mqttClientConfiguration.userName == expectedUserName)
        #expect(mqttClientConfiguration.password == expectedPassword)
        #expect(!mqttClientConfiguration.useSSL)
        #expect(!mqttClientConfiguration.useWebSockets)
        #expect(bootstrap?.telemetryClientConfiguration(serviceName: telemetryServiceName) == nil)
    }

    @Test("Bootstrap should use MQTTWS and no telemetry")
    func bootstrap_should_use_MQTTWS_and_no_telemetry() async throws {
        // Given
        let mockData = try FileLoader.loadJSONFile("Bootstrap-MQTTWS", from: Bundle.module)
        let mockHandler: MockURLProtocol.MockHandler = { _ in
            MockResponse(httpCode: 200, data: mockData)
        }
        MockURLProtocol.setMock(mockHandler: mockHandler)

        // When
        let bootstrap = try await bootstrapService.bootstrap(bootstrapConfiguration: bootstrapConfiguration)

        // Then
        let mqttClientConfiguration = try #require(bootstrap?.mqttClientConfiguration())
        #expect(mqttClientConfiguration.clientIdentifier == expectedClientIdentifier)
        #expect(mqttClientConfiguration.host == expectedMQTTHost)
        #expect(mqttClientConfiguration.port == 80)
        #expect(mqttClientConfiguration.userName == expectedUserName)
        #expect(mqttClientConfiguration.password == expectedPassword)
        #expect(!mqttClientConfiguration.useSSL)
        #expect(mqttClientConfiguration.useWebSockets)
        #expect(bootstrap?.telemetryClientConfiguration(serviceName: telemetryServiceName) == nil)
    }

    @Test("Bootstrap should not return a configuration if a HTTP error occurs")
    func bootstrap_should_not_return_a_configuration_if_HTTP_error_occurs() async throws {
        // Given
        let mockHandler: MockURLProtocol.MockHandler = { _ in
            MockResponse(httpCode: 401, data: Data())
        }
        MockURLProtocol.setMock(mockHandler: mockHandler)

        // When
        let bootstrap = try await bootstrapService.bootstrap(bootstrapConfiguration: bootstrapConfiguration)

        // Then
        #expect(bootstrap == nil)
    }
}
