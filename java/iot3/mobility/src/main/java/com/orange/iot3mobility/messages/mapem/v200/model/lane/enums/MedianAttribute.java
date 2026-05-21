/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a median or barrier lane (ETSI TS 103 301 – {@code lane_type.median}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum MedianAttribute implements LaneTypeFlag {

    /** This median lane may be revoked by traffic management. */
    MEDIAN_REVOCABLE_LANE("medianRevocableLane"),

    /** A median divider is present. */
    MEDIAN("median"),

    /** White line hashing is present. */
    WHITE_LINE_HASHING("whiteLineHashing"),

    /** Single striped lines mark the median. */
    STRIPED_LINES("stripedLines"),

    /** Double striped lines mark the median. */
    DOUBLE_STRIPED_LINES("doubleStripedLines"),

    /** Traffic cones are present. */
    TRAFFIC_CONES("trafficCones"),

    /** A construction barrier is present. */
    CONSTRUCTION_BARRIER("constructionBarrier"),

    /** Traffic channel devices are present. */
    TRAFFIC_CHANNELS("trafficChannels"),

    /** Low curbs are present. */
    LOW_CURBS("lowCurbs"),

    /** High curbs are present. */
    HIGH_CURBS("highCurbs");

    private final String jsonValue;

    private static final Map<String, MedianAttribute> BY_VALUE = new HashMap<>();

    static {
        for (MedianAttribute medianAttribute : values()) {
            BY_VALUE.put(medianAttribute.jsonValue, medianAttribute);
        }
    }

    MedianAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link MedianAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static MedianAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

