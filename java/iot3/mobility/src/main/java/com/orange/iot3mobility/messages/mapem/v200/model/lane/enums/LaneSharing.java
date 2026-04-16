/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Other user/mode types that share a lane (ETSI TS 103 301 – {@code shared_with}).
 * <p>
 * Indicates that additional traffic of another mode may be present in the same spatial lane.
 */
public enum LaneSharing {

    /** An overlapping lane description is provided for this lane. */
    OVERLAPPING_LANE_DESCRIPTION_PROVIDED("overlappingLaneDescriptionProvided"),

    /** Multiple physical lanes are treated as a single logical lane. */
    MULTIPLE_LANES_TREATED_AS_ONE_LANE("multipleLanesTreatedAsOneLane"),

    /** Other non-motorized traffic types share this lane. */
    OTHER_NON_MOTORIZED_TRAFFIC_TYPES("otherNonMotorizedTrafficTypes"),

    /** Individual motorized vehicle traffic shares this lane. */
    INDIVIDUAL_MOTORIZED_VEHICLE_TRAFFIC("individualMotorizedVehicleTraffic"),

    /** Bus vehicle traffic shares this lane. */
    BUS_VEHICLE_TRAFFIC("busVehicleTraffic"),

    /** Taxi vehicle traffic shares this lane. */
    TAXI_VEHICLE_TRAFFIC("taxiVehicleTraffic"),

    /** Pedestrian traffic shares this lane (legacy spelling). */
    PEDESTRIANS_TRAFFIC("pedestriansTraffic"),

    /** Cyclist vehicle traffic shares this lane. */
    CYCLIST_VEHICLE_TRAFFIC("cyclistVehicleTraffic"),

    /** Tracked vehicle traffic shares this lane. */
    TRACKED_VEHICLE_TRAFFIC("trackedVehicleTraffic"),

    /** Pedestrian traffic shares this lane. */
    PEDESTRIAN_TRAFFIC("pedestrianTraffic");

    private final String jsonValue;

    private static final Map<String, LaneSharing> BY_VALUE = new HashMap<>();

    static {
        for (LaneSharing laneSharing : values()) {
            BY_VALUE.put(laneSharing.jsonValue, laneSharing);
        }
    }

    LaneSharing(String jsonValue) {
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
     * Resolve a JSON string to the corresponding {@link LaneSharing}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static LaneSharing fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

