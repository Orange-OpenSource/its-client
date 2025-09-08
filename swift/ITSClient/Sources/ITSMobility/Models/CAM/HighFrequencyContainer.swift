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

/// The high frequency container.
public struct HighFrequencyContainer: Codable, Sendable {
    /// The heading in decidegrees.
    public let etsiHeading: Int?
    /// The speed in centimeters per second.
    public let etsiSpeed: Int?
    /// The drive direction.
    public let driveDirection: DriveDirection?
    /// The vehicule length in decimeters.
    public let etsiVehicleLength: Int?
    /// The vehicule width in decimeters.
    public let etsiVehicleWidth: Int?
    /// The curvature.
    public let curvature: Int?
    /// The curvature calculation mode.
    public let curvatureCalculationMode: CurvatureCalculationMode?
    /// The longitudinal acceleration in decimeters per second squared.
    public let etsiLongitudinalAcceleration: Int?
    /// The yaw rate in centidegrees per second.
    public let etsiYawRate: Int?
    /// The acceleration control with a bit representation in a string.
    public let etsiAccelerationControl: String?
    /// The lane position.
    public let lanePosition: LanePosition?
    /// The lateral acceleration in decimeters per second squared.
    public let etsiLateralAcceleration: Int?
    /// The vertical acceleration in decimeters per second squared.
    public let etsiVerticalAcceleration: Int?
    /// The high frequency confidence.
    public let confidence: HighFrequencyContainerConfidence?
    /// The heading in degrees.
    public var heading: Double? {
        etsiHeading.map { ETSI.deciDegreesToDegrees($0) }
    }
    /// The speed in meters per second.
    public var speed: Double? {
        etsiSpeed.map { ETSI.centimetersPerSecondToMetersPerSecond($0) }
    }
    /// The vehicule length in meters.
    public var vehicleLength: Double? {
        etsiVehicleLength.map { ETSI.decimetersToMeters($0) }
    }
    /// The vehicule width in meters.
    public var vehicleWidth: Double? {
        etsiVehicleWidth.map { ETSI.decimetersToMeters($0) }
    }
    /// The longitudinal acceleration in meters per second squared.
    public var longitudinalAcceleration: Double? {
        etsiLongitudinalAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }
    /// The yaw rate in degrees per second.
    public var yawRate: Double? {
        etsiYawRate.map { ETSI.centiDegreesPerSecondToDegreesPerSecond($0) }
    }
    /// The acceleration control.
    public var accelerationControl: AccelerationControl? {
        etsiAccelerationControl.map { AccelerationControl(rawValue: strtoul($0, nil, 2)) }
    }
    /// The lateral acceleration in meters per second squared.
    public var lateralAcceleration: Double? {
        etsiLateralAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }
    /// The vertical acceleration in meters per second squared.
    public var verticalAcceleration: Double? {
        etsiVerticalAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }

    private static let minHeading = 0
    private static let maxHeading = 3_601
    private static let minSpeed = 0
    private static let maxSpeed = 16_383
    private static let minVehiculeLength = 1
    private static let maxVehiculeLength = 1_023
    private static let minVehiculeWidth = 1
    private static let maxVehiculeWidth = 62
    private static let minCurvature = -1_023
    private static let maxCurvature = 1_023
    private static let minAcceleration = -160
    private static let maxAcceleration = 161
    private static let minYawRate = -32_766
    private static let maxYawRate = 32_766
    static let unavailableYawRate = 32_767
    static let unavailableHeading = ETSI.deciDegreesToDegrees(Self.maxHeading)
    static let unavailableSpeed = ETSI.centimetersPerSecondToMetersPerSecond(Self.maxSpeed)
    static let unavailableVehiculeLength = ETSI.decimetersToMeters(Self.maxVehiculeLength)
    static let unavailableVehiculeWidth = ETSI.decimetersToMeters(Self.maxVehiculeWidth)
    static let unavailableCurvature = 1_023
    static let unavailableAcceleration = ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond(Self.maxAcceleration)

    enum CodingKeys: String, CodingKey {
        case etsiHeading = "heading"
        case etsiSpeed = "speed"
        case driveDirection = "drive_direction"
        case etsiVehicleLength = "vehicle_length"
        case etsiVehicleWidth = "vehicle_width"
        case confidence, curvature
        case curvatureCalculationMode = "curvature_calculation_mode"
        case etsiLongitudinalAcceleration = "longitudinal_acceleration"
        case etsiYawRate = "yaw_rate"
        case etsiAccelerationControl = "acceleration_control"
        case lanePosition = "lane_position"
        case etsiLateralAcceleration = "lateral_acceleration"
        case etsiVerticalAcceleration = "vertical_acceleration"
    }

    /// Initializes a `HighFrequencyContainer`.
    /// - Parameters:
    ///   - heading: The heading in degrees.
    ///   - speed: The speed in meters per second.
    ///   - driveDirection: The drive direction.
    ///   - vehicleLength: The vehicule length in meters.
    ///   - vehicleWidth: The vehicule width in meters.
    ///   - curvature: The curvature.
    ///   - curvatureCalculationMode: The curvature calculation mode.
    ///   - longitudinalAcceleration: The longitudinal acceleration in meters per second squared.
    ///   - yawRate: The yaw rate in degrees per second.
    ///   - accelerationControl: The acceleration control.
    ///   - lanePosition: The lane position.
    ///   - lateralAcceleration: The lateral acceleration in meters per second squared.
    ///   - verticalAcceleration: The vertical acceleration in meters per second squared.
    ///   - confidence: The high frequency confidence.
    public init(
        heading: Double?,
        speed: Double?,
        driveDirection: DriveDirection? = nil,
        vehicleLength: Double? = nil,
        vehicleWidth: Double? = nil,
        curvature: Int? = nil,
        curvatureCalculationMode: CurvatureCalculationMode? = nil,
        longitudinalAcceleration: Double? = nil,
        yawRate: Double? = nil,
        accelerationControl: AccelerationControl? = nil,
        lanePosition: LanePosition? = nil,
        lateralAcceleration: Double? = nil,
        verticalAcceleration: Double? = nil,
        confidence: HighFrequencyContainerConfidence? = nil
    ) {
        self.etsiHeading = heading.map {
            clip(ETSI.degreesToDeciDegrees($0),
                 Self.minHeading,
                 Self.maxHeading)
        }
        self.etsiSpeed = speed.map {
            clip(ETSI.metersPerSecondToCentimetersPerSecond($0),
                 Self.minSpeed,
                 Self.maxSpeed)
        }
        self.driveDirection = driveDirection
        self.etsiVehicleLength = vehicleLength.map {
            clip(ETSI.metersToDecimeters($0),
                 Self.minVehiculeLength,
                 Self.maxVehiculeLength)
        }
        self.etsiVehicleWidth = vehicleWidth.map {
            clip(ETSI.metersToDecimeters($0),
                 Self.minVehiculeWidth,
                 Self.maxVehiculeWidth)
        }
        self.curvature = curvature.map { clip($0, Self.minCurvature, Self.maxCurvature) }
        self.curvatureCalculationMode = curvatureCalculationMode
        self.etsiLongitudinalAcceleration = longitudinalAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
        self.etsiYawRate = yawRate.map {
            clip(ETSI.degreesPerSecondToCentiDegreesPerSecond($0),
                 Self.minYawRate,
                 Self.maxYawRate)
        }
        self.etsiAccelerationControl = accelerationControl.map { String($0.rawValue, radix: 2) }
        self.lanePosition = lanePosition
        self.etsiLateralAcceleration = lateralAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
        self.etsiVerticalAcceleration = verticalAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
        self.confidence = confidence
    }
}

/// The drive direction.
public enum DriveDirection: Int, Codable, Sendable {
    case forward = 0
    case backward = 1
    case unavailable
}

/// The curvature calculation mode.
public enum CurvatureCalculationMode: Int, Codable, Sendable {
    case yawRateUsed = 0
    case yawRateNotUsed = 1
    case unavailable = 2
}

/// The acceleration control.
public struct AccelerationControl: OptionSet, Sendable {
    /// The raw value.
    public let rawValue: UInt

    /// Initializes a `AccelerationControl`.
    /// - Parameter rawValue: The raw value.
    public init(rawValue: UInt) {
        self.rawValue = rawValue
    }

    /// The brake pedal engaged acceleration control.
    public static let brakePedalEngaged = AccelerationControl(rawValue: 1 << 0)
    /// The gas pedal engaged acceleration control.
    public static let gasPedalEngaged = AccelerationControl(rawValue: 1 << 1)
    /// The emergency brake engaged acceleration control.
    public static let emergencyBrakeEngaged = AccelerationControl(rawValue: 1 << 2)
    /// The collision warning engaged acceleration control.
    public static let collisionWarningEngaged = AccelerationControl(rawValue: 1 << 3)
    /// The ACC engaged acceleration control.
    public static let accEngaged = AccelerationControl(rawValue: 1 << 4)
    /// The cruise control engaged acceleration control.
    public static let cruiseControlEngaged = AccelerationControl(rawValue: 1 << 5)
    /// The speed limiter engaged acceleration control.
    public static let speedLimiterEngaged = AccelerationControl(rawValue: 1 << 6)
}

/// The lane position.
public enum LanePosition: Int, Codable, Sendable {
    case offTheRoad = -1
    case innerHardShoulder = 0
    case innermostDrivingLane = 1
    case secondLaneFromInside = 2
    case outterHardShoulder = 14
}

/// The high frequency confidence.
public struct HighFrequencyContainerConfidence: Codable, Sendable {
    /// The heading in decidegrees.
    public let etsiHeading: Int?
    /// The speed in centimeters per second.
    public let etsiSpeed: Int?
    /// The vehicule length confidence.
    public let vehicleLengthConfidence: VehiculeLengthConfidence?
    /// The yaw rate confidence.
    public let yawRateConfidence: YawRateConfidence?
    /// The longitudinal acceleration in decimeters per second squared.
    public let etsiLongitudinalAcceleration: Int?
    /// The lateral acceleration in decimeters per second squared.
    public let etsiLateralAcceleration: Int?
    /// The curvature confidence.
    public let curvatureConfidence: CurvatureConfidence?
    /// The vertical acceleration in decimeters per second squared.
    public let etsiVerticalAcceleration: Int?
    /// The heading in degrees.
    public var heading: Double? {
        etsiHeading.map { ETSI.deciDegreesToDegrees($0) }
    }
    // The speed in meters per second.
    public var speed: Double? {
        etsiSpeed.map { ETSI.centimetersPerSecondToMetersPerSecond($0) }
    }
    /// The longitudinal acceleration in meters per second squared.
    public var longitudinalAcceleration: Double? {
        etsiLongitudinalAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }
    /// The lateral acceleration in meters per second squared.
    public var lateralAcceleration: Double? {
        etsiLateralAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }
    /// The vertical acceleration in meters per second squared.
    public var verticalAcceleration: Double? {
        etsiVerticalAcceleration.map { ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond($0) }
    }

    private static let minHeading = 1
    private static let maxHeading = 127
    private static let minSpeed = 1
    private static let maxSpeed = 127
    private static let minAcceleration = 0
    private static let maxAcceleration = 102
    static let unavailableHeading = ETSI.deciDegreesToDegrees(Self.maxHeading)
    static let unavailableSpeed = ETSI.centimetersPerSecondToMetersPerSecond(Self.maxSpeed)
    static let unavailableAcceleration = ETSI.decimetersPerSquaredSecondToMetersPerSquaredSecond(Self.maxAcceleration)

    enum CodingKeys: String, CodingKey {
        case etsiHeading = "heading"
        case etsiSpeed = "speed"
        case vehicleLengthConfidence = "vehicle_length"
        case yawRateConfidence = "yaw_rate"
        case etsiLongitudinalAcceleration = "longitudinal_acceleration"
        case etsiLateralAcceleration = "lateral_acceleration"
        case curvatureConfidence = "curvature"
        case etsiVerticalAcceleration = "vertical_acceleration"
    }

    /// Initializes a `HighFrequencyContainerConfidence`.
    /// - Parameters:
    ///   - heading: The heading in degrees.
    ///   - speed: The speed in meters per second.
    ///   - vehicleLengthConfidence: The vehicule length confidence.
    ///   - yawRateConfidence: The yaw rate confidence.
    ///   - curvatureConfidence: The curvature confidence.
    ///   - longitudinalAcceleration: The longitudinal acceleration in meters per second squared.
    ///   - lateralAcceleration: The lateral acceleration in meters per second squared.
    ///   - verticalAcceleration: The vertical acceleration in meters per second squared.
    public init(
        heading: Double?,
        speed: Double?,
        vehicleLengthConfidence: VehiculeLengthConfidence? = nil,
        yawRateConfidence: YawRateConfidence? = nil,
        curvatureConfidence: CurvatureConfidence? = nil,
        longitudinalAcceleration: Double? = nil,
        lateralAcceleration: Double? = nil,
        verticalAcceleration: Double? = nil
    ) {
        self.etsiHeading = heading.map {
            clip(ETSI.degreesToDeciDegrees($0),
                 Self.minHeading,
                 Self.maxHeading)
        }
        self.etsiSpeed = speed.map {
            clip(ETSI.metersPerSecondToCentimetersPerSecond($0),
                 Self.minSpeed,
                 Self.maxSpeed)
        }
        self.vehicleLengthConfidence = vehicleLengthConfidence
        self.yawRateConfidence = yawRateConfidence
        self.etsiLongitudinalAcceleration = longitudinalAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
        self.etsiLateralAcceleration = lateralAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
        self.curvatureConfidence = curvatureConfidence
        self.etsiVerticalAcceleration = verticalAcceleration.map {
            clip(ETSI.metersPerSquaredSecondToDecimetersPerSquaredSecond($0),
                 Self.minAcceleration,
                 Self.maxAcceleration)
        }
    }
}

/// The vehicule length confidence.
public enum VehiculeLengthConfidence: Int, Codable, Sendable {
    case noTrailerPresent = 0
    case trailerPresentWithKnownLength = 1
    case trailerPresentWithUnknownLength = 2
    case trailerPresenceIsUnknown = 3
    case unavailable = 4
}

/// The yaw rate confidence.
public enum YawRateConfidence: Int, Codable, Sendable {
    case degSec_000_01 = 0
    case degSec_000_05 = 1
    case degSec_000_10 = 2
    case degSec_001_00 = 3
    case degSec_005_00 = 4
    case degSec_010_00 = 5
    case degSec_100_00 = 6
    case outOfRange = 7
    case unavailable = 8

    enum CodingKeys: String, CodingKey {
        case degSec_000_01 = "degSec-000-01"
        case degSec_000_05 = "degSec-000-05"
        case degSec_000_10 = "degSec-000-10"
        case degSec_001_00 = "degSec-001-00"
        case degSec_005_00 = "degSec-005-00"
        case degSec_010_00 = "degSec-010-00"
        case degSec_100_00 = "degSec-100-00"
        case outOfRange, unavailable
    }
}

/// The curvature confidence.
public enum CurvatureConfidence: Int, Codable, Sendable {
    case onePerMeter_0_0002 = 0
    case onePerMeter_0_0001 = 1
    case onePerMeter_0_0005 = 2
    case onePerMeter_0_02 = 3
    case onePerMeter_0_01 = 4
    case onePerMeter_01 = 5
    case outOfRange = 6
    case unavailable = 7

    enum CodingKeys: String, CodingKey {
        case onePerMeter_0_0002 = "onePerMeter-0-00002"
        case onePerMeter_0_0001 = "onePerMeter-0-0001"
        case onePerMeter_0_0005 = "onePerMeter-0-0005"
        case onePerMeter_0_02 = "onePerMeter-0-002"
        case onePerMeter_0_01 = "onePerMeter-0-01"
        case onePerMeter_01 = "onePerMeter-0-1"
        case outOfRange, unavailable
    }
}
