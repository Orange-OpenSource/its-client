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
import Testing
@testable import ITSMobility

struct CAMTests {
    private let latitude = 44.7758076
    private let longitude = -0.6528173
    private let altitude = 81.44
    private let semiMajorConfidence = 10
    private let semiMajorOrientation = 1
    private let semiMinorConfidence = 50
    private let altitudeConfidence = AltitudeConfidence.alt_000_02
    private let stationType = StationType.passengerCar
    private let headingConfidence = 0.2
    private let speedConfidence = 0.03
    private let vehiculeLengthConfidence = VehiculeLengthConfidence.noTrailerPresent
    private let heading = 303.7
    private let speed = 0.21
    private let driveDirection = DriveDirection.forward
    private let vehicleLength = 4.0
    private let vehicleWidth = 2.0
    private let longitudinalAcceleration = HighFrequencyContainer.unavailableAcceleration
    private let stationID: UInt32 = 123456
    private let dateComponents = DateComponents(calendar: .init(identifier: .gregorian),
                                                timeZone: TimeZone.gmt,
                                                year: 2025,
                                                month: 3,
                                                day: 25,
                                                hour: 8,
                                                minute: 12,
                                                second: 26,
                                                nanosecond: 165 * 1_000_000) // Milliseconds
    private let sourceUUID = "v2x_12345678"

    @Test("CAM JSON should be decoded correctly")
    func cam_json_should_be_decoded_correctly() throws {
        // Given
        let camData = try FileLoader.loadJSONFile("Cam")

        // When
        let cam = try JSONDecoder().decode(CAM.self, from: camData)

        // Then
        assertCAM(cam)
    }

    @Test("CAM JSON should be encoded correctly")
    func cam_json_should_be_encoded_correctly() throws {
        // Given
        let position = Position(latitude: latitude, longitude: longitude, altitude: altitude)
        let positionConfidence = PositionConfidenceEllipse(semiMajorConfidence: semiMajorConfidence,
                                                           semiMajorOrientation: semiMajorOrientation,
                                                           semiMinorConfidence: semiMinorConfidence)
        let confidence = Confidence(altitude: altitudeConfidence,
                                    positionConfidenceEllipse: positionConfidence)
        let basicContainer = BasicContainer(stationType: stationType,
                                            referencePosition: position,
                                            confidence: confidence)
        let highFrequencyConfidence = HighFrequencyContainerConfidence(heading: headingConfidence,
                                                                       speed: speedConfidence,
                                                                       vehicleLength: vehiculeLengthConfidence)
        let highFrequencyContainer = HighFrequencyContainer(heading: heading,
                                                            speed: speed,
                                                            driveDirection: driveDirection,
                                                            vehicleLength: vehicleLength,
                                                            vehicleWidth: vehicleWidth,
                                                            longitudinalAcceleration: longitudinalAcceleration,
                                                            confidence: highFrequencyConfidence)
        let camMessage = CAMMessage(stationID: stationID,
                                    generationDeltaTime: 1742890411.638,
                                    basicContainer: basicContainer,
                                    highFrequencyContainer: highFrequencyContainer)
        let date = try #require(dateComponents.date)
        let cam = CAM(message: camMessage,
                      sourceUUID: sourceUUID,
                      timestamp: date.timeIntervalSince1970)
        // When
        let encodedData = try JSONEncoder().encode(cam)
        let decodedCAM = try JSONDecoder().decode(CAM.self, from: encodedData)

        // Then
        assertCAM(decodedCAM)
    }

    private func assertCAM(_ cam: CAM) {
        #expect(cam.origin == Origin.originSelf)
        #expect(cam.version == "1.1.3")
        #expect(cam.type == MessageType.cam)
        #expect(cam.sourceUUID == sourceUUID)
        #expect(Date(timeIntervalSince1970: cam.timestamp) == dateComponents.date)
        #expect(cam.message.protocolVersion == 1)
        #expect(cam.message.stationID == stationID)
        #expect(cam.message.etsiGenerationDeltaTime == 28278)
        let basicContainer = cam.message.basicContainer
        #expect(basicContainer.stationType == stationType)
        #expect(basicContainer.referencePosition.latitude == latitude)
        #expect(basicContainer.referencePosition.longitude == longitude)
        #expect(basicContainer.referencePosition.altitude == altitude)
        #expect(basicContainer.confidence?.positionConfidenceEllipse?.semiMajorConfidence == semiMajorConfidence)
        #expect(basicContainer.confidence?.positionConfidenceEllipse?.semiMinorConfidence == semiMinorConfidence)
        #expect(basicContainer.confidence?.positionConfidenceEllipse?.semiMajorOrientation == semiMajorOrientation)
        #expect(basicContainer.confidence?.altitude == altitudeConfidence)
        let highFrequencyContainer = cam.message.highFrequencyContainer
        #expect(highFrequencyContainer?.heading == heading)
        #expect(highFrequencyContainer?.speed == speed)
        #expect(highFrequencyContainer?.longitudinalAcceleration == longitudinalAcceleration)
        #expect(highFrequencyContainer?.driveDirection == driveDirection)
        #expect(highFrequencyContainer?.vehicleLength == vehicleLength)
        #expect(highFrequencyContainer?.vehicleWidth == vehicleWidth)
        #expect(highFrequencyContainer?.confidence?.heading == headingConfidence)
        #expect(highFrequencyContainer?.confidence?.speed == speedConfidence)
        #expect(highFrequencyContainer?.confidence?.vehicleLength == vehiculeLengthConfidence)
        #expect(cam.message.lowFrequencyContainer == nil)
    }
}

