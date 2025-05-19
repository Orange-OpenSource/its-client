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

/// The situation container.
public struct SituationContainer: Codable, Sendable {
    /// The event type.
    public let eventType: Cause
    /// The information quality.
    public let informationQuality: Int?
    /// The linked cause.
    public let linkedCause: Cause?
    private static let unavailableInformationQuality = 0
    private static let maxInformationQuality = 7

    enum CodingKeys: String, CodingKey {
        case eventType = "event_type"
        case informationQuality = "information_quality"
        case linkedCause = "linked_cause"
    }

    init(
        eventType: Cause,
        informationQuality: Int? = nil,
        linkedCause: Cause? = nil
    ) {
        self.eventType = eventType
        self.informationQuality = informationQuality.map {
            clip($0, Self.unavailableInformationQuality, Self.maxInformationQuality)
        }
        self.linkedCause = linkedCause
    }
}
