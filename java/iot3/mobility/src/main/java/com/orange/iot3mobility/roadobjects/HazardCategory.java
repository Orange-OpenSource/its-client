/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

public enum HazardCategory {

    UNDEFINED ("undefined", 0),
    TRAFFIC_CONDITION ("trafficCondition", 1),
    ACCIDENT ("accident", 2),
    ROADWORKS ("roadworks", 3),
    ADVERSE_WEATHER_CONDITION_ADHESION ("adverseWeatherCondition-Adhesion", 6),
    HAZARDOUS_LOCATION_SURFACE_CONDITION ("hazardousLocation-SurfaceCondition", 9),
    HAZARDOUS_LOCATION_OBSTACLE_ON_THE_ROAD ("hazardousLocation-ObstacleOnTheRoad", 10),
    HAZARDOUS_LOCATION_ANIMAL_ON_THE_ROAD ("hazardousLocation-AnimalOnTheRoad", 11),
    HUMAN_PRESENCE_ON_THE_ROAD ("humanPresenceOnTheRoad", 12),
    WRONG_WAY_OF_DRIVING ("wrongWayDriving", 14),
    RESCUE_AND_RECOVERY_WORK_IN_PROGRESS ("rescueAndRecoveryWorkInProgress", 15),
    ADVERSE_WEATHER_CONDITION_EXTREME_WEATHER_CONDITION ("adverseWeatherCondition-ExtremeWeatherCondition", 17),
    ADVERSE_WEATHER_CONDITION_VISIBILITY ("adverseWeatherCondition-Visibility", 18),
    ADVERSE_WEATHER_CONDITION_PRECIPITATION ("adverseWeatherCondition-Precipitation", 19),
    SLOW_VEHICLE ("slowVehicle", 26),
    DANGEROUS_END_OF_QUEUE ("dangerousEndOfQueue", 27),
    VEHICLE_BREAKDOWN ("vehicleBreakdown", 91),
    POST_CRASH ("postCrash", 92),
    HUMAN_PROBLEM ("humanProblem", 93),
    STATIONARY_VEHICLE ("stationaryVehicle", 94),
    EMERGENCY_VEHICLE_APPROACHING ("emergencyVehicleApproaching", 95),
    HAZARDOUS_LOCATION_DANGEROUS_CURVE ("hazardousLocation-DangerousCurve", 96),
    COLLISION_RISK ("collisionRisk", 97),
    SIGNAL_VIOLATION ("signalViolation", 98),
    DANGEROUS_SITUATION ("dangerousSituation", 99);

    final String name;
    final int cause;

    HazardCategory(String name, int cause){
        this.name = name;
        this.cause = cause;
    }

    public String getName() {
        return name;
    }

    public int getCause() {
        return cause;
    }

}
