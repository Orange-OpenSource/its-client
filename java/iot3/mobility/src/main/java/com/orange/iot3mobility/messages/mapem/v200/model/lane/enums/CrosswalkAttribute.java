/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a crosswalk lane (ETSI TS 103 301 – {@code lane_type.crosswalk}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum CrosswalkAttribute implements LaneTypeFlag {

    /** This crosswalk lane may be revoked by traffic management. */
    CROSSWALK_REVOCABLE_LANE("crosswalkRevocableLane"),

    /** Bicycle use is allowed on this crosswalk (note: intentional typo matches schema). */
    BICYLE_USE_ALLOWED("bicyleUseAllowed"),

    /** This crosswalk is a fly-over lane (grade separation). */
    IS_XWALK_FLY_OVER_LANE("isXwalkFlyOverLane"),

    /** Crosswalk operates on a fixed cycle time. */
    FIXED_CYCLE_TIME("fixedCycleTime"),

    /** Crosswalk operates on bi-directional cycle times. */
    BI_DIRECTIONAL_CYCLE_TIMES("biDirectionalCycleTimes"),

    /** A push-to-walk button is present at this crosswalk. */
    HAS_PUSH_TO_WALK_BUTTON("hasPushToWalkButton"),

    /** Audio signaling support is present. */
    AUDIO_SUPPORT("audioSupport"),

    /** RF signal request equipment is present. */
    RF_SIGNAL_REQUEST_PRESENT("rfSignalRequestPresent"),

    /** Unsignalized segments are present within this crosswalk. */
    UNSIGNALIZED_SEGMENTS_PRESENT("unsignalizedSegmentsPresent");

    private final String jsonValue;

    private static final Map<String, CrosswalkAttribute> BY_VALUE = new HashMap<>();

    static {
        for (CrosswalkAttribute crosswalkAttribute : values()) {
            BY_VALUE.put(crosswalkAttribute.jsonValue, crosswalkAttribute);
        }
    }

    CrosswalkAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link CrosswalkAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static CrosswalkAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

