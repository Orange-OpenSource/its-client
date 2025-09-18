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

/// The path history.
public struct PathHistory: Codable, Sendable {
    /// The offset position of a detected event point with regards to the previous event point.
    public let pathPosition: PathPosition
    /// The time travelled by the detecting ITS-S since the previous detected event point in centiseconds.
    public let etsiPathDeltaTime: Int16?
    /// The time travelled by the detecting ITS-S since the previous detected event point in seconds.
    public var pathDeltaTime: TimeInterval? {
        etsiPathDeltaTime.map { ETSI.centiSecondsToSeconds(Int($0)) }
    }

    enum CodingKeys: String, CodingKey {
        case etsiPathDeltaTime = "path_delta_time"
        case pathPosition = "path_position"
    }

    /// Initializes a `PathHistory`.
    /// - Parameters:
    ///   - pathPosition: The offset position of a detected event point with regards to the previous  event point.
    ///   - pathDeltaTime: The time travelled by the detecting ITS-S since the previous detected event point in seconds.
    public init(pathPosition: PathPosition, pathDeltaTime: TimeInterval?) {
        self.pathPosition = pathPosition
        self.etsiPathDeltaTime = pathDeltaTime.map { Int16(ETSI.secondsToCentiSeconds($0)) }
    }
}

/// The path position.
public struct PathPosition: Codable, Sendable {
    /// The delta latitude in decimicrodegrees.
    public let etsiDeltaLatitude: Int?
    /// The delta longitude in decimicrodegrees.
    public let etsiDeltaLongitude: Int?
    /// The delta altitude in centimeters.
    public let etsiDeltaAltitude: Int?
    /// The delta latitiude in degrees.
    public var deltaLatitude: Double? {
        etsiDeltaLatitude.map { ETSI.deciMicroDegreesToDegrees($0) }
    }
    /// The delta longitude in degrees.
    public var deltaLongitude: Double? {
        etsiDeltaLongitude.map { ETSI.deciMicroDegreesToDegrees($0) }
    }
    /// The delta altitude in meters.
    public var deltaAltitude: Double? {
        etsiDeltaAltitude.map { ETSI.centimetersToMeters($0) }
    }

    private static let minDeltaPosition = -131_071
    private static let maxDeltaPosition = 131_072
    private static let minDeltaAltitude = -12_700
    private static let maxDeltaAltitude = 12_800
    static let deltaPositionUnavailable = ETSI.deciMicroDegreesToDegrees(Self.maxDeltaPosition)
    static let unavailableDeltaAltitude = ETSI.centimetersToMeters(Self.maxDeltaAltitude)

    public enum CodingKeys: String, CodingKey {
        case etsiDeltaAltitude = "delta_altitude"
        case etsiDeltaLatitude = "delta_latitude"
        case etsiDeltaLongitude = "delta_longitude"
    }

    /// Initializes a `PathPosition`.
    /// - Parameters:
    ///   - deltaLatitude: The delta latitude in degrees.
    ///   - deltaLongitude: The delta longitude in degrees.
    ///   - deltaAltitude: The delta altitude in meters.
    public init(deltaLatitude: Double?, deltaLongitude: Double?, deltaAltitude: Double?) {
        self.etsiDeltaLatitude = deltaLatitude.map {
            clip(ETSI.degreesToDeciMicroDegrees($0),
                 Self.minDeltaPosition,
                 Self.maxDeltaPosition)
        }
        self.etsiDeltaLongitude = deltaLongitude.map {
            clip(ETSI.degreesToDeciMicroDegrees($0),
                 Self.minDeltaPosition,
                 Self.maxDeltaPosition)
        }
        self.etsiDeltaAltitude = deltaAltitude.map {
            clip(ETSI.metersToCentimeters($0),
                 Self.minDeltaAltitude,
                 Self.maxDeltaAltitude)
        }
    }
}
