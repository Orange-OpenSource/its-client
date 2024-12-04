/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its;

public enum HazardType {
	
    UNDEFINED("roadHazard", 0, 0),

    TRAFFIC_CONDITION_NO_SUBCAUSE ("trafficJam", 0, 1),
    INCREASED_VOLUME_OF_TRAFFIC ("increasedVolumeOfTraffic", 1, 1),
    TRAFFIC_JAM_SLOWLY_INCREASING ("trafficJamSlowlyIncreasing", 2, 1),
    TRAFFIC_JAM_INCREASING ("trafficJamIncreasing", 3, 1),
    TRAFFIC_JAM_STRONGLY_INCREASING("trafficJamStronglyIncreasing", 4, 1),
    TRAFFIC_STATIONARY ("trafficStationary", 5, 1),
    TRAFFIC_JAM_SLIGHTLY_DECREASING ("trafficJamSlightlyDecreasing", 6, 1),
    TRAFFIC_JAM_DECREASING ("trafficJamDecreasing", 7, 1),
    TRAFFIC_JAM_STRONGLY_DECREASING ("trafficJamStronglyDecreasing", 8, 1),

    ACCIDENT_NO_SUBCAUSE ("accident", 0, 2),
    MULTI_VEHICLE_ACCIDENT ("multiVehicleAccident", 1, 2),
    HEAVY_ACCIDENT ("heavyAccident", 2, 2),
    ACCIDENT_INVOLVING_LORRY ("accidentInvolvingLorry", 3, 2),
    ACCIDENT_INVOLVING_BUS ("accidentInvolvingBus", 4, 2),
    ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS ("accidentInvolvingHazardousMaterials", 5, 2),
    ACCIDENT_ON_OPPOSITE_LANE ("accidentOnOppositeLane", 6, 2),
    UNSECURED_ACCIDENT ("unsecuredAccident", 7, 2),
    ASSISTANCE_REQUESTED ("assistanceRequested", 8, 2),

    ROADWORKS_NO_SUBCAUSE ("roadworks", 0, 3),
    MAJOR_ROADWORKS ("majorRoadworks", 1, 3),
    ROAD_MARKING_WORK ("roadMarkingWork", 2, 3),
    SLOW_MOVING_ROAD_MAINTENANCE ("slowMovingRoadMaintenance", 3, 3),
    SHORT_TERM_STATIONARY_ROADWORKS ("shortTermStationaryRoadworks", 4, 3),
    STREET_CLEANING ("streetCleaning", 5, 3),
    WINTER_SERVICE ("winterService", 6, 3),
    
    ADVERSE_WEATHER_CONDITION_ADHESION_NO_SUBCAUSE ("slipperyRoad", 0, 6),
    HEAVY_FROST_ON_ROAD ("heavyFrostOnRoad", 1, 6),
    FUEL_ON_ROAD ("fuelOnRoad", 2, 6),
    MUD_ON_ROAD ("mudOnRoad", 3, 6),
    SNOW_ON_ROAD ("snowOnRoad", 4, 6),
    ICE_ON_ROAD ("iceOnRoad", 5, 6),
    BLACK_ICE_ON_ROAD ("blackIceOnRoad", 6, 6),
    OIL_ON_ROAD ("oilOnRoad", 7, 6),
    LOOSE_CHIPPINGS ("looseChippings", 8, 6),
    INSTANT_BLACK_ICE ("instantBlackIce", 9, 6),
    ROADS_SALTED ("roadsSalted", 10, 6),
    
    HAZARDOUS_LOCATION_SURFACE_CONDITION_NO_SUBCAUSE ("hazardousSurfaceCondition", 0, 9),
    ROCKFALLS ("rockfalls", 1, 9),
    EARTHQUAKE_DAMAGE ("earthquakeDamage", 2, 9),
    SEWER_COLLAPSE ("sewerCollapse", 3, 9),
    SUBSIDENCE ("subsidence", 4, 9),
    SNOW_DRIFTS ("snowDrifts", 5, 9),
    STORM_DAMAGE ("stormDamage", 6, 9),
    BURST_PIPE ("burstPipe", 7, 9),
    VOLCANO_ERUPTION ("volcanoEruption", 8, 9),
    FALLING_ICE ("fallingIce", 9, 9),

    HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD_NO_SUBCAUSE ("hazardousObstacleOnTheRoad", 0, 10),
    SHED_LOAD ("shedLoad", 1, 10),
    PARTS_OF_VEHICLES ("partsOfVehicles", 2, 10),
    PARTS_OF_TYRES ("partsOfTyres", 3, 10),
    BIG_OBJECTS ("bigObjects", 4, 10),
    FALLEN_TREES ("fallenTrees", 5, 10),
    HUB_CAPS ("hubCaps", 6, 10),
    WAITING_VEHICLES ("waitingVehicles", 7, 10),

    HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD_NO_SUBCAUSE ("animalOnTheRoad", 0, 11),
    WILD_ANIMALS ("wildAnimals", 1, 11),
    HERD_OF_ANIMALS ("herdOfAnimals", 2, 11),
    SMALL_ANIMALS ("smallAnimals", 3, 11),
    LARGE_ANIMALS ("largeAnimals", 4, 11),

    HUMAN_PRESENCE_ON_THE_ROAD_NO_SUBCAUSE ("humanPresenceOnTheRoad", 0, 12),
    CHILDREN_ON_ROADWAY ("childrenOnRoadway", 1, 12),
    CYCLIST_ON_ROADWAY ("cyclistOnRoadway", 2, 12),
    MOTORCYCLIST_ON_ROADWAY ("motorcyclistOnRoadway", 3, 12),
    SCOOTER_ON_ROADWAY ("scooterOnRoadway", 4, 12),

    WRONG_WAY_OF_DRIVING_NO_SUBCAUSE ("wrongWayOfDriving", 0, 14),
    WRONG_LANE ("wrongLane", 1, 14),
    WRONG_DIRECTION ("wrongDirection", 2, 14),
    
    RESCUE_AND_RECOVERY_WORK_IN_PROGRESS_NO_SUBCAUSE ("rescueAndRecoveryWorkInProgress", 0, 15),
    EMERGENCY_VEHICLES ("emergencyVehicles", 1, 15),
    RESCUE_HELICOPTER_LANDING ("rescueHelicopterLanding", 2, 15),
    POLICE_ACTIVITY_ONGOING ("policeActivityOngoing", 3, 15),
    MEDICAL_EMERGENCY_ONGOING ("medicalEmergencyOngoing", 4, 15),
    CHILD_ABDUCTION_IN_PROGRESS ("childAbductionInProgress", 5, 15),

    ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION_NO_SUBCAUSE ("extremeWeatherCondition", 0, 17),
    STRONG_WINDS ("strongWinds", 1, 17),
    DAMAGING_HAIL ("damagingHail", 2, 17),
    HURRICANE ("hurricane", 3, 17),
    THUNDERSTORM ("thunderstorm", 4, 17),
    TORNADO ("tornado", 5, 17),
    BLIZZARD ("blizzard", 6, 17),

    ADVERSE_WEATHER_CONDITION_VISIBILITY_NO_SUBCAUSE ("badVisibility", 0, 18),
    FOG ("fog", 1, 18),
    SMOKE ("smoke", 2, 18),
    HEAVY_SNOWFALL ("heavySnowfall", 3, 18),
    HEAVY_RAIN ("heavyRain", 4, 18),
    HEAVY_HAIL ("heavyHail", 5, 18),
    LOW_SUN_GLARE ("lowSunGlare", 6, 18),
    SANDSTORMS ("sandstorms", 7, 18),
    SWARMS_OF_INSECTS ("swarmsOfInsects", 8, 18),

    HEAVY_RAIN_PRECIPITATION_NO_SUBCAUSE ("heavyPrecipitation", 0, 19),
    HEAVY_RAIN_PRECIPITATION ("heavyRain", 1, 19),
    HEAVY_SNOWFALL_PRECIPITATION ("heavySnowfall", 2, 19),
    SOFT_HAIL ("softHail", 3, 19),

    SLOW_VEHICLE_NO_SUBCAUSE ("slowVehicle", 0, 26),
    MAINTENANCE_VEHICLE ("maintenanceVehicle", 1, 26),
    VEHICLES_SLOWING_TO_LOOK_AT_ACCIDENT ("vehiclesSlowingToLookAtAccident", 2, 26),
    ABNORMAL_LOAD ("abnormalLoad", 3, 26),
    ABNORMAL_WIDE_LOAD ("abnormalWideLoad", 4, 26),
    CONVOY ("convoy", 5, 26),
    SNOWPLOUGH ("snowplough", 6, 26),
    DEICING ("deicing", 7, 26),
    SALTING_VEHICLES ("saltingVehicles", 8, 26),
    
    DANGEROUS_END_OF_QUEUE_NO_SUBCAUSE ("dangerousEndOfQueue", 0, 27),
    SUDDEN_END_OF_QUEUE ("suddenEndOfQueue", 1, 27),
    QUEUE_OVER_HILL ("queueOverHill", 2, 27),
    QUEUE_AROUND_BEND ("queueAroundBend", 3, 27),
    QUEUE_IN_TUNNEL ("queueInTunnel", 4, 27),
    
    POST_CRASH_NO_SUBCAUSE ("postCrash", 0, 92),
    LACK_OF_FUEL ("lackOfFuel", 1, 92),
    LACK_OF_BATTERY_POWER ("lackOfBatteryPower", 2, 92),
    ENGINE_PROBLEM ("engineProblem", 3, 92),
    TRANSMISSION_PROBLEM ("transmissionProblem", 4, 92),
    ENGINE_COOLING_PROBLEM ("engineCoolingProblem", 5, 92),
    BRAKING_SYSTEM_PROBLEM ("brakingSystemProblem", 6, 92),
    STEERING_PROBLEM ("steeringProblem", 7, 92),
    TYRE_PUNCTURE ("tyrePuncture", 8, 92),
    ACCIDENT_WITHOUT_E_CALL_TRIGGERED ("accidentWithoutECallTriggered", 1, 92),
    ACCIDENT_WITH_E_CALL_MANUALLY_TRIGGERED ("accidentWithECallManuallyTriggered", 2, 92),
    ACCIDENT_WITH_E_CALL_AUTOMATICALLY_TRIGGERED ("accidentWithECallAutomaticallyTriggered", 3, 92),
    ACCIDENT_WITH_E_CALL_TRIGGERED_WITHOUT_ACCESS_TO_CELLULAR_NETWORK ("accidentWithECallTriggeredWithoutAccessToCellularNetwork", 4, 92),

    STATIONARY_VEHICLE_NO_SUBCAUSE ("stationaryVehicle", 0, 94),
    HUMAN_PROBLEM ("humanProblem", 1, 94),
    VEHICLE_BREAKDOWN ("vehicleBreakdown", 2, 94),
    POST_CRASH ("postCrash", 3, 94),
    PUBLIC_TRANSPORT_STOP ("publicTransportStop", 4, 94),
    CARRYING_DANGEROUS_GOODS ("carryingDangerousGoods", 5, 94),

    HUMAN_PROBLEM_NO_SUBCAUSE ("humanProblem", 0, 93),
    GLYCEMIA_PROBLEM ("glycemiaProblem", 1, 93),
    HEART_PROBLEM ("heartProblem", 2, 93),

    EMERGENCY_VEHICLE_APPROACHING_NO_SUBCAUSE ("emergencyVehicleApproaching", 0, 95),
    EMERGENCY_VEHICLE_APPROACHING ("emergencyVehicleApproaching", 1, 95),
    PRIORITIZED_VEHICLE_APPROACHING ("prioritizedVehicleApproaching", 2, 95),

    HAZARDOUS_LOCATION_DANGEROUS_CURVE_NO_SUBCAUSE ("dangerousCurve", 0, 96),
    DANGEROUS_LEFT_TURN_CURVE ("dangerousLeftTurnCurve", 1, 96),
    DANGEROUS_RIGHT_TURN_CURVE ("dangerousRightTurnCurve", 2, 96),
    MULTIPLE_CURVES_STARTING_WITH_UNKNOWN_TURNING_DIRECTION ("multipleCurvesStartingWithUnknownTurningDirection", 3, 96),
    MULTIPLE_CURVES_STARTING_WITH_LEFT_TURN ("multipleCurvesStartingWithLeftTurn", 4, 96),
    MULTIPLE_CURVES_STARTING_WITH_RIGHT_TURN ("multipleCurvesStartingWithRightTurn", 5, 96),

    COLLISION_RISK_NO_SUBCAUSE ("collisionRisk", 0, 97),
    LONGITUDINAL_COLLISION_RISK ("longitudinalCollisionRisk", 1, 97),
    CROSSING_COLLISION_RISK ("crossingCollisionRisk", 2, 97),
    LATERAL_COLLISION_RISK ("lateralCollisionRisk", 3, 97),
    VULNERABLE_ROAD_USER ("vulnerableRoadUser", 4, 97),

    STOP_SIGN_VIOLATION_NO_SUBCAUSE ("stopSignViolation", 0, 98),
    STOP_SIGN_VIOLATION ("stopSignViolation", 1, 98),
    TRAFFIC_LIGHT_VIOLATION ("trafficLightViolation", 2, 98),
    TURNING_REGULATION_VIOLATION ("turningRegulationViolation", 3, 98),

    DANGEROUS_SITUATION_NO_SUBCAUSE ("dangerousSituation", 0, 99),
    EMERGENCY_ELECTRONIC_BRAKE_ENGAGED ("emergencyElectronicBrakeEngaged", 1, 99),
    PRE_CRASH_SYSTEM_ENGAGED ("preCrashSystemEngaged", 2, 99),
    ESP_ENGAGED ("espEngaged", 3, 99),
    ABS_ENGAGED ("absEngaged", 4, 99),
    AEB_ENGAGED ("aebEngaged", 5, 99),
    BRAKE_WARNING_ENGAGED ("brakeWarningEngaged", 6, 99),
    COLLISION_RISK_WARNING_ENGAGED ("collisionRiskWarningEngaged", 7, 99);
	
	final String name;
	final int cause;
	final int subcause;

	private HazardType(String name, int subcause, int cause) {
		this.name = name;
		this.cause = cause;
		this.subcause = subcause;
	}
	
	public String getName() {
		return name;
	}

	public int getCause() {
		return cause;
	}

	public int getSubcause() {
		return subcause;
	}

}
