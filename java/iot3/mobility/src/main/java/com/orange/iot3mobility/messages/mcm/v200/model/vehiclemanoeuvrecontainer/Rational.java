/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.ManoeuvreCooperationGoal;

/**
 * Rational associated to an offer, intent, advice or request.
 * Exactly one of {@code manoeuvreCooperationGoal} or {@code manoeuvreCooperationCost} must be non-null.
 *
 * @param manoeuvreCooperationGoal Optional. Goal motivation [0..6].
 *   vehicleInterception(0), roadSafety(1), humanHealth(2), emergencyIntervention(3),
 *   roadOperatorIntervention(4), localTrafficManagement(5), globalTrafficManagement(6).
 * @param manoeuvreCooperationCost Optional. Cost motivation [-1000..1000].
 */
public record Rational(Integer manoeuvreCooperationGoal, Integer manoeuvreCooperationCost) {

    public static Rational ofGoal(int goal) {
        return new Rational(goal, null);
    }

    /**
     * Factory for a rational with a manoeuvre cooperation goal using the typed enum constant.
     *
     * @param goal {@link ManoeuvreCooperationGoal} value
     * @return Rational with the goal set
     */
    public static Rational ofGoal(ManoeuvreCooperationGoal goal) {
        return new Rational(goal.value, null);
    }

    public static Rational ofCost(int cost) {
        return new Rational(null, cost);
    }
}

