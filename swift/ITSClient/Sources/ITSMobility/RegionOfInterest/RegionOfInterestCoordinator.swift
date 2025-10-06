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

actor RegionOfInterestCoordinator {
    private let quadkeyBuilder = QuadkeyBuilder()
    private var currentDENMRegionOfInterest: RegionOfInterest?
    private var currentCAMRegionOfInterest: RegionOfInterest?

    func updateRoadAlarmRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String,
        subscriber: RegionOfInterestSubscriber
    ) async {
        let regionOfInterest = await updateRegionOfInterest(for: .denm,
                                                            latitude: latitude,
                                                            longitude: longitude,
                                                            zoomLevel: zoomLevel,
                                                            namespace: namespace,
                                                            currentRegionOfInterest: currentDENMRegionOfInterest,
                                                            subscriber: subscriber)
        if currentDENMRegionOfInterest != regionOfInterest {
            currentDENMRegionOfInterest = regionOfInterest
        }
    }

    func updateRoadUserRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String,
        subscriber: RegionOfInterestSubscriber
    ) async {
        let regionOfInterest = await updateRegionOfInterest(for: .cam,
                                                            latitude: latitude,
                                                            longitude: longitude,
                                                            zoomLevel: zoomLevel,
                                                            namespace: namespace,
                                                            currentRegionOfInterest: currentCAMRegionOfInterest,
                                                            subscriber: subscriber)
        if currentCAMRegionOfInterest != regionOfInterest {
            currentCAMRegionOfInterest = regionOfInterest
        }
    }

    func reset() {
        currentDENMRegionOfInterest = nil
        currentCAMRegionOfInterest = nil
    }

    private func updateRegionOfInterest(
        for messageType: MessageType,
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String,
        currentRegionOfInterest: RegionOfInterest?,
        subscriber: RegionOfInterestSubscriber) async -> RegionOfInterest
    {
        let separator = "/"
        let quadkey = quadkeyBuilder.quadkeyFrom(latitude: latitude,
                                                 longitude: longitude,
                                                 zoomLevel: zoomLevel,
                                                 separator: separator)

        let neighborQuadkeys = quadkeyBuilder.neighborQuadkeys(for: quadkey, separator: separator)
        var regionOfInterest = RegionOfInterest(quadkeys: [quadkey] + neighborQuadkeys)
        let topicUpdate = RegionOfInterestTopicBuilder.buildTopicSubscriptions(
            newRegionOfInterest: regionOfInterest,
            currentRegionOfInterest: currentRegionOfInterest,
            for: messageType,
            namespace: namespace
        )

        for updateItem in topicUpdate.subscriptions where await !subscriber.subscribe(topic: updateItem.topic) {
            regionOfInterest.removeQuadkey(updateItem.quadkey)
        }

        for updateItem in topicUpdate.unsubscriptions where await !subscriber.unsubscribe(topic: updateItem.topic) {
            regionOfInterest.addQuadkey(updateItem.quadkey)
        }

        return regionOfInterest
    }
}
