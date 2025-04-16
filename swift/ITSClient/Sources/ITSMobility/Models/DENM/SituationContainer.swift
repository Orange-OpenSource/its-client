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
public struct SituationContainer: Codable {
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
        self.informationQuality = informationQuality.map({
            clip($0, Self.unavailableInformationQuality, Self.maxInformationQuality)
        })
        self.linkedCause = linkedCause
    }
}

/// The cause.
public struct Cause: Codable {
    /// The cause.
    public let cause: CauseType
    /// The subcause
    public let subcause: UInt8?

    public init(cause: CauseType, subcause: UInt8? = nil) {
        self.cause = cause
        self.subcause = subcause
    }
}

/// The cause type
public enum CauseType: Int8, Codable, Sendable {
    case reserved = 0
    case trafficCondition = 1
    case accident = 2
    case roadworks = 3
    case adverseWeatherConditionAdhesion = 6
    case hazardousLocationSurfaceCondition = 9
    case hazardousLocationObstacleOnTheRoad = 10
    case hazardousLocationAnimalOnTheRoad = 11
    case humanPresenceOnTheRoad = 12
    case wrongWayDriving = 14
    case rescueAndRecoveryWorkInProgress = 15
    case adverseWeatherConditionExtremeWeatherCondition = 17
    case adverseWeatherConditionVisibility = 18
    case adverseWeatherConditionPrecipitation = 19
    case slowVehicle = 26
    case dangerousEndOfQueue = 27
    case vehicleBreakdown = 91
    case postCrash = 92
    case humanProblem = 93
    case stationaryVehicle = 94
    case emergencyVehicleApproaching = 95
    case hazardousLocationDangerousCurve = 96
    case collisionRisk = 97
    case signalViolation = 98
    case dangerousSituation = 99
}


