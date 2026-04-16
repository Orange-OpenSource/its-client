/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a ground striping lane (ETSI TS 103 301 – {@code lane_type.striping}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum StripingAttribute implements LaneTypeFlag {

    /** This striping lane may be revoked by traffic management. */
    STRIPE_TO_CONNECTING_LANES_REVOCABLE_LANE("stripeToConnectingLanesRevocableLane"),

    /** The stripe is drawn on the left side. */
    STRIPE_DRAW_ON_LEFT("stripeDrawOnLeft"),

    /** The stripe is drawn on the right side. */
    STRIPE_DRAW_ON_RIGHT("stripeDrawOnRight"),

    /** The stripe connects to a lane on the left. */
    STRIPE_TO_CONNECTING_LANES_LEFT("stripeToConnectingLanesLeft"),

    /** The stripe connects to a lane on the right. */
    STRIPE_TO_CONNECTING_LANES_RIGHT("stripeToConnectingLanesRight"),

    /** The stripe connects to a lane ahead. */
    STRIPE_TO_CONNECTING_LANES_AHEAD("stripeToConnectingLanesAhead");

    private final String jsonValue;

    private static final Map<String, StripingAttribute> BY_VALUE = new HashMap<>();

    static {
        for (StripingAttribute stripingAttribute : values()) {
            BY_VALUE.put(stripingAttribute.jsonValue, stripingAttribute);
        }
    }

    StripingAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link StripingAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static StripingAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

