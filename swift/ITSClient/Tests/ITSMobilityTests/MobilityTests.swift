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
import ITSCore
@testable import ITSMobility
import Testing

struct MobilityTests {
    private let mobility: Mobility
    private let mobilityConfiguration: MobilityConfiguration

    init() throws {
        mobility = Mobility()
        let mqttClientConfiguration = MQTTClientConfiguration(host: "test.mosquitto.org",
                                                              port: 1883,
                                                              clientIdentifier: UUID().uuidString,
                                                              useSSL: false)
        let url = try #require(URL(string: "http://localhost:4318"))
        let telemetryClientConfiguration = TelemetryClientConfiguration(url: url,
                                                                        serviceName: "its-tests-service")
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration,
                                                  telemetryClientConfiguration: telemetryClientConfiguration)
        mobilityConfiguration = MobilityConfiguration(coreConfiguration: coreConfiguration,
                                                      stationID: UInt32.random(in: 0..<UInt32.max))
    }

    @Test("Send position should send a payload on a topic computed from coordinates")
    func send_position_should_send_payload_on_topic_computed_from_coordinates() async throws {
        try await mobility.start(mobilityConfiguration: mobilityConfiguration)
        try await mobility.sendPosition(stationType: .pedestrian,
                                        latitude: 43.63516355648167,
                                        longitude: 1.3744570239910097,
                                        altitude: 155,
                                        heading: 45,
                                        speed: 8.1)
        await mobility.stop()
        // Wait a bit for the spans flush
        try await Task.sleep(for: .seconds(0.5))
    }

    @Test("Send alert should send a payload on a topic computed from coordinates")
    func send_alert_should_send_payload_on_topic_computed_from_coordinates() async throws {
        try await mobility.start(mobilityConfiguration: mobilityConfiguration)
        try await mobility.sendAlert(stationType: .pedestrian,
                                     latitude: 43.63516355648167,
                                     longitude: 1.3744570239910097,
                                     altitude: 155,
                                     cause: .trafficCondition())
        await mobility.stop()
        // Wait a bit for the spans flush
        try await Task.sleep(for: .seconds(0.5))
    }
}
