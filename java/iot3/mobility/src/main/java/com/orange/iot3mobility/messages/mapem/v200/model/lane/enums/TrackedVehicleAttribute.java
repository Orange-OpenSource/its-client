/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a tracked vehicle (rail) lane (ETSI TS 103 301 – {@code lane_type.tracked_vehicle}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum TrackedVehicleAttribute implements LaneTypeFlag {

    /** This tracked vehicle lane may be revoked by traffic management. */
    SPEC_REVOCABLE_LANE("spec-RevocableLane"),

    /** Commuter rail road track. */
    SPEC_COMMUTER_RAIL_ROAD_TRACK("spec-commuterRailRoadTrack"),

    /** Light rail road track. */
    SPEC_LIGHT_RAIL_ROAD_TRACK("spec-lightRailRoadTrack"),

    /** Heavy rail road track. */
    SPEC_HEAVY_RAIL_ROAD_TRACK("spec-heavyRailRoadTrack"),

    /** Other rail type. */
    SPEC_OTHER_RAIL_TYPE("spec-otherRailType");

    private final String jsonValue;

    private static final Map<String, TrackedVehicleAttribute> BY_VALUE = new HashMap<>();

    static {
        for (TrackedVehicleAttribute trackedVehicleAttribute : values()) {
            BY_VALUE.put(trackedVehicleAttribute.jsonValue, trackedVehicleAttribute);
        }
    }

    TrackedVehicleAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link TrackedVehicleAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static TrackedVehicleAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

