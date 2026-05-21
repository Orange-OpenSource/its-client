/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Allowed directions of travel over a lane object (ETSI TS 103 301 – {@code directional_use}).
 * <p>
 * By convention, a lane is described from the stop line outwards. {@code INGRESS_PATH} means
 * traffic travels toward the stop line; {@code EGRESS_PATH} means traffic travels away from it.
 */
public enum LaneDirection {

    /** Traffic travels toward the intersection stop line (inbound). */
    INGRESS_PATH("ingressPath"),

    /** Traffic travels away from the intersection stop line (outbound). */
    EGRESS_PATH("egressPath");

    private final String jsonValue;

    private static final Map<String, LaneDirection> BY_VALUE = new HashMap<>();

    static {
        for (LaneDirection laneDirection : values()) {
            BY_VALUE.put(laneDirection.jsonValue, laneDirection);
        }
    }

    LaneDirection(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the ETSI TS 103 301 schema.
     *
     * @return the exact string used in MAPEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link LaneDirection}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static LaneDirection fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

