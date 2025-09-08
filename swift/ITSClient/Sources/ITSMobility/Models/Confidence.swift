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

/// The confidence.
public struct Confidence: Codable, Sendable {
    /// The altitude confidence.
    public let altitude: AltitudeConfidence?
    /// The position confidence ellipse.
    public let positionConfidenceEllipse: PositionConfidenceEllipse?

    enum CodingKeys: String, CodingKey {
        case altitude
        case positionConfidenceEllipse = "position_confidence_ellipse"
    }

    /// Initializes a `Confidence`.
    /// - Parameters:
    ///   - altitude: The altitude confidence.
    ///   - positionConfidenceEllipse: The position confidence ellipse.
    public init(altitude: AltitudeConfidence? = nil, positionConfidenceEllipse: PositionConfidenceEllipse?) {
        self.altitude = altitude
        self.positionConfidenceEllipse = positionConfidenceEllipse
    }
}

/// The altitude confidence.
public enum AltitudeConfidence: Int, Codable, Sendable {
    case alt_000_01 = 0
    case alt_000_02 = 1
    case alt_000_05 = 2
    case alt_000_10 = 3
    case alt_000_20 = 4
    case alt_000_50 = 5
    case alt_001_00 = 6
    case alt_002_00 = 7
    case alt_005_00 = 8
    case alt_010_00 = 9
    case alt_020_00 = 10
    case alt_050_00 = 11
    case alt_100_00 = 12
    case alt_200_00 = 13
    case outOfRange = 14
    case unavailable = 15

    enum CodingKeys: String, CodingKey {
        case alt_000_01 = "alt-000-01"
        case alt_000_02 = "alt-000-02"
        case alt_000_05 = "alt-000-05"
        case alt_000_10 = "alt-000-10"
        case alt_000_20 = "alt-000-20"
        case alt_000_50 = "alt-000-50"
        case alt_001_00 = "alt-001-00"
        case alt_002_00 = "alt-002-00"
        case alt_005_00 = "alt-005-00"
        case alt_010_00 = "alt-010-00"
        case alt_020_00 = "alt-020-00"
        case alt_050_00 = "alt-050-00"
        case alt_100_00 = "alt-100-00"
        case outOfRange, unavailable
    }
}

/// The position confidence ellipse.
public struct PositionConfidenceEllipse: Codable, Sendable {
    /// The semi major confidence in centimeters.
    public let etsiSemiMajorConfidence: Int?
    /// The semi minor confidence in centimeters.
    public let etsiSemiMinorConfidence: Int?
    /// The semi major orientation in decidegrees.
    public let etsiSemiMajorOrientation: Int?
    /// The semi major confidence in meters.
    public var semiMajorConfidence: Double? {
        etsiSemiMajorConfidence.map { ETSI.centimetersToMeters($0) }
    }
    /// The semi minor confidence in meters.
    public var semiMinorConfidence: Double? {
        etsiSemiMinorConfidence.map { ETSI.centimetersToMeters($0) }
    }
    /// The semi major confidence in degrees.
    public var semiMajorOrientation: Double? {
        etsiSemiMajorOrientation.map { ETSI.deciDegreesToDegrees($0) }
    }

    private static let minConfidence = 0
    private static let minOrientation = 0
    private static let maxConfidence = 4_095
    private static let maxOrientation = 3_601
    /// The unavailable confidence value.
    public static let unavailableConfidence = ETSI.centimetersToMeters(Self.maxConfidence)
    /// The unavailable orientation value.
    public static let unavailableOrientation = ETSI.deciDegreesToDegrees(Self.maxOrientation)

    enum CodingKeys: String, CodingKey {
        case etsiSemiMajorConfidence = "semi_major_confidence"
        case etsiSemiMinorConfidence = "semi_minor_confidence"
        case etsiSemiMajorOrientation = "semi_major_orientation"
    }

    /// Initializes a `PositionConfidenceEllipse`.
    /// - Parameters:
    ///   - semiMajorConfidence: The semi major confidence in meters.
    ///   - semiMinorConfidence: The semi minor confidence in meters.
    ///   - semiMajorOrientation: The semi major orientation in degrees.
    public init(
        semiMajorConfidence: Double? = Self.unavailableConfidence,
        semiMinorConfidence: Double? = Self.unavailableConfidence,
        semiMajorOrientation: Double? = Self.unavailableOrientation
    ) {
        self.etsiSemiMajorConfidence = semiMajorConfidence.map {
            clip(ETSI.metersToCentimeters($0), Self.minConfidence, Self.maxConfidence)
        }
        self.etsiSemiMinorConfidence = semiMinorConfidence.map {
            clip(ETSI.metersToCentimeters($0), Self.minConfidence, Self.maxConfidence)
        }
        self.etsiSemiMajorOrientation = semiMajorOrientation.map {
            clip(ETSI.degreesToDeciDegrees($0), Self.minOrientation, Self.maxOrientation)
        }
    }
}
