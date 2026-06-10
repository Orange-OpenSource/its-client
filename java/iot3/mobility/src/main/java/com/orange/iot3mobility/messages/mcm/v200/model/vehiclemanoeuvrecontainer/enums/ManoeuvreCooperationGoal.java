/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums;

/**
 * Manoeuvre cooperation goal enum as defined in MCM TS 103 561.
 * Represents the goal for cooperation in manoeuvre coordination.
 */
public enum ManoeuvreCooperationGoal {
    VEHICLE_INTERCEPTION(0),
    ROAD_SAFETY(1),
    HUMAN_HEALTH(2),
    EMERGENCY_INTERVENTION(3),
    ROAD_OPERATOR_INTERVENTION(4),
    LOCAL_TRAFFIC_MANAGEMENT(5),
    GLOBAL_TRAFFIC_MANAGEMENT(6);

    public final int value;

    ManoeuvreCooperationGoal(int value) {
        this.value = value;
    }

    public static ManoeuvreCooperationGoal fromValue(int value) {
        for (ManoeuvreCooperationGoal goal : values()) {
            if (goal.value == value) {
                return goal;
            }
        }
        throw new IllegalArgumentException("Unknown manoeuvre cooperation goal: " + value);
    }
}

