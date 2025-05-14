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

struct RoadUserCoordinatorTests {
    @Test("Road user coordinator should send a create event when a new user is incoming")
    func road_user_coordinator_should_send_create_event_when_new_user_incoming() async throws {
        // Given
        let roadUserCoordinator = RoadUserCoordinator()
        let observer = MockRoadUserCoordinatorObserver()
        await roadUserCoordinator.setObserver(observer)

        // When
        let cam = buildCAM()
        let camData = try JSONEncoder().encode(cam)
        await roadUserCoordinator.handleRoadUser(withPayload: camData)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didReceiveCAMCount == 1)

        let currentCreatedRoadUser = try #require(await observer.currentCreatedRoadUser)
        let currentCAM = try #require(await observer.currentCAM)
        #expect(currentCAM.sourceUUID == cam.sourceUUID)
        #expect(currentCreatedRoadUser.stationType == cam.message.basicContainer.stationType)
        #expect(currentCAM.timestamp == cam.timestamp)
    }

    @Test("Road user coordinator should send an update event when a user is updated")
    func road_user_coordinator_should_send_update_event_when_user_updated() async throws {
        // Given
        let roadUserCoordinator = RoadUserCoordinator()
        let observer = MockRoadUserCoordinatorObserver()
        await roadUserCoordinator.setObserver(observer)

        // When
        let cam = buildCAM()
        let camData = try JSONEncoder().encode(cam)
        await roadUserCoordinator.handleRoadUser(withPayload: camData)
        let updatedLatitude = 43.5657460
        let updatedLongitude = 43.5657460
        let camUpdate = buildCAM(latitude: updatedLatitude, longitude: updatedLongitude)
        let camUpdateData = try JSONEncoder().encode(camUpdate)
        await roadUserCoordinator.handleRoadUser(withPayload: camUpdateData)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didUpdateCallsCount == 1)
        #expect(await observer.didReceiveCAMCount == 2)

        let currentCreatedRoadUser = try #require(await observer.currentCreatedRoadUser)
        let currentUpdatedRoadUser = try #require(await observer.currentUpdatedRoadUser)
        #expect(currentUpdatedRoadUser.id == currentCreatedRoadUser.id)
        #expect(currentUpdatedRoadUser.latitude == updatedLatitude)
        #expect(currentUpdatedRoadUser.longitude == updatedLongitude)
    }

    @Test("Road user coordinator should send a remove event when a user is expired")
    func road_user_coordinator_should_send_update_event_when_user_expired() async throws {
        // Given
        let roadUserCoordinator = RoadUserCoordinator()
        let observer = MockRoadUserCoordinatorObserver()
        await roadUserCoordinator.setObserver(observer)

        // When
        let cam = buildCAM()
        let camData = try JSONEncoder().encode(cam)
        await roadUserCoordinator.handleRoadUser(withPayload: camData)

        try await Task.sleep(for: .seconds(4.0))

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didReceiveCAMCount == 1)
        #expect(await observer.didDeleteCallsCount == 1)

        let currentDeletedRoadUser = try #require(await observer.currentDeletedRoadUser)
        #expect(currentDeletedRoadUser.underlyingCAM.sourceUUID == cam.sourceUUID)
        #expect(currentDeletedRoadUser.stationType == cam.message.basicContainer.stationType)
    }

    private func buildCAM(latitude: Double = 43.5657439,
                          longitude: Double = 1.4681494) -> CAM {
        let now = Date().timeIntervalSince1970
        let position = Position(latitude: latitude, longitude: longitude, altitude: 120)
        let basicContainer = BasicContainer(stationType: .pedestrian,
                                            referencePosition: position)
        let highFrequencyContainer = HighFrequencyContainer(heading: 35.0,
                                                            speed: 1.5)
        let camMessage = CAMMessage(stationID: 123456,
                                    generationDeltaTime: now,
                                    basicContainer: basicContainer,
                                    highFrequencyContainer: highFrequencyContainer)
        let cam = CAM(message: camMessage,
                      sourceUUID: "v2x_12345678",
                      timestamp: now)

        return cam
    }
}
