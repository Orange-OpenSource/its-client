/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a sidewalk lane (ETSI TS 103 301 – {@code lane_type.sidewalk}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum SidewalkAttribute implements LaneTypeFlag {

    /** This sidewalk lane may be revoked by traffic management. */
    SIDEWALK_REVOCABLE_LANE("sidewalkRevocableLane"),

    /** Bicycle use is allowed on this sidewalk (note: intentional typo matches schema). */
    BICYLE_USE_ALLOWED("bicyleUseAllowed"),

    /** This sidewalk is a fly-over lane (grade separation). */
    IS_SIDEWALK_FLY_OVER_LANE("isSidewalkFlyOverLane"),

    /** Bicycles must be walked (not ridden) on this sidewalk. */
    WALK_BIKES("walkBikes");

    private final String jsonValue;

    private static final Map<String, SidewalkAttribute> BY_VALUE = new HashMap<>();

    static {
        for (SidewalkAttribute sidewalkAttribute : values()) {
            BY_VALUE.put(sidewalkAttribute.jsonValue, sidewalkAttribute);
        }
    }

    SidewalkAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link SidewalkAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static SidewalkAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

