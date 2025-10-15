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

    @Test("Updating road alarm ROI should make 9 subscriptions and 0 unsubscription the first time")
    func updating_road_alarm_roi_should_make_9_subscriptions_and_0_unsubscription_first_time() async throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()

        // When
        let mockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: mockSubscriber
        )

        // Then
        #expect(await mockSubscriber.numberOfSubscriptionsCalled == 9)
        #expect(await mockSubscriber.numberOfUnsubscriptionsCalled == 0)
    }

    @Test("Updating road alarm ROI should make 9 subscriptions and 9 unsubscriptions when moving")
    func updating_road_alarm_roi_should_make_9_subscriptions_and_9_unsubscriptions_when_moving() async throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        let firstMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: firstMockSubscriber
        )

        // When
        let secondMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.64516355648167,
            longitude: 1.3844570239910097,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: secondMockSubscriber
        )

        // Then
        #expect(await firstMockSubscriber.numberOfSubscriptionsCalled == 9)
        #expect(await firstMockSubscriber.numberOfUnsubscriptionsCalled == 0)
        #expect(await secondMockSubscriber.numberOfSubscriptionsCalled == 9)
        #expect(await secondMockSubscriber.numberOfUnsubscriptionsCalled == 9)
    }

    @Test("Updating road alarm ROI should make 0 subscription when no update")
    func updating_road_alarm_roi_should_make_0_subscription_when_no_update() async throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        let firstMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: firstMockSubscriber
        )

        // When
        let secondMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: 43.63516355648101,
            longitude: 1.3744570239910001,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: secondMockSubscriber)

        // Then
        #expect(await firstMockSubscriber.numberOfSubscriptionsCalled == 9)
        #expect(await firstMockSubscriber.numberOfUnsubscriptionsCalled == 0)
        #expect(await secondMockSubscriber.numberOfSubscriptionsCalled == 0)
        #expect(await secondMockSubscriber.numberOfUnsubscriptionsCalled == 0)
    }

    @Test("Updating road user ROI should make 0 subscription when no update")
    func updating_road_position_roi_should_make_0_subscription_when_no_update() async throws {
        // Given
        let regionOfInterestCoordinator = RegionOfInterestCoordinator()
        let firstMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadUserRegionOfInterest(
            latitude: 43.63516355648167,
            longitude: 1.3744570239910097,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: firstMockSubscriber
        )

        // When
        let secondMockSubscriber = MockRegionOfInterestSubscriber()
        await regionOfInterestCoordinator.updateRoadUserRegionOfInterest(
            latitude: 43.63516355648101,
            longitude: 1.3744570239910001,
            zoomLevel: 22,
            namespace: namespace,
            subscriber: secondMockSubscriber)

        // Then
        #expect(await firstMockSubscriber.numberOfSubscriptionsCalled == 9)
        #expect(await firstMockSubscriber.numberOfUnsubscriptionsCalled == 0)
        #expect(await secondMockSubscriber.numberOfSubscriptionsCalled == 0)
        #expect(await secondMockSubscriber.numberOfUnsubscriptionsCalled == 0)
    }
}
