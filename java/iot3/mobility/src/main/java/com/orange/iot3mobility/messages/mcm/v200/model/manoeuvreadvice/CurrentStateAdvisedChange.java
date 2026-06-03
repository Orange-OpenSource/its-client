/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice;

/**
 * Current state advised change string enum as defined in MCM TS 103 561.
 */
public enum CurrentStateAdvisedChange {
    TRANSIT_TO_HUMAN_DRIVING_MODE("transit_to_human_driving_mode"),
    TRANSIT_TO_AUTOMATED_DRIVING_MODE("transit_to_automated_driving_mode"),
    LEAVE_GROUP("leave_group"),
    EMERGENCY_BRAKE_TRIGGERING("emergency_brake_triggering"),
    STAY_IN_LANE("stay_in_lane"),
    STOP("stop"),
    RESET_STOP("reset_stop"),
    RESET_STAY_IN_LANE("reset_stay_in_lane"),
    STAY_AWAY_OF_VEHICLE_WITH_STATION_ID("stay_away_of_vehicle_sith_station_id"),
    RESET_STAY_AWAY_OF_VEHICLE("reset_stay_away_of_vehicle"),
    FOLLOW_ME_WITH_MIN_TIME_INTER_DISTANCE("follow_me_with_min_time_inter_distance"),
    JOIN_GROUP("join_group");

    public final String value;

    CurrentStateAdvisedChange(String value) {
        this.value = value;
    }

    public static CurrentStateAdvisedChange fromValue(String value) {
        for (CurrentStateAdvisedChange change : values()) {
            if (change.value.equals(value)) {
                return change;
            }
        }
        throw new IllegalArgumentException("Unknown current state advised change: " + value);
    }
}

