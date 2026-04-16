/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Allowed manoeuvres from a motorized vehicle lane (DSRC schema – {@code allowed_maneuvers}).
 * <p>
 * Used in {@code GenericLane.maneuvers} and {@code ConnectingLane.maneuver}.
 */
public enum AllowedManeuver {

    /** Straight-through movement is allowed. */
    MANEUVER_STRAIGHT_ALLOWED("maneuverStraightAllowed"),

    /** Left-turn movement is allowed. */
    MANEUVER_LEFT_ALLOWED("maneuverLeftAllowed"),

    /** Right-turn movement is allowed. */
    MANEUVER_RIGHT_ALLOWED("maneuverRightAllowed"),

    /** U-turn movement is allowed. */
    MANEUVER_U_TURN_ALLOWED("maneuverUTurnAllowed"),

    /** Left turn on red is allowed. */
    MANEUVER_LEFT_TURN_ON_RED_ALLOWED("maneuverLeftTurnOnRedAllowed"),

    /** Right turn on red is allowed. */
    MANEUVER_RIGHT_TURN_ON_RED_ALLOWED("maneuverRightTurnOnRedAllowed"),

    /** Lane change manoeuvre is allowed. */
    MANEUVER_LANE_CHANGE_ALLOWED("maneuverLaneChangeAllowed"),

    /** No stopping is allowed in this lane. */
    MANEUVER_NO_STOPPING_ALLOWED("maneuverNoStoppingAllowed"),

    /** Yielding is always required. */
    YIELD_ALLWAYS_REQUIRED("yieldAllwaysRequired"),

    /** Vehicle must stop then proceed (e.g. yield sign). */
    GO_WITH_HALT("goWithHalt"),

    /** Proceed with caution. */
    CAUTION("caution");

    private final String jsonValue;

    private static final Map<String, AllowedManeuver> BY_VALUE = new HashMap<>();

    static {
        for (AllowedManeuver allowedManeuver : values()) {
            BY_VALUE.put(allowedManeuver.jsonValue, allowedManeuver);
        }
    }

    AllowedManeuver(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the DSRC schema.
     *
     * @return the exact string used in MAPEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link AllowedManeuver}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static AllowedManeuver fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

