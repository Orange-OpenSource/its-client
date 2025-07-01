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

final class RegionOfInterestCoordinator {
    struct TopicUpdateRequest {
        let subscriptions: [String]
        let unsubscriptions: [String]
    }

    private let quadkeyBuilder = QuadkeyBuilder()
    private var currentDENMRegionOfInterest: RegionOfInterest?
    private var currentCAMRegionOfInterest: RegionOfInterest?

    func updateRoadAlarmRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String
    ) -> TopicUpdateRequest? {
        updateRegionOfInterest(for: .denm,
                               latitude: latitude,
                               longitude: longitude,
                               zoomLevel: zoomLevel,
                               namespace: namespace,
                               currentRegionOfInterest: &currentDENMRegionOfInterest)
    }

    func updateRoadPositionRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String
    ) -> TopicUpdateRequest? {
        updateRegionOfInterest(for: .cam,
                               latitude: latitude,
                               longitude: longitude,
                               zoomLevel: zoomLevel,
                               namespace: namespace,
                               currentRegionOfInterest: &currentCAMRegionOfInterest)
    }

    private func updateRegionOfInterest(
        for messageType: MessageType,
        latitude: Double,
        longitude: Double,
        zoomLevel: Int,
        namespace: String,
        currentRegionOfInterest: inout RegionOfInterest?) -> TopicUpdateRequest? {
        let separator = "/"
        let quadkey = quadkeyBuilder.quadkeyFrom(latitude: latitude,
                                                 longitude: longitude,
                                                 zoomLevel: zoomLevel,
                                                 separator: separator)

        guard quadkey != currentRegionOfInterest?.quadkey else {
            return nil
        }

        let neighborQuadkeys = quadkeyBuilder.neighborQuadkeys(for: quadkey, separator: separator)
        let regionOfInterest = RegionOfInterest(quadkey: quadkey,
                                                neighborQuadkeys: neighborQuadkeys)
        let topicUpdate = updateTopicSubscriptions(newRegionOfInterest: regionOfInterest,
                                                   currentRegionOfInterest: currentRegionOfInterest,
                                                   for: messageType,
                                                   namespace: namespace)
        currentRegionOfInterest = regionOfInterest

        return topicUpdate
    }

    private func updateTopicSubscriptions(
        newRegionOfInterest: RegionOfInterest,
        currentRegionOfInterest: RegionOfInterest?,
        for messageType: MessageType,
        namespace: String
    ) -> TopicUpdateRequest {
        var subscriptions = [String]()
        var unsubscriptions = [String]()

        let newQuadkeys = newRegionOfInterest.allQuadkeys
        let currentQuadkeys = currentRegionOfInterest?.allQuadkeys ?? []

        newQuadkeys.forEach { newQuadkey in
            if !currentQuadkeys.contains(newQuadkey) {
                subscriptions.append(topic(for: messageType, in: newQuadkey, namespace: namespace))
            }
        }

        currentQuadkeys.forEach { currentQuadkey in
            if !newQuadkeys.contains(currentQuadkey) {
                unsubscriptions.append(topic(for: messageType, in: currentQuadkey, namespace: namespace))
            }
        }

        return TopicUpdateRequest(subscriptions: subscriptions, unsubscriptions: unsubscriptions)
    }

    private func topic(for messageType: MessageType, in quadkey: String, namespace: String) -> String {
        "\(namespace)/outQueue/v2x/\(messageType)/+/\(quadkey)/#"
    }
}
