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
@testable import ITSMobility
import Testing

struct RoadAlarmCoordinatorTests {
    @Test("Road alarm coordinator should send a create event when a new alarm is incoming")
    func road_alarm_coordinator_should_send_create_event_when_new_alarm_incoming() async throws {
        // Given
        let roadAlarmCoordinator = RoadAlarmCoordinator()
        let observer = MockRoadAlarmCoordinatorObserver()
        await roadAlarmCoordinator.setObserver(observer)

        // When
        let denm = buildDENM()
        let denmData = try JSONEncoder().encode(denm)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmData)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didReceiveDENMCount == 1)

        let expectedOriginatingStationID = denm.message.managementContainer.actionID.originatingStationID
        let currentCreatedRoadAlarm = try #require(await observer.currentCreatedRoadAlarm)
        let currentDENM = try #require(await observer.currentDENM)
        let denmMessage = currentCreatedRoadAlarm.underlyingDENM.message
        let originatingStationID = denmMessage.managementContainer.actionID.originatingStationID
        #expect(originatingStationID == expectedOriginatingStationID)
        #expect(currentDENM.sourceUUID == denm.sourceUUID)
        #expect(currentDENM.timestamp == denm.timestamp)
    }

    @Test("Road alarm coordinator should send an update event when a alarm is updated")
    func road_alarm_coordinator_should_send_update_event_when_alarm_updated() async throws {
        // Given
        let roadAlarmCoordinator = RoadAlarmCoordinator()
        let observer = MockRoadAlarmCoordinatorObserver()
        await roadAlarmCoordinator.setObserver(observer)

        // When
        let denm = buildDENM()
        let denmData = try JSONEncoder().encode(denm)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmData)
        let updatedLatitude = 43.5657460
        let updatedLongitude = 43.5657460
        let denmUpdate = buildDENM(latitude: updatedLatitude, longitude: updatedLongitude)
        let denmUpdateData = try JSONEncoder().encode(denmUpdate)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmUpdateData)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didUpdateCallsCount == 1)
        #expect(await observer.didReceiveDENMCount == 2)

        let currentCreatedRoadAlarm = try #require(await observer.currentCreatedRoadAlarm)
        let currentUpdatedRoadAlarm = try #require(await observer.currentUpdatedRoadAlarm)
        #expect(currentUpdatedRoadAlarm.id == currentCreatedRoadAlarm.id)
        #expect(currentUpdatedRoadAlarm.latitude == updatedLatitude)
        #expect(currentUpdatedRoadAlarm.longitude == updatedLongitude)
    }

    @Test("Road alarm coordinator should send a remove event when a alarm is terminated")
    func road_alarm_coordinator_should_send_remove_event_when_alarm_terminated() async throws {
        // Given
        let roadAlarmCoordinator = RoadAlarmCoordinator()
        let observer = MockRoadAlarmCoordinatorObserver()
        await roadAlarmCoordinator.setObserver(observer)

        // When
        let denm = buildDENM()
        let denmData = try JSONEncoder().encode(denm)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmData)
        let updatedLatitude = 43.5657460
        let updatedLongitude = 43.5657460
        let denmUpdate = buildDENM(latitude: updatedLatitude,
                                   longitude: updatedLongitude,
                                   termination: .isCancellation)
        let denmUpdateData = try JSONEncoder().encode(denmUpdate)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmUpdateData)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didDeleteCallsCount == 1)
        #expect(await observer.didReceiveDENMCount == 2)

        let currentCreatedRoadAlarm = try #require(await observer.currentCreatedRoadAlarm)
        let currentRemovedRoadAlarm = try #require(await observer.currentDeletedRoadAlarm)
        #expect(currentRemovedRoadAlarm.id == currentCreatedRoadAlarm.id)
    }

    @Test("Road alarm coordinator should send a remove event when a alarm is expired")
    func road_alarm_coordinator_should_send_remove_event_when_alarm_expired() async throws {
        // Given
        let roadAlarmCoordinator = RoadAlarmCoordinator()
        let observer = MockRoadAlarmCoordinatorObserver()
        await roadAlarmCoordinator.setObserver(observer)

        // When
        let expirationDelay: TimeInterval = 1.0
        let denm = buildDENM(validationDuration: expirationDelay)
        let denmData = try JSONEncoder().encode(denm)
        await roadAlarmCoordinator.handleRoadAlarm(withPayload: denmData)

        try await Task.sleep(seconds: expirationDelay + 4.0)

        // Then
        #expect(await observer.didCreateCallsCount == 1)
        #expect(await observer.didReceiveDENMCount == 1)
        #expect(await observer.didDeleteCallsCount == 1)

        let expectedOriginatingStationID = denm.message.managementContainer.actionID.originatingStationID
        let currentDeletedRoadAlarm = try #require(await observer.currentDeletedRoadAlarm)
        let denmMessage = currentDeletedRoadAlarm.underlyingDENM.message
        let originatingStationID = denmMessage.managementContainer.actionID.originatingStationID
        #expect(originatingStationID == expectedOriginatingStationID)
    }

    private func buildDENM(
        latitude: Double = 43.5657439,
        longitude: Double = 1.4681494,
        validationDuration: TimeInterval? = nil,
        termination: Termination? = nil
    ) -> DENM {
        let now = Date().timeIntervalSince1970
        let actionID = ActionID(originatingStationID: 10000, sequenceNumber: 1)
        let position = Position(latitude: latitude, longitude: longitude, altitude: 120)
        let managementContainer = ManagementContainer(actionID: actionID,
                                                      detectionTime: now,
                                                      referenceTime: now,
                                                      eventPosition: position,
                                                      termination: termination,
                                                      validityDuration: validationDuration,
                                                      stationType: .passengerCar)
        let situationContainer = SituationContainer(eventType: Cause.accident())
        let demMessage = DENMMessage(stationID: 654321,
                                     managementContainer: managementContainer,
                                     situationContainer: situationContainer)
        let denm = DENM(message: demMessage,
                        sourceUUID: "v2x_87654321",
                        timestamp: now)
        return denm
    }
}
