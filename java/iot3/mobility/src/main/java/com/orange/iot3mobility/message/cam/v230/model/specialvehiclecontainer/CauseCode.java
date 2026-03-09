/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * CauseCode v2.3.0
 * <p>
 * Representation of the cause code value of a traffic event.
 *
 * @param cause Main cause of a detected event. trafficCondition (1), accident (2), roadworks (3), impassability (5),
 *              adverseWeatherCondition-Adhesion (6), aquaplaning (7), hazardousLocation-SurfaceCondition (9),
 *              hazardousLocation-ObstacleOnTheRoad (10), hazardousLocation-AnimalOnTheRoad (11),
 *              humanPresenceOnTheRoad (12), wrongWayDriving (14), rescueAndRecoveryWorkInProgress (15),
 *              adverseWeatherCondition-ExtremeWeatherCondition (17), adverseWeatherCondition-Visibility (18),
 *              adverseWeatherCondition-Precipitation (19), violence (20), slowVehicle (26), dangerousEndOfQueue (27),
 *              publicTransportVehicleApproaching (28), vehicleBreakdown (91), postCrash (92), humanProblem (93),
 *              stationaryVehicle (94), emergencyVehicleApproaching (95), hazardousLocation-DangerousCurve (96),
 *              collisionRisk (97), signalViolation (98), dangerousSituation (99), railwayLevelCrossing (100)
 * @param subcause Subordinate cause of a detected event.
 *                 <ul>
 *                 <li>trafficCondition (1): unavailable (0), increasedVolumeOfTraffic (1),
 *                 trafficJamSlowlyIncreasing (2), trafficJamIncreasing (3), trafficJamStronglyIncreasing (4),
 *                 trafficJam (5), trafficJamSlightlyDecreasing (6), trafficJamDecreasing (7),
 *                 trafficJamStronglyDecreasing (8), trafficJamStable (9)</li>
 *                 <li>accident (2): unavailable (0), multiVehicleAccident (1), heavyAccident (2),
 *                 accidentInvolvingLorry (3), accidentInvolvingBus (4), accidentInvolvingHazardousMaterials (5),
 *                 accidentOnOppositeLane (6), unsecuredAccident (7), assistanceRequested (8)</li>
 *                 <li>roadworks (3): unavailable (0), majorRoadworks (1), roadMarkingWork (2),
 *                 slowMovingRoadMaintenance (3), shortTermStationaryRoadworks (4), streetCleaning (5),
 *                 winterService (6), setupPhase (7), remodellingPhase (8), dismantlingPhase (9)</li>
 *                 <li>impassability (5): unavailable (0), flooding (1), dangerOfAvalanches (2),
 *                 blastingOfAvalanches (3), landslips (4), chemicalSpillage (5), winterClosure (6), sinkhole (7),
 *                 earthquakeDamage (8), fallenTrees (9), rockfalls (10), sewerOverflow (11), stormDamage (12),
 *                 subsidence (13), burstPipe (14), burstWaterMain (15), fallenPowerCables (16), snowDrifts (17)</li>
 *                 <li>adverseWeatherCondition-Adhesion (6): unavailable (0), heavyFrostOnRoad (1), fuelOnRoad (2),
 *                 mudOnRoad (3), snowOnRoad (4), iceOnRoad (5), blackIceOnRoad (6), oilOnRoad (7), looseChippings (8),
 *                 instantBlackIce (9), roadsSalted (10)</li>
 *                 <li>aquaplaning (7): none</li>
 *                 <li>hazardousLocation-SurfaceCondition (9): unavailable (0), rockfalls (1), earthquakeDamage (2),
 *                 sewerCollapse (3), subsidence (4), snowDrifts (5), stormDamage (6), burstPipe (7),
 *                 volcanoEruption (8), fallingIce (9), fire (10), flooding (11)</li>
 *                 <li>hazardousLocation-ObstacleOnTheRoad (10): unavailable (0), shedLoad (1), partsOfVehicles (2),
 *                 partsOfTyres (3), bigObjects (4), fallenTrees (5), hubCaps (6), waitingVehicles (7)</li>
 *                 <li>hazardousLocation-AnimalOnTheRoad (11): unavailable (0), wildAnimals (1), herdOfAnimals (2),
 *                 smallAnimals (3), largeAnimals (4), wildAnimalsSmall (5), wildAnimalsLarge (6), domesticAnimals (7),
 *                 domesticAnimalsSmall (8), domesticAnimalsLarge (9)</li>
 *                 <li>humanPresenceOnTheRoad (12): unavailable (0), childrenOnRoadway (1), cyclistOnRoadway (2),
 *                 motorcyclistOnRoadway (3), pedestrian (4), ordinary-pedestrian (5), road-worker (6),
 *                 first-responder (7), lightVruVehicle (8), bicyclist (9), wheelchair-user (10), horse-and-rider (11),
 *                 rollerskater (12), e-scooter (13), personal-transporter (14), pedelec (15), speed-pedelec (16),
 *                 ptw (17), moped (18), motorcycle (19), motorcycle-and-sidecar-right (20),
 *                 motorcycle-and-sidecar-left (21)</li>
 *                 <li>wrongWayDriving (14): unavailable (0), wrongLane (1), wrongDirection (2)</li>
 *                 <li>rescueAndRecoveryWorkInProgress (15): unavailable (0), emergencyVehicles (1),
 *                 rescueHelicopterLanding (2), policeActivityOngoing (3), medicalEmergencyOngoing (4),
 *                 childAbductionInProgress (5), prioritizedVehicle (6), rescueAndRecoveryVehicle (7)</li>
 *                 <li>adverseWeatherCondition-ExtremeWeatherCondition (17): unavailable (0), strongWinds (1),
 *                 damagingHail (2), hurricane (3), thunderstorm (4), tornado (5), blizzard (6)</li>
 *                 <li>adverseWeatherCondition-Visibility (18): unavailable (0), fog (1), smoke (2), heavySnowfall (3),
 *                 heavyRain (4), heavyHail (5), lowSunGlare (6), sandstorms (7), swarmsOfInsects (8)</li>
 *                 <li>adverseWeatherCondition-Precipitation (19): unavailable (0), heavyRain (1), heavySnowfall (2),
 *                 softHail (3)</li>
 *                 <li>violence (20): none</li>
 *                 <li>slowVehicle (26): none</li>
 *                 <li>dangerousEndOfQueue (27): unavailable (0), suddenEndOfQueue (1), queueOverHill (2),
 *                 queueAroundBend (3), queueInTunnel (4)</li>
 *                 <li>publicTransportVehicleApproaching (28): none</li>
 *                 <li>vehicleBreakdown (91): unavailable (0), lackOfFuel (1), lackOfBatteryPower (2),
 *                 engineProblem (3), transmissionProblem (4), engineCoolingProblem (5), brakingSystemProblem (6),
 *                 steeringProblem (7), tyrePuncture (8), tyrePressureProblem (9), vehicleOnFire (10)</li>
 *                 <li>postCrash (92): unavailable (0), accidentWithoutECallTriggered (1),
 *                 accidentWithECallManuallyTriggered (2), accidentWithECallAutomaticallyTriggered (3),
 *                 accidentWithECallTriggeredWithoutAccessToCellularNetwork (4)</li>
 *                 <li>humanProblem (93): unavailable (0), glycemiaProblem (1), heartProblem (2)</li>
 *                 <li>stationaryVehicle (94): unavailable (0), humanProblem (1), vehicleBreakdown (2), postCrash (3),
 *                 publicTransportStop (4), carryingDangerousGoods (5), vehicleOnFire (6)</li>
 *                 <li>emergencyVehicleApproaching (95): unavailable (0), emergencyVehicleApproaching (1),
 *                 prioritizedVehicleApproaching (2)</li>
 *                 <li>hazardousLocation-DangerousCurve (96): unavailable (0), dangerousLeftTurnCurve (1),
 *                 dangerousRightTurnCurve (2), multipleCurvesStartingWithUnknownTurningDirection (3),
 *                 multipleCurvesStartingWithLeftTurn (4), multipleCurvesStartingWithRightTurn (5)</li>
 *                 <li>collisionRisk (97): unavailable (0), longitudinalCollisionRisk (1), crossingCollisionRisk (2),
 *                 lateralCollisionRisk (3), vulnerableRoadUser (4), collisionRiskWithPedestrian (5),
 *                 collisionRiskWithCyclist (6), collisionRiskWithMotorVehicle (7)</li>
 *                 <li>signalViolation (98): unavailable (0), stopSignViolation (1), trafficLightViolation (2),
 *                 turningRegulationViolation (3)</li>
 *                 <li>dangerousSituation (99): unavailable (0), emergencyElectronicBrakeEngaged (1),
 *                 preCrashSystemEngaged (2), espEngaged (3), absEngaged (4), aebEngaged (5), brakeWarningEngaged (6),
 *                 collisionRiskWarningEngaged (7)</li>
 *                 <li>railwayLevelCrossing (100): unavailable (0), doNotCrossAbnormalSituation (1), closed (2),
 *                 unguarded (3), nominal (4), trainApproaching (5)</li>
 *                 </ul>
 */
public record CauseCode(
        int cause,
        Integer subcause) {}
