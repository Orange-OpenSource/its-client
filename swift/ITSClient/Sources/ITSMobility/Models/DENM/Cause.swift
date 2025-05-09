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

/// The cause.
public struct Cause: Codable, Sendable, Equatable {
    /// The cause.
    public let cause: UInt8
    /// The subcause
    public let subcause: UInt8?

    init(cause: UInt8, subcause: UInt8? = nil) {
        self.cause = cause
        self.subcause = subcause
    }
}

// Reserved
extension Cause {
    /// The reserved cause.
    public static let reserved = Cause(cause: 0, subcause: nil)
}

// Traffic condition
extension Cause {
    /// The traffic condition cause without subcause.
    public static let trafficCondition = Cause(cause: 1, subcause: nil)
    /// The traffic condition cause with an increased volume of traffic subcause.
    public static let increasedVolumeOfTraffic = Cause(cause: 1, subcause: 1)
    /// The traffic condition cause with a traffic jam slowly increasing subcause.
    public static let trafficJamSlowlyIncreasing = Cause(cause: 1, subcause: 2)
    /// The traffic condition cause with a traffic jam increasing subcause.
    public static let trafficJamIncreasing = Cause(cause: 1, subcause: 3)
    /// The traffic condition cause with a traffic jam strongly increasing subcause.
    public static let trafficJamStronglyIncreasing = Cause(cause: 1, subcause: 4)
    /// The traffic condition cause with a traffic stationary subcause.
    public static let trafficStationary = Cause(cause: 1, subcause: 5)
    /// The traffic condition cause with a traffic jam slightly decreasing subcause.
    public static let trafficJamSlightlyDecreasing = Cause(cause: 1, subcause: 6)
    /// The traffic condition cause with a traffic jam decreasing subcause.
    public static let trafficJamDecreasing = Cause(cause: 1, subcause: 7)
    /// The traffic condition cause with a traffic jam strongly decreasing subcause.
    public static let trafficJamStronglyDecreasing = Cause(cause: 1, subcause: 8)
}

// Accident
extension Cause {
    /// The accident cause without subcause.
    public static let accident = Cause(cause: 2, subcause: nil)
    /// The accident cause with a multi vehicle accident subcause.
    public static let multiVehiculeAccident = Cause(cause: 2, subcause: 1)
    /// The accident cause with an heavy accident subcause.
    public static let heavyAccident = Cause(cause: 2, subcause: 2)
    /// The accident cause with an accident involving lorry subcause.
    public static let accidentInvolvingLorry = Cause(cause: 2, subcause: 3)
    /// The accident cause with an accident involving bus subcause.
    public static let accidentInvolvingBus = Cause(cause: 2, subcause: 4)
    /// The accident cause with an accident involving hazardous materials subcause.
    public static let accidentInvolvingHazardousMaterials = Cause(cause: 2, subcause: 5)
    /// The accident cause with an accident on opposite lane subcause.
    public static let accidentOnOppositeLane = Cause(cause: 2, subcause: 6)
    /// The accident cause with an unsecured accident subcause.
    public static let unsecuredAccident = Cause(cause: 2, subcause: 7)
    /// The accident cause with an assistance requested subcause.
    public static let assistanceRequested = Cause(cause: 2, subcause: 8)
}

// Roadworks
extension Cause {
    /// The roadworks cause without subcause.
    public static let roadworks = Cause(cause: 3, subcause: nil)
    /// The roadworks cause with a major roadworks subcause.
    public static let majorRoadworks = Cause(cause: 3, subcause: 1)
    /// The roadworks cause with a road marking work subcause.
    public static let roadMarkingWork = Cause(cause: 3, subcause: 2)
    /// The roadworks cause with a slow moving road maintenance subcause.
    public static let slowMovingRoadMaintenance = Cause(cause: 3, subcause: 3)
    /// The roadworks cause with a short term stationary roadworks subcause.
    public static let shortTermStationaryRoadworks = Cause(cause: 3, subcause: 4)
    /// The roadworks cause with a street cleaning subcause.
    public static let streetCleaning = Cause(cause: 3, subcause: 5)
    /// The roadworks cause with a winter service subcause.
    public static let winterService = Cause(cause: 3, subcause: 6)
}

// Adverse weather condition adhesion
extension Cause {
    /// The adverse weather condition adhesion cause without subcause.
    public static let adverseWeatherConditionAdhesion = Cause(cause: 6, subcause: nil)
    /// The adverse weather condition adhesion cause with an heavy frost on road subcause.
    public static let heavyFrostOnRoad = Cause(cause: 6, subcause: 1)
    /// The adverse weather condition adhesion cause with a fuel on road subcause.
    public static let fuelOnRoad = Cause(cause: 6, subcause: 2)
    /// The adverse weather condition adhesion cause with a mud on road subcause.
    public static let mudOnRoad = Cause(cause: 6, subcause: 3)
    /// The adverse weather condition adhesion cause with a snow on road subcause.
    public static let snowOnRoad = Cause(cause: 6, subcause: 4)
    /// The adverse weather condition adhesion cause with an ice on road subcause.
    public static let iceOnRoad = Cause(cause: 6, subcause: 5)
    /// The adverse weather condition adhesion cause with a black ice on road subcause.
    public static let blackIceOnRoad = Cause(cause: 6, subcause: 6)
    /// The adverse weather condition adhesion cause with an oil on road subcause.
    public static let oilOnRoad = Cause(cause: 6, subcause: 7)
    /// The adverse weather condition adhesion cause with a loose chippings subcause.
    public static let looseChippings = Cause(cause: 6, subcause: 8)
    /// The adverse weather condition adhesion cause with an instant black ice subcause.
    public static let instantBlackIce = Cause(cause: 6, subcause: 9)
    /// The adverse weather condition adhesion cause with a road salted subcause.
    public static let roadsSalted = Cause(cause: 6, subcause: 10)
}

// Hazardous location surface condition
extension Cause {
    /// The hazardous location surface condition cause without subcause.
    public static let hazardousLocationSurfaceCondition = Cause(cause: 9, subcause: nil)
    /// The hazardous location surface condition cause with a rock falls subcause.
    public static let rockfalls = Cause(cause: 9, subcause: 1)
    /// The hazardous location surface condition cause with an earthquake damage subcause.
    public static let earthquakeDamage = Cause(cause: 9, subcause: 2)
    /// The hazardous location surface condition cause with a sewer collapse subcause.
    public static let sewerCollapse = Cause(cause: 9, subcause: 3)
    /// The hazardous location surface condition cause with a subsidence subcause.
    public static let subsidence = Cause(cause: 9, subcause: 4)
    /// The hazardous location surface condition cause with a snow drifts subcause.
    public static let snowDrifts = Cause(cause: 9, subcause: 5)
    /// The hazardous location surface condition cause with a storm damage subcause.
    public static let stormDamage = Cause(cause: 9, subcause: 6)
    /// The hazardous location surface condition cause with a burst pipe subcause.
    public static let burstPipe = Cause(cause: 9, subcause: 7)
    /// The hazardous location surface condition cause with a volcano eruption subcause.
    public static let volcanoEruption = Cause(cause: 9, subcause: 8)
    /// The hazardous location surface condition cause with a falling ice subcause.
    public static let fallingIce = Cause(cause: 9, subcause: 9)
}

// Hazardous location obstacle on the road
extension Cause {
    /// The hazardous location obstacle on the road cause without subcause.
    public static let hazardousLocationObstacleOnTheRoad = Cause(cause: 10, subcause: nil)
    /// The hazardous location obstacle on the road cause with a shed load subcause.
    public static let shedLoad = Cause(cause: 10, subcause: 1)
    /// The hazardous location obstacle on the road cause with a parts of vehicles subcause.
    public static let partsOfVehicles = Cause(cause: 10, subcause: 2)
    /// The hazardous location obstacle on the road cause with a parts of tyres subcause.
    public static let partsOfTyres = Cause(cause: 10, subcause: 3)
    /// The hazardous location obstacle on the road cause with a big objects subcause.
    public static let bigObjects = Cause(cause: 10, subcause: 4)
    /// The hazardous location obstacle on the road cause with a fallen trees subcause.
    public static let fallenTrees = Cause(cause: 10, subcause: 5)
    /// The hazardous location obstacle on the road cause with a hub caps subcause.
    public static let hubCaps = Cause(cause: 10, subcause: 6)
    /// The hazardous location obstacle on the road cause with a waiting vehicles subcause.
    public static let waitingVehicles = Cause(cause: 10, subcause: 7)
}

// Hazardous location animal on the road
extension Cause {
    /// The hazardous location animal on the road cause without subcause.
    public static let hazardousLocationAnimalOnTheRoad = Cause(cause: 11, subcause: nil)
    /// The hazardous location animal on the road cause with a wild animals subcause.
    public static let wildAnimals = Cause(cause: 11, subcause: 1)
    /// The hazardous location animal on the road cause with a herd of animals subcause.
    public static let herdOfAnimals = Cause(cause: 11, subcause: 2)
    /// The hazardous location animal on the road cause with a small animals subcause.
    public static let smallAnimals = Cause(cause: 11, subcause: 3)
    /// The hazardous location animal on the road cause with a large animals subcause.
    public static let largeAnimals = Cause(cause: 11, subcause: 4)
}

// Human presence on the road
extension Cause {
    /// The human presence on the road cause without subcause.
    public static let humanPresenceOnTheRoad = Cause(cause: 12, subcause: nil)
    /// The human presence on the road cause with a children on roadway subcause.
    public static let childrenOnRoadway = Cause(cause: 12, subcause: 1)
    /// The human presence on the road cause with a cyclist on roadway subcause.
    public static let cyclistOnRoadway = Cause(cause: 12, subcause: 2)
    /// The human presence on the road cause with a motorcyclist on roadway subcause.
    public static let motorcyclistOnRoadway = Cause(cause: 12, subcause: 3)
    /// The human presence on the road cause with a scooter on roadway subcause.
    public static let scooterOnRoadway = Cause(cause: 12, subcause: 4)
}

// Wrong way driving
extension Cause {
    /// The wrong way driving cause without subcause.
    public static let wrongWayDriving = Cause(cause: 14, subcause: nil)
    /// The wrong way driving cause with a wrong lane subcause.
    public static let wrongLane = Cause(cause: 14, subcause: 1)
    /// The wrong way driving cause with a wrong direction subcause.
    public static let wrongDirection = Cause(cause: 14, subcause: 2)
}

// Rescue and recovery work in progress
extension Cause {
    /// The rescue and recovery work in progress cause without subcause.
    public static let rescueAndRecoveryWorkInProgress = Cause(cause: 15, subcause: nil)
    /// The rescue and recovery work in progress cause with an emergency vehicles subcause.
    public static let emergencyVehicles = Cause(cause: 15, subcause: 1)
    /// The rescue and recovery work in progress cause with a rescue helicopter landing subcause.
    public static let rescueHelicopterLanding = Cause(cause: 15, subcause: 2)
    /// The rescue and recovery work in progress cause with a police activity ongoing subcause.
    public static let policeActivityOngoing = Cause(cause: 15, subcause: 3)
    /// The rescue and recovery work in progress cause with a medical emergency ongoing subcause.
    public static let medicalEmergencyOngoing = Cause(cause: 15, subcause: 4)
    /// The rescue and recovery work in progress cause with a child abduction in progress subcause.
    public static let childAbductionInProgress = Cause(cause: 15, subcause: 5)
}

// Extreme weather condition
extension Cause {
    /// The extreme weather condition cause without subcause.
    public static let adverseWeatherConditionExtremeWeatherCondition = Cause(cause: 17, subcause: nil)
    /// The extreme weather condition cause with a strong winds subcause.
    public static let strongWinds = Cause(cause: 17, subcause: 1)
    /// The extreme weather condition cause with a damaging hail subcause.
    public static let damagingHail = Cause(cause: 17, subcause: 2)
    /// The extreme weather condition cause with a hurricane subcause.
    public static let hurricane = Cause(cause: 17, subcause: 3)
    /// The extreme weather condition cause with a thunderstorm subcause.
    public static let thunderstorm = Cause(cause: 17, subcause: 4)
    /// The extreme weather condition cause with a tornado subcause.
    public static let tornado = Cause(cause: 17, subcause: 5)
    /// The extreme weather condition cause with a blizzard subcause.
    public static let blizzard = Cause(cause: 17, subcause: 6)
}

// Adverse weather condition visibility
extension Cause {
    /// The adverse weather condition visibility cause without subcause.
    public static let adverseWeatherConditionVisibility = Cause(cause: 18, subcause: nil)
    /// The adverse weather condition visibility cause with a fog subcause.
    public static let fog = Cause(cause: 18, subcause: 1)
    /// The adverse weather condition visibility cause with a smoke subcause.
    public static let smoke = Cause(cause: 18, subcause: 2)
    /// The adverse weather condition visibility cause with an heavy snowfall subcause.
    public static let heavySnowfall = Cause(cause: 18, subcause: 3)
    /// The adverse weather condition visibility cause with an heavy rain subcause.
    public static let heavyRain = Cause(cause: 18, subcause: 4)
    /// The adverse weather condition visibility cause with an heavy hail subcause.
    public static let heavyHail = Cause(cause: 18, subcause: 5)
    /// The adverse weather condition visibility cause with a low sun glare subcause.
    public static let lowSunGlare = Cause(cause: 18, subcause: 6)
    /// The adverse weather condition visibility cause with a sandstorm subcause.
    public static let sandstorms = Cause(cause: 18, subcause: 7)
    /// The adverse weather condition visibility cause with a swarm of insects subcause.
    public static let swarmsOfInsects = Cause(cause: 18, subcause: 8)
}

// Adverse weather condition precipitation
extension Cause {
    /// The adverse weather condition precipitation cause without subcause.
    public static let adverseWeatherConditionPrecipitation = Cause(cause: 19, subcause: nil)
    /// The adverse weather condition precipitation cause with an heavy rain precipitation subcause.
    public static let heavyRainPrecipitation = Cause(cause: 19, subcause: 1)
    /// The adverse weather condition precipitation cause with an heavy snowfall precipitation subcause.
    public static let heavySnowfallPrecipitation = Cause(cause: 19, subcause: 2)
    /// The adverse weather condition precipitation cause with a soft hail subcause.
    public static let softHail = Cause(cause: 19, subcause: 3)
}

// Slow vehicle
extension Cause {
    /// The slow vehicle cause without subcause.
    public static let slowVehicle = Cause(cause: 26, subcause: nil)
    /// The slow vehicle cause with a maintenance vehicle subcause.
    public static let maintenanceVehicle = Cause(cause: 26, subcause: 1)
    /// The slow vehicle cause with a vehicles slowing to look at accident subcause.
    public static let vehiclesSlowingToLookAtAccident = Cause(cause: 26, subcause: 2)
    /// The slow vehicle cause with an abnormal load subcause.
    public static let abnormalLoad = Cause(cause: 26, subcause: 3)
    /// The slow vehicle cause with an abnormal wide load subcause.
    public static let abnormalWideLoad = Cause(cause: 26, subcause: 4)
    /// The slow vehicle cause with a convoy subcause.
    public static let convoy = Cause(cause: 26, subcause: 5)
    /// The slow vehicle cause with a snowplough subcause.
    public static let snowplough = Cause(cause: 26, subcause: 6)
    /// The slow vehicle cause with a deicing subcause.
    public static let deicing = Cause(cause: 26, subcause: 7)
    /// The slow vehicle cause with a salting vehicles subcause.
    public static let saltingVehicles = Cause(cause: 26, subcause: 8)
}

// Dangerous end of queue
extension Cause {
    /// The dangerous end of queue cause without subcause.
    public static let dangerousEndOfQueue = Cause(cause: 27, subcause: nil)
    /// The dangerous end of queue cause with a sudden end of queue subcause.
    public static let suddenEndOfQueue = Cause(cause: 27, subcause: 1)
    /// The dangerous end of queue cause with a queue over hill subcause.
    public static let queueOverHill = Cause(cause: 27, subcause: 2)
    /// The dangerous end of queue cause with a queue around bend subcause.
    public static let queueAroundBend = Cause(cause: 27, subcause: 3)
    /// The dangerous end of queue cause with a queue in tunnel subcause.
    public static let queueInTunnel = Cause(cause: 27, subcause: 4)
}

// Vehicle breakdown
extension Cause {
    /// The vehicle breakdown cause without subcause.
    public static let vehicleBreakdown = Cause(cause: 91, subcause: nil)
    /// The vehicle breakdown cause with a lack of fuel subcause.
    public static let lackOfFuel = Cause(cause: 91, subcause: 1)
    /// The vehicle breakdown cause with a lack of battery power subcause.
    public static let lackOfBatteryPower = Cause(cause: 91, subcause: 2)
    /// The vehicle breakdown cause with an engine problem subcause.
    public static let engineProblem = Cause(cause: 91, subcause: 3)
    /// The vehicle breakdown cause with a transmission problem subcause.
    public static let transmissionProblem = Cause(cause: 91, subcause: 4)
    /// The vehicle breakdown cause with an engine cooling problem subcause.
    public static let engineCoolingProblem = Cause(cause: 91, subcause: 5)
    /// The vehicle breakdown cause with a braking system problem subcause.
    public static let brakingSystemProblem = Cause(cause: 91, subcause: 6)
    /// The vehicle breakdown cause with a steering problem subcause.
    public static let steeringProblem = Cause(cause: 91, subcause: 7)
    /// The vehicle breakdown cause with a tyre puncture subcause.
    public static let tyrePuncture = Cause(cause: 91, subcause: 8)
}

// Post crash
extension Cause {
    /// The post crash cause without subcause.
    public static let postCrash = Cause(cause: 92, subcause: nil)
    /// The post crash cause with an accident without e call triggered subcause.
    public static let accidentWithoutECallTriggered = Cause(cause: 92, subcause: 1)
    /// The post crash cause with an accident with e call manually triggered subcause.
    public static let accidentWithECallManuallyTriggered = Cause(cause: 92, subcause: 2)
    /// The post crash cause with an accident with e call automatically triggered subcause.
    public static let accidentWithECallAutomaticallyTriggered = Cause(cause: 92, subcause: 3)
    /// The post crash cause with an accident with e call triggered without access to cellular network subcause.
    public static let accidentWithECallTriggeredWithoutAccessToCellularNetwork = Cause(cause: 92, subcause: 4)
}

// Human problem
extension Cause {
    /// The human problem cause without subcause.
    public static let humanProblem = Cause(cause: 93, subcause: nil)
    /// The human problem cause with a glycemic problem subcause.
    public static let glycemicProblem = Cause(cause: 93, subcause: 1)
    /// The human problem cause with a heart problem subcause.
    public static let heartProblem = Cause(cause: 93, subcause: 2)
}

// Stationary vehicle
extension Cause {
    /// The stationary vehicle cause without subcause.
    public static let stationaryVehicle = Cause(cause: 94, subcause: nil)
    /// The stationary vehicle cause with a human problem subcause.
    public static let stationaryVehicleHumanProblem = Cause(cause: 94, subcause: 1)
    /// The stationary vehicle cause with a vehicle breakdown subcause.
    public static let stationaryVehicleVehicleBreakdown = Cause(cause: 94, subcause: 2)
    /// The stationary vehicle cause with a post crash subcause.
    public static let stationaryVehiclePostCrash = Cause(cause: 94, subcause: 3)
    /// The stationary vehicle cause with a public transport stop subcause.
    public static let publicTransportStop = Cause(cause: 94, subcause: 4)
    /// The stationary vehicle cause with a carrying dangerous goods subcause.
    public static let carryingDangerousGoods = Cause(cause: 94, subcause: 5)
}

// Emergency vehicle approaching
extension Cause {
    /// The emergency vehicle approaching cause without subcause.
    public static let emergencyVehicleApproachingWithoutSubcause = Cause(cause: 95, subcause: nil)
    /// The emergency vehicle approaching cause with an emergency vehicle approaching subcause.
    public static let emergencyVehicleApproaching = Cause(cause: 95, subcause: 1)
    /// The emergency vehicle approaching cause with a prioritized vehicle approaching subcause.
    public static let prioritizedVehicleApproaching = Cause(cause: 95, subcause: 2)
}

// Hazardous location dangerous curve
extension Cause {
    /// The hazardous location dangerous curve cause without subcause.
    public static let hazardousLocationDangerousCurve = Cause(cause: 96, subcause: nil)
    /// The hazardous location dangerous curve cause with a dangerous left turn curve subcause.
    public static let dangerousLeftTurnCurve = Cause(cause: 96, subcause: 1)
    /// The hazardous location dangerous curve cause with a dangerous right turn curve subcause.
    public static let dangerousRightTurnCurve = Cause(cause: 96, subcause: 2)
    /// The hazardous location dangerous curve cause with a multiple curves starting with unknown turning direction subcause.
    public static let multipleCurvesStartingWithUnknownTurningDirection = Cause(cause: 96, subcause: 3)
    /// The hazardous location dangerous curve cause with a multiple curves starting with left turn subcause.
    public static let multipleCurvesStartingWithLeftTurn = Cause(cause: 96, subcause: 4)
    /// The hazardous location dangerous curve cause with a multiple curves starting with right turn subcause.
    public static let multipleCurvesStartingWithRightTurn = Cause(cause: 96, subcause: 5)
}

// Collision risk
extension Cause {
    /// The collision risk cause without subcause.
    public static let collisionRisk = Cause(cause: 97, subcause: nil)
    /// The collision risk cause with a longitudinal collision risk subcause.
    public static let longitudinalCollisionRisk = Cause(cause: 97, subcause: 1)
    /// The collision risk cause with a crossing collision risk subcause.
    public static let crossingCollisionRisk = Cause(cause: 97, subcause: 2)
    /// The collision risk cause with a lateral collision risk subcause.
    public static let lateralCollisionRisk = Cause(cause: 97, subcause: 3)
    /// The collision risk cause with a vulnerable road user subcause.
    public static let vulnerableRoadUser = Cause(cause: 97, subcause: 4)
}

// Signal violation
extension Cause {
    /// The signal violation cause without subcause.
    public static let signalViolation = Cause(cause: 98, subcause: nil)
    /// The signal violation cause with a stop sign violation subcause.
    public static let stopSignViolation = Cause(cause: 98, subcause: 1)
    /// The signal violation cause with a traffic light violation subcause.
    public static let trafficLightViolation = Cause(cause: 98, subcause: 2)
    /// The signal violation cause with a turning regulation violation subcause.
    public static let turningRegulationViolation = Cause(cause: 98, subcause: 3)
}

// Dangerous situation
extension Cause {
    /// The dangerous situation cause without subcause.
    public static let dangerousSituation = Cause(cause: 99, subcause: nil)
    /// The dangerous situation cause with an emergency electronic brake engaged subcause.
    public static let emergencyElectronicBrakeEngaged = Cause(cause: 99, subcause: 1)
    /// The dangerous situation cause with a pre crash system engaged subcause.
    public static let preCrashSystemEngaged = Cause(cause: 99, subcause: 2)
    /// The dangerous situation cause with an ESP engaged subcause.
    public static let espEngaged = Cause(cause: 99, subcause: 3)
    /// The dangerous situation cause with an ABS engaged subcause.
    public static let absEngaged = Cause(cause: 99, subcause: 4)
    /// The dangerous situation cause with an AEB engaged subcause.
    public static let aebEngaged = Cause(cause: 99, subcause: 5)
    /// The dangerous situation cause with a brake warning engaged subcause.
    public static let brakeWarningEngaged = Cause(cause: 99, subcause: 6)
    /// The dangerous situation cause with a collision risk warning engaged subcause.
    public static let collisionRiskWarningEngaged = Cause(cause: 99, subcause: 7)
}
