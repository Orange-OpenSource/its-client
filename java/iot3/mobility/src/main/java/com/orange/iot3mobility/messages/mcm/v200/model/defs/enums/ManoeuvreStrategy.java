/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Manoeuvre strategy string enum as defined in MCM TS 103 561.
 */
public enum ManoeuvreStrategy {
    UNDEFINED("undefined"),
    TRANSIT_TO_HUMAN_DRIVEN_MODE("transit_to_human_driven_mode"),
    TRANSIT_TO_AUTOMATED_DRIVING_MODE("transit_to_automated_driving_mode"),
    DRIVE_STRAIGHT("drive_straight"),
    TURN_LEFT("turn_left"),
    TURN_RIGHT("turn_right"),
    U_TURN("u_turn"),
    MOVE_BACKWARD("move_backward"),
    OVERTAKE("overtake"),
    ACCELERATE("accelerate"),
    SLOW_DOWN("slow_down"),
    STOP("stop"),
    GO_TO_LEFT_LANE("go_to_left_lane"),
    GO_TO_RIGHT_LANE("go_to_right_lane"),
    GET_ON_HIGHWAY("get_on_highway"),
    EXIT_HIGHWAY("exit_highway"),
    STOP_AND_WAIT("stop_and_wait"),
    EMERGENCY_BRAKE_AND_STOP("emergency_brake_and_stop"),
    RESET_STOP_AND_RESTART_MOVING("reset_stop_and_restart_moving"),
    STAY_IN_LANE("stay_in_lane"),
    RESET_STAY_IN_LANE("reset_stay_in_lane"),
    STAY_AWAY("stay_away"),
    RESET_STAY_AWAY("reset_stay_away"),
    FOLLOW_ME("follow_me"),
    EXISTING_GROUP("existing_group"),
    TEMPORARILY_DISBAND_AN_EXISTING_GROUP("temporarily_disband_an_existing_group"),
    CONSTITUTE_A_TEMPORARILY_GROUP("constitute_a_temporarily_group"),
    DISBAND_A_TEMPORARILY_GROUP("disband_a_temporarily_group"),
    TAKE_TOLLING_LANE("take_tolling_lane");

    public final String value;

    ManoeuvreStrategy(String value) {
        this.value = value;
    }

    public static ManoeuvreStrategy fromValue(String value) {
        for (ManoeuvreStrategy strategy : values()) {
            if (strategy.value.equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown manoeuvre strategy: " + value);
    }
}

