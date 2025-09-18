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
    /// The confidence.
    public let confidence: Confidence?
    /// The reference position.
    public let referencePosition: Position
    /// The station type.
    public let stationType: StationType?

    enum CodingKeys: String, CodingKey {
        case stationType = "station_type"
        case referencePosition = "reference_position"
        case confidence
    }

    /// Initializes a `BasicContainer`.
    /// - Parameters:
    ///   - stationType: The station type.
    ///   - referencePosition: The reference position.
    ///   - confidence: The confidence.
    public init(
        stationType: StationType? = .unknown,
        referencePosition: Position,
        confidence: Confidence? = nil
    ) {
        self.stationType = stationType
        self.referencePosition = referencePosition
        self.confidence = confidence
    }
}
