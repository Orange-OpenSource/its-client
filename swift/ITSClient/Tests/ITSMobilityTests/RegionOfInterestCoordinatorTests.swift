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

struct RegionOfInterestCoordinatorTests {
    private let namespace = "default"

    @Test("Updating road alarm ROI should return 9 subscriptions and 0 unsubscription the first time")
    func updating_road_alarm_roi_should_return_9_subscriptions_and_0_unsubscription_first_time() throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()

        // When
        let requestUpdate = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace)

        // Then
        let unwrapRequestUpdate = try #require(requestUpdate)
        #expect(unwrapRequestUpdate.subscriptions.count == 9)
        #expect(unwrapRequestUpdate.unsubscriptions.isEmpty)
    }

    @Test("Updating road alarm ROI should return 9 subscriptions and 9 unsubscriptions when moving")
    func updating_road_alarm_roi_should_return_9_subscriptions_and_9_unsubscriptions_when_moving() throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        _ = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace)

        // When
        let requestUpdate = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.64516355648167,
            longitude: 1.3844570239910097,
            zoomLevel: 22,
            namespace: namespace)

        // Then
        let unwrapRequestUpdate = try #require(requestUpdate)
        #expect(unwrapRequestUpdate.subscriptions.count == 9)
        #expect(unwrapRequestUpdate.unsubscriptions.count == 9)
    }

    @Test("Updating road alarm ROI should return nil when no update")
    func updating_road_alarm_roi_should_return_nil_when_no_update() throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        _ = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace)

        // When
        let requestUpdate = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648101,
            longitude: 1.3744570239910001,
            zoomLevel: 22,
            namespace: namespace)

        // Then
        #expect(requestUpdate == nil)
    }

    @Test("Updating road position ROI should return nil when no update")
    func updating_road_position_roi_should_return_nil_when_no_update() throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        _ = regionOfInterestCoordinator.updateRoadPositionRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace)

        // When
        let requestUpdate = regionOfInterestCoordinator.updateRoadPositionRegionOfInterest(
            latitude: 43.63516355648101,
            longitude: 1.3744570239910001,
            zoomLevel: 22,
            namespace: namespace)

        // Then
        #expect(requestUpdate == nil)
    }
}
