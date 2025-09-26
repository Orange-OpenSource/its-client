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
@testable import ITSMobility
import Testing

struct DENMTests {
    private let origin = Origin.mecApplication
    private let latitude = 43.5657439
    private let longitude = 1.4681494
    private let altitude = 46.0
    private let relevanceDistance = RelevanceDistance.lessThan50m
    private let relevanceTrafficDirection = RelevanceTrafficDirection.upstreamTraffic
    private let validityDuration = 10.0
    private let transmissionInterval = 0.2
    private let stationType = StationType.passengerCar
    private let stationID: UInt32 = 654321
    private let originatingStationID: UInt32 = 10000
    private let dateComponents = DateComponents(calendar: .init(identifier: .gregorian),
                                                timeZone: TimeZone(secondsFromGMT: 0),
                                                year: 2025,
                                                month: 3,
                                                day: 26,
                                                hour: 10,
                                                minute: 21,
                                                second: 43,
                                                nanosecond: 586 * 1_000_000) // Milliseconds
    // Wednesday 26 March 2025 10:21:43.586
    private let sourceUUID = "v2x_87654321"
    private let cause = Cause.slowVehicle(subcause: .convoy)
    private let eventPositionHeading = 145.3
    private let eventSpeed = 0.14

    @Test("DENM JSON should be decoded correctly")
    func denm_json_should_be_decoded_correctly() throws {
        // Given
        let denmData = try FileLoader.loadJSONFile("Denm", from: Bundle.module)

        // When
        let denm = try JSONDecoder().decode(DENM.self, from: denmData)

        // Then
        assertDENM(denm)
    }

    @Test("DENM JSON should be encoded correctly")
    func denm_json_should_be_encoded_correctly() throws {
        // Given
        let date = try #require(dateComponents.date)
        let actionID = ActionID(originatingStationID: originatingStationID)
        let position = Position(latitude: latitude, longitude: longitude, altitude: altitude)
        let managementContainer = ManagementContainer(actionID: actionID,
                                                      detectionTime: date.timeIntervalSince1970,
                                                      referenceTime: date.timeIntervalSince1970,
                                                      eventPosition: position,
                                                      relevanceDistance: relevanceDistance,
                                                      relevanceTrafficDirection: relevanceTrafficDirection,
                                                      validityDuration: validityDuration,
                                                      transmissionInterval: transmissionInterval,
                                                      stationType: stationType)
        let situationContainer = SituationContainer(eventType: cause)
        let locationContainer = LocationContainer(eventSpeed: eventSpeed, eventPositionHeading: eventPositionHeading)
        let demMessage = DENMMessage(stationID: stationID,
                                     managementContainer: managementContainer,
                                     situationContainer: situationContainer,
                                     locationContainer: locationContainer)
        let denm = DENM(message: demMessage,
                        origin: origin,
                        sourceUUID: sourceUUID,
                        timestamp: date.timeIntervalSince1970)

        // When
        let encodedData = try JSONEncoder().encode(denm)
        let decodedDENM = try JSONDecoder().decode(DENM.self, from: encodedData)

        // Then
        assertDENM(decodedDENM)
    }

    private func assertDENM(_ denm: DENM) {
        #expect(denm.origin == origin)
        #expect(denm.version == "1.1.3")
        #expect(denm.type == MessageType.denm)
        #expect(denm.sourceUUID == sourceUUID)
        #expect(Date(timeIntervalSince1970: denm.timestamp) == dateComponents.date)
        #expect(denm.message.protocolVersion == 1)
        #expect(denm.message.stationID == stationID)
        let managementContainer = denm.message.managementContainer
        #expect(managementContainer.stationType == stationType)
        #expect(managementContainer.actionID.originatingStationID == originatingStationID)
        #expect(managementContainer.detectionTime == dateComponents.date?.timeIntervalSince1970)
        #expect(managementContainer.referenceTime == dateComponents.date?.timeIntervalSince1970)
        #expect(managementContainer.eventPosition.latitude == latitude)
        #expect(managementContainer.eventPosition.longitude == longitude)
        #expect(managementContainer.eventPosition.altitude == altitude)
        #expect(managementContainer.transmissionInterval == transmissionInterval)
        #expect(managementContainer.validityDuration == validityDuration)
        #expect(managementContainer.relevanceDistance == relevanceDistance)
        #expect(managementContainer.relevanceTrafficDirection == relevanceTrafficDirection)
        let situationContainer = denm.message.situationContainer
        #expect(situationContainer?.eventType == cause)
        let locationContainer = denm.message.locationContainer
        #expect(locationContainer?.eventPositionHeading == eventPositionHeading)
        #expect(locationContainer?.eventSpeed == eventSpeed)
        #expect(locationContainer?.traces.isEmpty ?? false)
        #expect(denm.message.alacarteContainer == nil)
    }
}
