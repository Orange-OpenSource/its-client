/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a vehicle parking lane (ETSI TS 103 301 – {@code lane_type.parking}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum ParkingAttribute implements LaneTypeFlag {

    /** This parking lane may be revoked by traffic management. */
    PARKING_REVOCABLE_LANE("parkingRevocableLane"),

    /** Parallel parking is currently in use. */
    PARALLEL_PARKING_IN_USE("parallelParkingInUse"),

    /** Head-in parking is currently in use. */
    HEAD_IN_PARKING_IN_USE("headInParkingInUse"),

    /** This zone is a no-parking zone. */
    DO_NOT_PARK_ZONE("doNotParkZone"),

    /** Parking is for bus use only. */
    PARKING_FOR_BUS_USE("parkingForBusUse"),

    /** Parking is for taxi use only. */
    PARKING_FOR_TAXI_USE("parkingForTaxiUse"),

    /** No public parking is available. */
    NO_PUBLIC_PARKING_USE("noPublicParkingUse");

    private final String jsonValue;

    private static final Map<String, ParkingAttribute> BY_VALUE = new HashMap<>();

    static {
        for (ParkingAttribute parkingAttribute : values()) {
            BY_VALUE.put(parkingAttribute.jsonValue, parkingAttribute);
        }
    }

    ParkingAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link ParkingAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static ParkingAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

