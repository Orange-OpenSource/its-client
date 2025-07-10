/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

public enum HazardType {
	
    UNDEFINED("roadHazard", 0, HazardCategory.UNDEFINED),

    TRAFFIC_CONDITION_NO_SUBCAUSE ("trafficJam", 0, HazardCategory.TRAFFIC_CONDITION),
    INCREASED_VOLUME_OF_TRAFFIC ("increasedVolumeOfTraffic", 1, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_SLOWLY_INCREASING ("trafficJamSlowlyIncreasing", 2, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_INCREASING ("trafficJamIncreasing", 3, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_STRONGLY_INCREASING("trafficJamStronglyIncreasing", 4, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_STATIONARY ("trafficStationary", 5, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_SLIGHTLY_DECREASING ("trafficJamSlightlyDecreasing", 6, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_DECREASING ("trafficJamDecreasing", 7, HazardCategory.TRAFFIC_CONDITION),
    TRAFFIC_JAM_STRONGLY_DECREASING ("trafficJamStronglyDecreasing", 8, HazardCategory.TRAFFIC_CONDITION),

    ACCIDENT_NO_SUBCAUSE ("accident", 0, HazardCategory.ACCIDENT),
    MULTI_VEHICLE_ACCIDENT ("multiVehicleAccident", 1, HazardCategory.ACCIDENT),
    HEAVY_ACCIDENT ("heavyAccident", 2, HazardCategory.ACCIDENT),
    ACCIDENT_INVOLVING_LORRY ("accidentInvolvingLorry", 3, HazardCategory.ACCIDENT),
    ACCIDENT_INVOLVING_BUS ("accidentInvolvingBus", 4, HazardCategory.ACCIDENT),
    ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS ("accidentInvolvingHazardousMaterials", 5, HazardCategory.ACCIDENT),
    ACCIDENT_ON_OPPOSITE_LANE ("accidentOnOppositeLane", 6, HazardCategory.ACCIDENT),
    UNSECURED_ACCIDENT ("unsecuredAccident", 7, HazardCategory.ACCIDENT),
    ASSISTANCE_REQUESTED ("assistanceRequested", 8, HazardCategory.ACCIDENT),

    ROADWORKS_NO_SUBCAUSE ("roadworks", 0, HazardCategory.ROADWORKS),
    MAJOR_ROADWORKS ("majorRoadworks", 1, HazardCategory.ROADWORKS),
    ROAD_MARKING_WORK ("roadMarkingWork", 2, HazardCategory.ROADWORKS),
    SLOW_MOVING_ROAD_MAINTENANCE ("slowMovingRoadMaintenance", 3, HazardCategory.ROADWORKS),
    SHORT_TERM_STATIONARY_ROADWORKS ("shortTermStationaryRoadworks", 4, HazardCategory.ROADWORKS),
    STREET_CLEANING ("streetCleaning", 5, HazardCategory.ROADWORKS),
    WINTER_SERVICE ("winterService", 6, HazardCategory.ROADWORKS),
    
    ADVERSE_WEATHER_CONDITION_ADHESION_NO_SUBCAUSE ("slipperyRoad", 0, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    HEAVY_FROST_ON_ROAD ("heavyFrostOnRoad", 1, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    FUEL_ON_ROAD ("fuelOnRoad", 2, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    MUD_ON_ROAD ("mudOnRoad", 3, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    SNOW_ON_ROAD ("snowOnRoad", 4, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    ICE_ON_ROAD ("iceOnRoad", 5, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    BLACK_ICE_ON_ROAD ("blackIceOnRoad", 6, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    OIL_ON_ROAD ("oilOnRoad", 7, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    LOOSE_CHIPPINGS ("looseChippings", 8, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    INSTANT_BLACK_ICE ("instantBlackIce", 9, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    ROADS_SALTED ("roadsSalted", 10, HazardCategory.ADVERSE_WEATHER_CONDITION_ADHESION),
    
    HAZARDOUS_LOCATION_SURFACE_CONDITION_NO_SUBCAUSE ("hazardousSurfaceCondition", 0, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    ROCKFALLS ("rockfalls", 1, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    EARTHQUAKE_DAMAGE ("earthquakeDamage", 2, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    SEWER_COLLAPSE ("sewerCollapse", 3, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    SUBSIDENCE ("subsidence", 4, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    SNOW_DRIFTS ("snowDrifts", 5, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    STORM_DAMAGE ("stormDamage", 6, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    BURST_PIPE ("burstPipe", 7, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    VOLCANO_ERUPTION ("volcanoEruption", 8, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),
    FALLING_ICE ("fallingIce", 9, HazardCategory.HAZARDOUS_LOCATION_SURFACE_CONDITION),

    HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD_NO_SUBCAUSE ("hazardousObstacleOnTheRoad", 0, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    SHED_LOAD ("shedLoad", 1, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    PARTS_OF_VEHICLES ("partsOfVehicles", 2, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    PARTS_OF_TYRES ("partsOfTyres", 3, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    BIG_OBJECTS ("bigObjects", 4, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    FALLEN_TREES ("fallenTrees", 5, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    HUB_CAPS ("hubCaps", 6, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),
    WAITING_VEHICLES ("waitingVehicles", 7, HazardCategory.HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD),

    HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD_NO_SUBCAUSE ("animalOnTheRoad", 0, HazardCategory.HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD),
    WILD_ANIMALS ("wildAnimals", 1, HazardCategory.HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD),
    HERD_OF_ANIMALS ("herdOfAnimals", 2, HazardCategory.HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD),
    SMALL_ANIMALS ("smallAnimals", 3, HazardCategory.HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD),
    LARGE_ANIMALS ("largeAnimals", 4, HazardCategory.HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD),

    HUMAN_PRESENCE_ON_THE_ROAD_NO_SUBCAUSE ("humanPresenceOnTheRoad", 0, HazardCategory.HUMAN_PRESENCE_ON_THE_ROAD),
    CHILDREN_ON_ROADWAY ("childrenOnRoadway", 1, HazardCategory.HUMAN_PRESENCE_ON_THE_ROAD),
    CYCLIST_ON_ROADWAY ("cyclistOnRoadway", 2, HazardCategory.HUMAN_PRESENCE_ON_THE_ROAD),
    MOTORCYCLIST_ON_ROADWAY ("motorcyclistOnRoadway", 3, HazardCategory.HUMAN_PRESENCE_ON_THE_ROAD),
    SCOOTER_ON_ROADWAY ("scooterOnRoadway", 4, HazardCategory.HUMAN_PRESENCE_ON_THE_ROAD),

    WRONG_WAY_OF_DRIVING_NO_SUBCAUSE ("wrongWayOfDriving", 0, HazardCategory.WRONG_WAY_OF_DRIVING),
    WRONG_LANE ("wrongLane", 1, HazardCategory.WRONG_WAY_OF_DRIVING),
    WRONG_DIRECTION ("wrongDirection", 2, HazardCategory.WRONG_WAY_OF_DRIVING),
    
    RESCUE_AND_RECOVERY_WORK_IN_PROGRESS_NO_SUBCAUSE ("rescueAndRecoveryWorkInProgress", 0, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),
    EMERGENCY_VEHICLES ("emergencyVehicles", 1, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),
    RESCUE_HELICOPTER_LANDING ("rescueHelicopterLanding", 2, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),
    POLICE_ACTIVITY_ONGOING ("policeActivityOngoing", 3, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),
    MEDICAL_EMERGENCY_ONGOING ("medicalEmergencyOngoing", 4, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),
    CHILD_ABDUCTION_IN_PROGRESS ("childAbductionInProgress", 5, HazardCategory.RESCUE_AND_RECOVERY_WORK_IN_PROGRESS),

    ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION_NO_SUBCAUSE ("extremeWeatherCondition", 0, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    STRONG_WINDS ("strongWinds", 1, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    DAMAGING_HAIL ("damagingHail", 2, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    HURRICANE ("hurricane", 3, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    THUNDERSTORM ("thunderstorm", 4, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    TORNADO ("tornado", 5, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),
    BLIZZARD ("blizzard", 6, HazardCategory.ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION),

    ADVERSE_WEATHER_CONDITION_VISIBILITY_NO_SUBCAUSE ("badVisibility", 0, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    FOG ("fog", 1, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    SMOKE ("smoke", 2, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    HEAVY_SNOWFALL ("heavySnowfall", 3, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    HEAVY_RAIN ("heavyRain", 4, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    HEAVY_HAIL ("heavyHail", 5, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    LOW_SUN_GLARE ("lowSunGlare", 6, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    SANDSTORMS ("sandstorms", 7, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),
    SWARMS_OF_INSECTS ("swarmsOfInsects", 8, HazardCategory.ADVERSE_WEATHER_CONDITION_VISIBILITY),

    HEAVY_RAIN_PRECIPITATION_NO_SUBCAUSE ("heavyPrecipitation", 0, HazardCategory.ADVERSE_WEATHER_CONDITION_PRECIPITATION),
    HEAVY_RAIN_PRECIPITATION ("heavyRain", 1, HazardCategory.ADVERSE_WEATHER_CONDITION_PRECIPITATION),
    HEAVY_SNOWFALL_PRECIPITATION ("heavySnowfall", 2, HazardCategory.ADVERSE_WEATHER_CONDITION_PRECIPITATION),
    SOFT_HAIL ("softHail", 3, HazardCategory.ADVERSE_WEATHER_CONDITION_PRECIPITATION),

    SLOW_VEHICLE_NO_SUBCAUSE ("slowVehicle", 0, HazardCategory.SLOW_VEHICLE),
    MAINTENANCE_VEHICLE ("maintenanceVehicle", 1, HazardCategory.SLOW_VEHICLE),
    VEHICLES_SLOWING_TO_LOOK_AT_ACCIDENT ("vehiclesSlowingToLookAtAccident", 2, HazardCategory.SLOW_VEHICLE),
    ABNORMAL_LOAD ("abnormalLoad", 3, HazardCategory.SLOW_VEHICLE),
    ABNORMAL_WIDE_LOAD ("abnormalWideLoad", 4, HazardCategory.SLOW_VEHICLE),
    CONVOY ("convoy", 5, HazardCategory.SLOW_VEHICLE),
    SNOWPLOUGH ("snowplough", 6, HazardCategory.SLOW_VEHICLE),
    DEICING ("deicing", 7, HazardCategory.SLOW_VEHICLE),
    SALTING_VEHICLES ("saltingVehicles", 8, HazardCategory.SLOW_VEHICLE),
    
    DANGEROUS_END_OF_QUEUE_NO_SUBCAUSE ("dangerousEndOfQueue", 0, HazardCategory.DANGEROUS_END_OF_QUEUE),
    SUDDEN_END_OF_QUEUE ("suddenEndOfQueue", 1, HazardCategory.DANGEROUS_END_OF_QUEUE),
    QUEUE_OVER_HILL ("queueOverHill", 2, HazardCategory.DANGEROUS_END_OF_QUEUE),
    QUEUE_AROUND_BEND ("queueAroundBend", 3, HazardCategory.DANGEROUS_END_OF_QUEUE),
    QUEUE_IN_TUNNEL ("queueInTunnel", 4, HazardCategory.DANGEROUS_END_OF_QUEUE),

    VEHICLE_BREAKDOWN_NO_SUBCAUSE("vehicleBreakdown", 0, HazardCategory.VEHICLE_BREAKDOWN),
    LACK_OF_FUEL ("lackOfFuel", 1, HazardCategory.VEHICLE_BREAKDOWN),
    LACK_OF_BATTERY_POWER ("lackOfBatteryPower", 2, HazardCategory.VEHICLE_BREAKDOWN),
    ENGINE_PROBLEM ("engineProblem", 3, HazardCategory.VEHICLE_BREAKDOWN),
    TRANSMISSION_PROBLEM ("transmissionProblem", 4, HazardCategory.VEHICLE_BREAKDOWN),
    ENGINE_COOLING_PROBLEM ("engineCoolingProblem", 5, HazardCategory.VEHICLE_BREAKDOWN),
    BRAKING_SYSTEM_PROBLEM ("brakingSystemProblem", 6, HazardCategory.VEHICLE_BREAKDOWN),
    STEERING_PROBLEM ("steeringProblem", 7, HazardCategory.VEHICLE_BREAKDOWN),
    TYRE_PUNCTURE ("tyrePuncture", 8, HazardCategory.VEHICLE_BREAKDOWN),
    
    POST_CRASH_NO_SUBCAUSE ("postCrash", 0, HazardCategory.POST_CRASH),
    ACCIDENT_WITHOUT_E_CALL_TRIGGERED ("accidentWithoutECallTriggered", 1, HazardCategory.POST_CRASH),
    ACCIDENT_WITH_E_CALL_MANUALLY_TRIGGERED ("accidentWithECallManuallyTriggered", 2, HazardCategory.POST_CRASH),
    ACCIDENT_WITH_E_CALL_AUTOMATICALLY_TRIGGERED ("accidentWithECallAutomaticallyTriggered", 3, HazardCategory.POST_CRASH),
    ACCIDENT_WITH_E_CALL_TRIGGERED_WITHOUT_ACCESS_TO_CELLULAR_NETWORK ("accidentWithECallTriggeredWithoutAccessToCellularNetwork", 4, HazardCategory.POST_CRASH),

    HUMAN_PROBLEM_NO_SUBCAUSE ("humanProblem", 0, HazardCategory.HUMAN_PROBLEM),
    GLYCEMIA_PROBLEM ("glycemiaProblem", 1, HazardCategory.HUMAN_PROBLEM),
    HEART_PROBLEM ("heartProblem", 2, HazardCategory.HUMAN_PROBLEM),
    
    STATIONARY_VEHICLE_NO_SUBCAUSE ("stationaryVehicle", 0, HazardCategory.STATIONARY_VEHICLE),
    HUMAN_PROBLEM ("humanProblem", 1, HazardCategory.STATIONARY_VEHICLE),
    VEHICLE_BREAKDOWN ("vehicleBreakdown", 2, HazardCategory.STATIONARY_VEHICLE),
    POST_CRASH ("postCrash", 3, HazardCategory.STATIONARY_VEHICLE),
    PUBLIC_TRANSPORT_STOP ("publicTransportStop", 4, HazardCategory.STATIONARY_VEHICLE),
    CARRYING_DANGEROUS_GOODS ("carryingDangerousGoods", 5, HazardCategory.STATIONARY_VEHICLE),

    EMERGENCY_VEHICLE_APPROACHING_NO_SUBCAUSE ("emergencyVehicleApproaching", 0, HazardCategory.EMERGENCY_VEHICLE_APPROACHING),
    EMERGENCY_VEHICLE_APPROACHING ("emergencyVehicleApproaching", 1, HazardCategory.EMERGENCY_VEHICLE_APPROACHING),
    PRIORITIZED_VEHICLE_APPROACHING ("prioritizedVehicleApproaching", 2, HazardCategory.EMERGENCY_VEHICLE_APPROACHING),

    HAZARDOUS_LOCATION_DANGEROUS_CURVE_NO_SUBCAUSE ("dangerousCurve", 0, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),
    DANGEROUS_LEFT_TURN_CURVE ("dangerousLeftTurnCurve", 1, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),
    DANGEROUS_RIGHT_TURN_CURVE ("dangerousRightTurnCurve", 2, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),
    MULTIPLE_CURVES_STARTING_WITH_UNKNOWN_TURNING_DIRECTION ("multipleCurvesStartingWithUnknownTurningDirection", 3, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),
    MULTIPLE_CURVES_STARTING_WITH_LEFT_TURN ("multipleCurvesStartingWithLeftTurn", 4, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),
    MULTIPLE_CURVES_STARTING_WITH_RIGHT_TURN ("multipleCurvesStartingWithRightTurn", 5, HazardCategory.HAZARDOUS_LOCATION_DANGEROUS_CURVE),

    COLLISION_RISK_NO_SUBCAUSE ("collisionRisk", 0, HazardCategory.COLLISION_RISK),
    LONGITUDINAL_COLLISION_RISK ("longitudinalCollisionRisk", 1, HazardCategory.COLLISION_RISK),
    CROSSING_COLLISION_RISK ("crossingCollisionRisk", 2, HazardCategory.COLLISION_RISK),
    LATERAL_COLLISION_RISK ("lateralCollisionRisk", 3, HazardCategory.COLLISION_RISK),
    VULNERABLE_ROAD_USER ("vulnerableRoadUser", 4, HazardCategory.COLLISION_RISK),

    STOP_SIGN_VIOLATION_NO_SUBCAUSE ("stopSignViolation", 0, HazardCategory.SIGNAL_VIOLATION),
    STOP_SIGN_VIOLATION ("stopSignViolation", 1, HazardCategory.SIGNAL_VIOLATION),
    TRAFFIC_LIGHT_VIOLATION ("trafficLightViolation", 2, HazardCategory.SIGNAL_VIOLATION),
    TURNING_REGULATION_VIOLATION ("turningRegulationViolation", 3, HazardCategory.SIGNAL_VIOLATION),

    DANGEROUS_SITUATION_NO_SUBCAUSE ("dangerousSituation", 0, HazardCategory.DANGEROUS_SITUATION),
    EMERGENCY_ELECTRONIC_BRAKE_ENGAGED ("emergencyElectronicBrakeEngaged", 1, HazardCategory.DANGEROUS_SITUATION),
    PRE_CRASH_SYSTEM_ENGAGED ("preCrashSystemEngaged", 2, HazardCategory.DANGEROUS_SITUATION),
    ESP_ENGAGED ("espEngaged", 3, HazardCategory.DANGEROUS_SITUATION),
    ABS_ENGAGED ("absEngaged", 4, HazardCategory.DANGEROUS_SITUATION),
    AEB_ENGAGED ("aebEngaged", 5, HazardCategory.DANGEROUS_SITUATION),
    BRAKE_WARNING_ENGAGED ("brakeWarningEngaged", 6, HazardCategory.DANGEROUS_SITUATION),
    COLLISION_RISK_WARNING_ENGAGED ("collisionRiskWarningEngaged", 7, HazardCategory.DANGEROUS_SITUATION);

    final String name;
    final HazardCategory hazardCategory;
    final int subcause;

    HazardType(String name, int subcause, HazardCategory hazardCategory) {
        this.name = name;
        this.hazardCategory = hazardCategory;
        this.subcause = subcause;
    }

    public String getName() {
        return name;
    }

    public HazardCategory getCategory() {
        return hazardCategory;
    }

    public int getCause() {
        return hazardCategory.getCause();
    }

    public int getSubcause() {
        return subcause;
    }

}
