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

/// The basic container.
public struct BasicContainer: Codable, Sendable {
    public let confidence: Confidence?
    public let referencePosition: Position
    public let stationType: StationType?

    enum CodingKeys: String, CodingKey {
        case stationType = "station_type"
        case referencePosition = "reference_position"
        case confidence
    }

    init(
        stationType: StationType? = .unknown,
        referencePosition: Position,
        confidence: Confidence? = nil
    ) {
        self.stationType = stationType
        self.referencePosition = referencePosition
        self.confidence = confidence
    }
}
