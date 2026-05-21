/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a bicycle lane (ETSI TS 103 301 – {@code lane_type.bike_lane}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum BikeLaneAttribute implements LaneTypeFlag {

    /** This bicycle lane may be revoked by traffic management. */
    BIKE_REVOCABLE_LANE("bikeRevocableLane"),

    /** Pedestrian use is allowed on this bicycle lane. */
    PEDESTRIAN_USE_ALLOWED("pedestrianUseAllowed"),

    /** This bicycle lane is a fly-over lane (grade separation). */
    IS_BIKE_FLY_OVER_LANE("isBikeFlyOverLane"),

    /** Bicycle lane operates on a fixed cycle time. */
    FIXED_CYCLE_TIME("fixedCycleTime"),

    /** Bicycle lane operates on bi-directional cycle times. */
    BI_DIRECTIONAL_CYCLE_TIMES("biDirectionalCycleTimes"),

    /** Bicycle lane is physically isolated by a barrier. */
    ISOLATED_BY_BARRIER("isolatedByBarrier"),

    /** Unsignalized segments are present within this bicycle lane. */
    UNSIGNALIZED_SEGMENTS_PRESENT("unsignalizedSegmentsPresent");

    private final String jsonValue;

    private static final Map<String, BikeLaneAttribute> BY_VALUE = new HashMap<>();

    static {
        for (BikeLaneAttribute bikeLaneAttribute : values()) {
            BY_VALUE.put(bikeLaneAttribute.jsonValue, bikeLaneAttribute);
        }
    }

    BikeLaneAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link BikeLaneAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static BikeLaneAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

