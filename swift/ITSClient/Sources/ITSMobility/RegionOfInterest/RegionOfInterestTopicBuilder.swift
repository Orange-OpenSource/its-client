//
// Software Name: SWRMobility
// SPDX-FileCopyrightText: Copyright (c) Orange SA
//
// This software is confidential and proprietary information of Orange SA.
// You shall not disclose such Confidential Information and shall not copy,
// use or distribute it in whole or in part without the prior written
// consent of Orange SA.
//
// Software description: SWRMobility is a V2X collision prevention solution.

enum RegionOfInterestTopicBuilder {
    static func buildTopicSubscriptions(
        newRegionOfInterest: RegionOfInterest,
        currentRegionOfInterest: RegionOfInterest?,
        for messageType: MessageType,
        namespace: String
    ) -> TopicUpdateRequest {
        var subscriptions = [TopicUpdateItem]()
        var unsubscriptions = [TopicUpdateItem]()

        let newQuadkeys = newRegionOfInterest.quadkeys
        let currentQuadkeys = currentRegionOfInterest?.quadkeys ?? []

        newQuadkeys.forEach { newQuadkey in
            if !currentQuadkeys.contains(newQuadkey) {
                let updateItem = TopicUpdateItem(topic: topic(for: messageType, in: newQuadkey, namespace: namespace),
                                                 quadkey: newQuadkey)
                subscriptions.append(updateItem)
            }
        }

        currentQuadkeys.forEach { currentQuadkey in
            if !newQuadkeys.contains(currentQuadkey) {
                let topic = topic(for: messageType, in: currentQuadkey, namespace: namespace)
                let updateItem = TopicUpdateItem(topic: topic,
                                                 quadkey: currentQuadkey)
                unsubscriptions.append(updateItem)
            }
        }

        return TopicUpdateRequest(subscriptions: subscriptions, unsubscriptions: unsubscriptions)
    }

    private static func topic(for messageType: MessageType, in quadkey: String, namespace: String) -> String {
        "\(namespace)/outQueue/v2x/\(messageType)/+/\(quadkey)/#"
    }
}
