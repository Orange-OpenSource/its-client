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

/// The "à la carte" container.
public struct AlacarteContainer: Codable, Sendable {
    /// The lane position.
    public let lanePosition: Int8?
    /// The positioning solution.
    public let positioningSolution: PositioningSolution?

    private static let minLanePosition: Int8 = -1
    private static let maxLanePosition: Int8 = 14

    enum CodingKeys: String, CodingKey {
        case lanePosition = "lane_position"
        case positioningSolution = "positioning_solution"
    }

    init(lanePosition: Int8?, positioningSolution: PositioningSolution?) {
        self.lanePosition = lanePosition.map {
            clip($0, Self.minLanePosition, Self.maxLanePosition)
        }
        self.positioningSolution = positioningSolution
    }
}

/// The positioning solution.
public enum PositioningSolution: Int, Codable, Sendable {
    case noPositioningSolution = 0
    case sGNSS = 1
    case dGNSS = 2
    case sGNSSplusDR = 3
    case dGNSSplusDR = 4
    case dR = 5
}
