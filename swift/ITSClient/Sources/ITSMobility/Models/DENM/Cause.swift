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

// swiftlint:disable file_length
/// The cause.
public enum Cause: Codable, Sendable, Equatable {
    /// The reserved cause.
    case reserved
    /// The traffic condition cause with an optional subcause.
    case trafficCondition(subcause: TrafficConditionSubcause? = nil)
    /// The accident cause with an optional subcause.
    case accident(subcause: AccidentSubcause? = nil)
    /// The roadworks cause with an optional subcause.
    case roadworks(subcause: RoadworksSubcause? = nil)
    /// The adverse weather condition adhesion cause with an optional subcause.
    case adverseWeatherConditionAdhesion(subcause: AdverseWeatherConditionAdhesionSubcause? = nil)
    /// The hazardous location surface condition cause with an optional subcause.
    case hazardousLocationSurfaceCondition(subcause: HazardousLocationSurfaceConditionSubcause? = nil)
    /// The hazardous location obstacle on the road cause with an optional subcause.
    case hazardousLocationObstacleOnTheRoad(subcause: HazardousLocationObstacleOnTheRoadSubcause? = nil)
    /// The hazardous location animal on the road cause with an optional subcause.
    case hazardousLocationAnimalOnTheRoad(subcause: HazardousLocationAnimalsOnTheRoadSubcause? = nil)
    /// The human presence on the road cause with an optional subcause.
    case humanPresenceOnTheRoad(subcause: HumanPresenceOnTheRoadSubcause? = nil)
    /// The wrong way driving cause with an optional subcause.
    case wrongWayDriving(subcause: WrongWayDrivingSubcause? = nil)
    /// The rescue and recovery work in progress cause with an optional subcause.
    case rescueAndRecoveryWorkInProgress(subcause: RescueAndRecoveryWorkInProgressSubcause? = nil)
    /// The extreme weather condition cause with an optional subcause.
    case extremeWeatherCondition(subcause: ExtremeWeatherConditionSubcause? = nil)
    /// The adverse weather condition visibility cause with an optional subcause.
    case adverseWeatherConditionVisibility(subcause: AdverseWeatherConditionVisibilitySubcause? = nil)
    /// The adverse weather condition precipitation cause with an optional subcause.
    case adverseWeatherConditionPrecipitation(subcause: AdverseWeatherConditionPrecipitationSubcause? = nil)
    /// The slow vehicle cause with an optional subcause.
    case slowVehicle(subcause: SlowVehicleSubcause? = nil)
    /// The dangerous end of queue cause with an optional subcause.
    case dangerousEndOfQueue(subcause: DangerousEndOfQueueSubcause? = nil)
    /// The vehicle breakdown cause with an optional subcause.
    case vehicleBreakdown(subcause: VehicleBreakdownSubcause? = nil)
    /// The post crash cause with an optional subcause.
    case postCrash(subcause: PostCrashSubcause? = nil)
    /// The human problem cause with an optional subcause.
    case humanProblem(subcause: HumanProblemSubcause? = nil)
    /// The stationary vehicle cause with an optional subcause.
    case stationaryVehicle(subcause: StationaryVehicleSubcause? = nil)
    /// The emergency vehicle approaching cause with an optional subcause.
    case emergencyVehicleApproaching(subcause: EmergencyVehicleApproachingSubcause? = nil)
    /// The hazardous location dangerous curve cause with an optional subcause.
    case hazardousLocationDangerousCurve(subcause: HazardousLocationDangerousCurveSubcause? = nil)
    /// The collision risk cause with an optional subcause.
    case collisionRisk(subcause: CollisionRiskSubcause? = nil)
    /// The signal violation cause with an optional subcause.
    case signalViolation(subcause: SignalViolationSubcause? = nil)
    /// The dangerous situation cause with an optional subcause.
    case dangerousSituation(subcause: DangerousSituationSubcause? = nil)

    private static let reservedRawValue: UInt8 = 0
    private static let trafficConditionRawValue: UInt8 = 1
    private static let accidentRawValue: UInt8 = 2
    private static let roadworksRawValue: UInt8 = 3
    private static let adverseWeatherConditionAdhesionRawValue: UInt8 = 6
    private static let hazardousLocationSurfaceConditionRawValue: UInt8 = 9
    private static let hazardousLocationObstacleOnTheRoadRawValue: UInt8 = 10
    private static let hazardousLocationAnimalOnTheRoadRawValue: UInt8 = 11
    private static let humanPresenceOnTheRoadRawValue: UInt8 = 12
    private static let wrongWayDrivingRawValue: UInt8 = 14
    private static let rescueAndRecoveryWorkInProgressRawValue: UInt8 = 15
    private static let extremeWeatherConditionRawValue: UInt8 = 17
    private static let adverseWeatherConditionVisibilityRawValue: UInt8 = 18
    private static let adverseWeatherConditionPrecipitationRawValue: UInt8 = 19
    private static let slowVehicleRawValue: UInt8 = 26
    private static let dangerousEndOfQueueRawValue: UInt8 = 27
    private static let vehicleBreakdownRawValue: UInt8 = 91
    private static let postCrashRawValue: UInt8 = 92
    private static let humanProblemRawValue: UInt8 = 93
    private static let stationaryVehicleRawValue: UInt8 = 94
    private static let emergencyVehicleApproachingRawValue: UInt8 = 95
    private static let hazardousLocationDangerousCurveRawValue: UInt8 = 96
    private static let collisionRiskRawValue: UInt8 = 97
    private static let signalViolationRawValue: UInt8 = 98
    private static let dangerousSituationRawValue: UInt8 = 99

    enum CodingKeys: String, CodingKey {
        case cause
        case subcause
    }

    // swiftlint:disable function_body_length
    // swiftlint:disable line_length
    /// Initializes a new instance by decoding from the given decoder.
    /// - Parameter decoder: The decoder to read data from.
    public init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let cause = try container.decode(UInt8.self, forKey: .cause)

        switch cause {
        case Self.reservedRawValue:
            self = .reserved
        case Self.trafficConditionRawValue:
            let subcause = Self.decodeSubcause(TrafficConditionSubcause.self, in: container, fallbackValue: .unknown)
            self = .trafficCondition(subcause: subcause)
        case Self.accidentRawValue:
            let subcause = Self.decodeSubcause(AccidentSubcause.self, in: container, fallbackValue: .unknown)
            self = .accident(subcause: subcause)
        case Self.roadworksRawValue:
            let subcause = Self.decodeSubcause(RoadworksSubcause.self, in: container, fallbackValue: .unknown)
            self = .roadworks(subcause: subcause)
        case Self.adverseWeatherConditionAdhesionRawValue:
            let subcause = Self.decodeSubcause(AdverseWeatherConditionAdhesionSubcause.self, in: container, fallbackValue: .unknown)
            self = .adverseWeatherConditionAdhesion(subcause: subcause)
        case Self.hazardousLocationSurfaceConditionRawValue:
            let subcause = Self.decodeSubcause(HazardousLocationSurfaceConditionSubcause.self, in: container, fallbackValue: .unknown)
            self = .hazardousLocationSurfaceCondition(subcause: subcause)
        case Self.hazardousLocationObstacleOnTheRoadRawValue:
            let subcause = Self.decodeSubcause(HazardousLocationObstacleOnTheRoadSubcause.self, in: container, fallbackValue: .unknown)
            self = .hazardousLocationObstacleOnTheRoad(subcause: subcause)
        case Self.hazardousLocationAnimalOnTheRoadRawValue:
            let subcause = Self.decodeSubcause(HazardousLocationAnimalsOnTheRoadSubcause.self, in: container, fallbackValue: .unknown)
            self = .hazardousLocationAnimalOnTheRoad(subcause: subcause)
        case Self.humanPresenceOnTheRoadRawValue:
            let subcause = Self.decodeSubcause(HumanPresenceOnTheRoadSubcause.self, in: container, fallbackValue: .unknown)
            self = .humanPresenceOnTheRoad(subcause: subcause)
        case Self.wrongWayDrivingRawValue:
            let subcause = Self.decodeSubcause(WrongWayDrivingSubcause.self, in: container, fallbackValue: .unknown)
            self = .wrongWayDriving(subcause: subcause)
        case Self.rescueAndRecoveryWorkInProgressRawValue:
            let subcause = Self.decodeSubcause(RescueAndRecoveryWorkInProgressSubcause.self, in: container, fallbackValue: .unknown)
            self = .rescueAndRecoveryWorkInProgress(subcause: subcause)
        case Self.extremeWeatherConditionRawValue:
            let subcause = Self.decodeSubcause(ExtremeWeatherConditionSubcause.self, in: container, fallbackValue: .unknown)
            self = .extremeWeatherCondition(subcause: subcause)
        case Self.adverseWeatherConditionVisibilityRawValue:
            let subcause = Self.decodeSubcause(AdverseWeatherConditionVisibilitySubcause.self, in: container, fallbackValue: .unknown)
            self = .adverseWeatherConditionVisibility(subcause: subcause)
        case Self.adverseWeatherConditionPrecipitationRawValue:
            let subcause = Self.decodeSubcause(AdverseWeatherConditionPrecipitationSubcause.self, in: container, fallbackValue: .unknown)
            self = .adverseWeatherConditionPrecipitation(subcause: subcause)
        case Self.slowVehicleRawValue:
            let subcause = Self.decodeSubcause(SlowVehicleSubcause.self, in: container, fallbackValue: .unknown)
            self = .slowVehicle(subcause: subcause)
        case Self.dangerousEndOfQueueRawValue:
            let subcause = Self.decodeSubcause(DangerousEndOfQueueSubcause.self, in: container, fallbackValue: .unknown)
            self = .dangerousEndOfQueue(subcause: subcause)
        case Self.vehicleBreakdownRawValue:
            let subcause = Self.decodeSubcause(VehicleBreakdownSubcause.self, in: container, fallbackValue: .unknown)
            self = .vehicleBreakdown(subcause: subcause)
        case Self.humanProblemRawValue:
            let subcause = Self.decodeSubcause(HumanProblemSubcause.self, in: container, fallbackValue: .unknown)
            self = .humanProblem(subcause: subcause)
        case Self.stationaryVehicleRawValue:
            let subcause = Self.decodeSubcause(StationaryVehicleSubcause.self, in: container, fallbackValue: .unknown)
            self = .stationaryVehicle(subcause: subcause)
        case Self.emergencyVehicleApproachingRawValue:
            let subcause = Self.decodeSubcause(EmergencyVehicleApproachingSubcause.self, in: container, fallbackValue: .unknown)
            self = .emergencyVehicleApproaching(subcause: subcause)
        case Self.hazardousLocationDangerousCurveRawValue:
            let subcause = Self.decodeSubcause(HazardousLocationDangerousCurveSubcause.self, in: container, fallbackValue: .unknown)
            self = .hazardousLocationDangerousCurve(subcause: subcause)
        case Self.collisionRiskRawValue:
            let subcause = Self.decodeSubcause(CollisionRiskSubcause.self, in: container, fallbackValue: .unknown)
            self = .collisionRisk(subcause: subcause)
        case Self.signalViolationRawValue:
            let subcause = Self.decodeSubcause(SignalViolationSubcause.self, in: container, fallbackValue: .unknown)
            self = .signalViolation(subcause: subcause)
        case Self.dangerousSituationRawValue:
            let subcause = Self.decodeSubcause(DangerousSituationSubcause.self, in: container, fallbackValue: .unknown)
            self = .dangerousSituation(subcause: subcause)
        default:
            throw DecodingError.dataCorruptedError(forKey: .cause, in: container, debugDescription: "Invalid cause")
        }
    }

    /// Encodes this value into the given encoder.
    /// - Parameter encoder: The encoder to write data to.
    public func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        switch self {
        case .reserved:
            try container.encode(Self.reservedRawValue, forKey: .cause)
        case .trafficCondition(let trafficConditionSubcause):
            try container.encode(Self.trafficConditionRawValue, forKey: .cause)
            try container.encode(trafficConditionSubcause, forKey: .subcause)
        case .accident(let accidentSubcause):
            try container.encode(Self.accidentRawValue, forKey: .cause)
            try container.encode(accidentSubcause, forKey: .subcause)
        case .roadworks(let roadworksSubcause):
            try container.encode(Self.roadworksRawValue, forKey: .cause)
            try container.encode(roadworksSubcause, forKey: .subcause)
        case .adverseWeatherConditionAdhesion(let adverseWeatherConditionAdhesionSubcause):
            try container.encode(Self.adverseWeatherConditionAdhesionRawValue, forKey: .cause)
            try container.encode(adverseWeatherConditionAdhesionSubcause, forKey: .subcause)
        case .hazardousLocationSurfaceCondition(let hazardousLocationSurfaceConditionSubcause):
            try container.encode(Self.hazardousLocationSurfaceConditionRawValue, forKey: .cause)
            try container.encode(hazardousLocationSurfaceConditionSubcause, forKey: .subcause)
        case .hazardousLocationObstacleOnTheRoad(let hazardousLocationObstacleOnTheRoadSubcause):
            try container.encode(Self.hazardousLocationObstacleOnTheRoadRawValue, forKey: .cause)
            try container.encode(hazardousLocationObstacleOnTheRoadSubcause, forKey: .subcause)
        case .hazardousLocationAnimalOnTheRoad(let hazardousLocationAnimalOnTheRoadSubcause):
            try container.encode(Self.hazardousLocationAnimalOnTheRoadRawValue, forKey: .cause)
            try container.encode(hazardousLocationAnimalOnTheRoadSubcause, forKey: .subcause)
        case .humanPresenceOnTheRoad(let humanPresenceOnTheRoadSubcause):
            try container.encode(Self.humanPresenceOnTheRoadRawValue, forKey: .cause)
            try container.encode(humanPresenceOnTheRoadSubcause, forKey: .subcause)
        case .wrongWayDriving(let wrongWayDrivingSubcause):
            try container.encode(Self.wrongWayDrivingRawValue, forKey: .cause)
            try container.encode(wrongWayDrivingSubcause, forKey: .subcause)
        case .rescueAndRecoveryWorkInProgress(let rescueAndRecoveryWorkInProgressSubcause):
            try container.encode(Self.rescueAndRecoveryWorkInProgressRawValue, forKey: .cause)
            try container.encode(rescueAndRecoveryWorkInProgressSubcause, forKey: .subcause)
        case .extremeWeatherCondition(let extremeWeatherConditionSubcause):
            try container.encode(Self.extremeWeatherConditionRawValue, forKey: .cause)
            try container.encode(extremeWeatherConditionSubcause, forKey: .subcause)
        case .adverseWeatherConditionVisibility(let adverseWeatherConditionVisibilitySubcause):
            try container.encode(Self.adverseWeatherConditionVisibilityRawValue, forKey: .cause)
            try container.encode(adverseWeatherConditionVisibilitySubcause, forKey: .subcause)
        case .adverseWeatherConditionPrecipitation(let adverseWeatherConditionPrecipitationSubcause):
            try container.encode(Self.adverseWeatherConditionPrecipitationRawValue, forKey: .cause)
            try container.encode(adverseWeatherConditionPrecipitationSubcause, forKey: .subcause)
        case .slowVehicle(let slowVehicleSubcause):
            try container.encode(Self.slowVehicleRawValue, forKey: .cause)
            try container.encode(slowVehicleSubcause, forKey: .subcause)
        case .dangerousEndOfQueue(let dangerousEndOfQueueSubcause):
            try container.encode(Self.dangerousEndOfQueueRawValue, forKey: .cause)
            try container.encode(dangerousEndOfQueueSubcause, forKey: .subcause)
        case .vehicleBreakdown(let vehicleBreakdownSubcause):
            try container.encode(Self.vehicleBreakdownRawValue, forKey: .cause)
            try container.encode(vehicleBreakdownSubcause, forKey: .subcause)
        case .postCrash(let postCrashSubcause):
            try container.encode(Self.postCrashRawValue, forKey: .cause)
            try container.encode(postCrashSubcause, forKey: .subcause)
        case .humanProblem(let humanProblemSubcause):
            try container.encode(Self.humanProblemRawValue, forKey: .cause)
            try container.encode(humanProblemSubcause, forKey: .subcause)
        case .stationaryVehicle(let stationaryVehicleSubcause):
            try container.encode(Self.stationaryVehicleRawValue, forKey: .cause)
            try container.encode(stationaryVehicleSubcause, forKey: .subcause)
        case .emergencyVehicleApproaching(let emergencyVehicleApproachingSubcause):
            try container.encode(Self.emergencyVehicleApproachingRawValue, forKey: .cause)
            try container.encode(emergencyVehicleApproachingSubcause, forKey: .subcause)
        case .hazardousLocationDangerousCurve(let hazardousLocationDangerousCurveSubcause):
            try container.encode(Self.hazardousLocationDangerousCurveRawValue, forKey: .cause)
            try container.encode(hazardousLocationDangerousCurveSubcause, forKey: .subcause)
        case .collisionRisk(let collisionRiskSubcause):
            try container.encode(Self.collisionRiskRawValue, forKey: .cause)
            try container.encode(collisionRiskSubcause, forKey: .subcause)
        case .signalViolation(let signalViolationSubcause):
            try container.encode(Self.signalViolationRawValue, forKey: .cause)
            try container.encode(signalViolationSubcause, forKey: .subcause)
        case .dangerousSituation(let dangerousSituationSubcause):
            try container.encode(Self.dangerousSituationRawValue, forKey: .cause)
            try container.encode(dangerousSituationSubcause, forKey: .subcause)
        }
    }
    // swiftlint:enable line_length
    // swiftlint:enable function_body_length

    private static func decodeSubcause<T: Decodable>(_ type: T.Type,
                                                     in container: KeyedDecodingContainer<CodingKeys>,
                                                     fallbackValue: T) -> T? {
        do {
            return try container.decodeIfPresent(type, forKey: .subcause)
        } catch {
            return fallbackValue
        }
    }
}

/// The traffic condition subcause.
public enum TrafficConditionSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The  increased volume of traffic subcause.
    case increasedVolumeOfTraffic = 1
    /// The traffic jam slowly increasing subcause.
    case trafficJamSlowlyIncreasing = 2
    /// The traffic jam increasing subcause.
    case trafficJamIncreasing = 3
    /// The traffic jam strongly increasing subcause.
    case trafficJamStronglyIncreasing = 4
    /// The traffic stationary subcause.
    case trafficStationary = 5
    /// The traffic jam slightly decreasing subcause.
    case trafficJamSlightlyDecreasing = 6
    /// The traffic jam decreasing subcause.
    case trafficJamDecreasing = 7
    /// The traffic jam strongly decreasing subcause.
    case trafficJamStronglyDecreasing = 8
}

/// The accident subcause.
public enum AccidentSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The multi vehicle accident subcause.
    case multiVehicleAccident = 1
    /// The heavy accident subcause.
    case heavyAccident = 2
    /// The accident involving lorry subcause.
    case accidentInvolvingLorry = 3
    /// The accident involving bus subcause.
    case accidentInvolvingBus = 4
    /// The accident involving hazardous materials subcause.
    case accidentInvolvingHazardousMaterials = 5
    /// The accident on opposite lane subcause.
    case accidentOnOppositeLane = 6
    /// The unsecured accident subcause.
    case unsecuredAccident = 7
    /// The assistance requested subcause.
    case assistanceRequested = 8
}

/// The roadworks subcause.
public enum RoadworksSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The major roadworks subcause.
    case majorRoadworks = 1
    /// The road marking work subcause.
    case roadMarkingWork = 2
    /// The slow moving road maintenance subcause.
    case slowMovingRoadMaintenance = 3
    /// The short term stationary roadworks subcause.
    case shortTermStationaryRoadworks = 4
    /// The street cleaning subcause.
    case streetCleaning = 5
    /// The winter service subcause.
    case winterService = 6
}

/// The adverse weather condition adhesion subcause.
public enum AdverseWeatherConditionAdhesionSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The heavy frost on road subcause.
    case heavyFrostOnRoad = 1
    /// The fuel on road subcause.
    case fuelOnRoad = 2
    /// The mud on road subcause.
    case mudOnRoad = 3
    /// The snow on road subcause.
    case snowOnRoad = 4
    /// The ice on road subcause.
    case iceOnRoad = 5
    /// The black ice on road subcause.
    case blackIceOnRoad = 6
    /// The oil on road subcause.
    case oilOnRoad = 7
    /// The loose chippings subcause.
    case looseChippings = 8
    /// The instant black ice subcause.
    case instantBlackIce = 9
    /// The road salted subcause.
    case roadsSalted = 10
}

/// The hazardous location surface condition subcause.
public enum HazardousLocationSurfaceConditionSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The rock falls subcause.
    case rockfalls = 1
    /// The earthquake damage subcause.
    case earthquakeDamage = 2
    /// The sewer collapse subcause.
    case sewerCollapse = 3
    /// The subsidence subcause.
    case subsidence = 4
    /// The snow drifts subcause.
    case snowDrifts = 5
    /// The storm damage subcause.
    case stormDamage = 6
    /// The burst pipe subcause.
    case burstPipe = 7
    /// The volcano eruption subcause.
    case volcanoEruption = 8
    /// The falling ice subcause.
    case fallingIce = 9
}

/// The hazardous location obstacle on the road subcause.
public enum HazardousLocationObstacleOnTheRoadSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The shed load subcause.
    case shedLoad = 1
    /// The parts of vehicles subcause.
    case partsOfVehicles = 2
    /// The parts of tyres subcause.
    case partsOfTyres = 3
    /// The big objects subcause.
    case bigObjects = 4
    /// The fallen trees subcause.
    case fallenTrees = 5
    /// The hub caps subcause.
    case hubCaps = 6
    /// The waiting vehicles subcause.
    case waitingVehicles = 7
}

/// The hazardous location animal on the road subcause.
public enum HazardousLocationAnimalsOnTheRoadSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The wild animals subcause.
    case wildAnimals = 1
    /// The herd of animals subcause.
    case herdOfAnimals = 2
    /// The small animals subcause.
    case smallAnimals = 3
    /// The large animals subcause.
    case largeAnimals = 4
}

/// The human presence on the road subcause.
public enum HumanPresenceOnTheRoadSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The children on roadway subcause.
    case childrenOnRoadway = 1
    /// The cyclist on roadway subcause.
    case cyclistOnRoadway = 2
    /// The motorcyclist on roadway subcause.
    case motorcyclistOnRoadway = 3
    /// The scooter on roadway subcause.
    case scooterOnRoadway = 4
}

/// The wrong way driving subcause.
public enum WrongWayDrivingSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The wrong lane subcause.
    case wrongLane = 1
    /// The wrong direction subcause.
    case wrongDirection = 2
}

/// The rescue and recovery work in progress subcause.
public enum RescueAndRecoveryWorkInProgressSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The emergency vehicles subcause.
    case emergencyVehicles = 1
    /// The rescue helicopter landing subcause.
    case rescueHelicopterLanding = 2
    /// The police activity ongoing subcause.
    case policeActivityOngoing = 3
    /// The medical emergency ongoing subcause.
    case medicalEmergencyOngoing = 4
    /// The child abduction in progress subcause.
    case childAbductionInProgress = 5
}

/// The extreme weather condition subcause.
public enum ExtremeWeatherConditionSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The strong winds subcause.
    case strongWinds = 1
    /// The damaging hail subcause.
    case damagingHail = 2
    /// The hurricane subcause.
    case hurricane = 3
    /// The thunderstorm subcause.
    case thunderstorm = 4
    /// The tornado subcause.
    case tornado = 5
    /// The blizzard subcause.
    case blizzard = 6
}

/// The adverse weather condition visibility subcause.
public enum AdverseWeatherConditionVisibilitySubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The fog subcause.
    case fog = 1
    /// The smoke subcause.
    case smoke = 2
    /// The heavy snowfall subcause.
    case heavySnowfall = 3
    /// The heavy rain subcause.
    case heavyRain = 4
    /// The heavy hail subcause.
    case heavyHail = 5
    /// The low sun glare subcause.
    case lowSunGlare = 6
    /// The sandstorms subcause.
    case sandstorms = 7
    /// The swarm of insects subcause.
    case swarmsOfInsects = 8
}

/// The adverse weather condition precipitation subcause.
public enum AdverseWeatherConditionPrecipitationSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The heavy rain precipitation subcause.
    case heavyRainPrecipitation = 1
    /// The heavy snowfall precipitation subcause.
    case heavySnowfallPrecipitation = 2
    /// The soft hail subcause.
    case softHail = 3
}

/// The slow vehicle subcause.
public enum SlowVehicleSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The maintenance vehicle subcause.
    case maintenanceVehicle = 1
    /// The vehicles slowing to look at accident subcause.
    case vehiclesSlowingToLookAtAccident = 2
    /// The abnormal load subcause.
    case abnormalLoad = 3
    /// The abnormal wide load subcause.
    case abnormalWideLoad = 4
    /// The convoy subcause.
    case convoy = 5
    /// The snowplough subcause.
    case snowplough = 6
    /// The deicing subcause.
    case deicing = 7
    /// The salting vehicles subcause.
    case saltingVehicles = 8
}

/// The dangerous end of queue subcause.
public enum DangerousEndOfQueueSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The sudden end of queue subcause.
    case suddenEndOfQueue = 1
    /// The queue over hill subcause.
    case queueOverHill = 2
    /// The queue around bend subcause.
    case queueAroundBend = 3
    /// The queue in tunnel subcause.
    case queueInTunnel = 4
}

/// The vehicle breakdown subcause.
public enum VehicleBreakdownSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The lack of fuel subcause.
    case lackOfFuel = 1
    /// The lack of battery power subcause.
    case lackOfBatteryPower = 2
    /// The engine problem subcause.
    case engineProblem = 3
    /// The transmission problem subcause.
    case transmissionProblem = 4
    /// The engine cooling problem subcause.
    case engineCoolingProblem = 5
    /// The braking system problem subcause.
    case brakingSystemProblem = 6
    /// The steering problem subcause.
    case steeringProblem = 7
    /// The tyre puncture subcause.
    case tyrePuncture = 8
}

/// The post crash subcause.
public enum PostCrashSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The accident without e call triggered subcause.
    case accidentWithoutECallTriggered = 1
    /// The accident with e call manually triggered subcause.
    case accidentWithECallManuallyTriggered = 2
    /// The accident with e call automatically triggered subcause.
    case accidentWithECallAutomaticallyTriggered = 3
    /// The accident with e call triggered without access to cellular network subcause.
    case accidentWithECallTriggeredWithoutAccessToCellularNetwork = 4
}

/// The human problem subcause.
public enum HumanProblemSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The glycemia problem subcause.
    case glycemiaProblem = 1
    /// The heart problem subcause.
    case heartProblem = 2
}

/// The stationary vehicle subcause.
public enum StationaryVehicleSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The human problem subcause.
    case humanProblem = 1
    /// The vehicle breakdown subcause.
    case vehicleBreakdown = 2
    /// The post crash subcause.
    case postCrash = 3
    /// The public transport stop subcause.
    case publicTransportStop = 4
    /// The carrying dangerous goods subcause.
    case carryingDangerousGoods = 5
}

/// The emergency vehicle approaching subcause.
public enum EmergencyVehicleApproachingSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The emergency vehicle approaching subcause.
    case emergencyVehicleApproaching = 1
    /// The prioritized vehicle approaching subcause.
    case prioritizedVehicleApproaching = 2
}

/// The hazardous location dangerous curve subcause.
public enum HazardousLocationDangerousCurveSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The dangerous left turn curve subcause.
    case dangerousLeftTurnCurve = 1
    /// The dangerous right turn curve subcause.
    case dangerousRightTurnCurve = 2
    /// The multiple curves starting with unknown turning direction subcause.
    case multipleCurvesStartingWithUnknownTurningDirection = 3
    /// The multiple curves starting with left turn subcause.
    case multipleCurvesStartingWithLeftTurn = 4
    /// The multiple curves starting with right turn subcause.
    case multipleCurvesStartingWithRightTurn = 5
}

/// The collision risk subcause.
public enum CollisionRiskSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The longitudinal collision risk subcause.
    case longitudinalCollisionRisk = 1
    /// The crossing collision risk subcause.
    case crossingCollisionRisk = 2
    /// The lateral collision risk subcause.
    case lateralCollisionRisk = 3
    /// The vulnerable road user subcause.
    case vulnerableRoadUser = 4
}

/// The signal violation subcause.
public enum SignalViolationSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The stop sign violation subcause.
    case stopSignViolation = 1
    /// The traffic light violation subcause.
    case trafficLightViolation = 2
    /// The turning regulation violation subcause.
    case turningRegulationViolation = 3
}

/// The dangerous situation subcause/
public enum DangerousSituationSubcause: UInt8, Codable, Sendable {
    /// The unknown subcause.
    case unknown = 0
    /// The emergency electronic brake engaged subcause.
    case emergencyElectronicBrakeEngaged = 1
    /// The pre crash system engaged subcause.
    case preCrashSystemEngaged = 2
    /// The ESP engaged subcause.
    case espEngaged = 3
    /// The ABS engaged subcause.
    case absEngaged = 4
    /// The AEB engaged subcause.
    case aebEngaged = 5
    /// The brake warning engaged subcause.
    case brakeWarningEngaged = 6
    /// The collision risk warning engaged subcause.
    case collisionRiskWarningEngaged = 7
}
// swiftlint:enable file_length
