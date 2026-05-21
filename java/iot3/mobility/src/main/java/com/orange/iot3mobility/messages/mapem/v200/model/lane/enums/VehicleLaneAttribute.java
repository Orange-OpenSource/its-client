/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for a motorized vehicle lane (ETSI TS 103 301 – {@code lane_type.vehicle}).
 * <p>
 * Implements {@link LaneTypeFlag} so instances can be stored in a {@code List<LaneTypeFlag>}.
 */
public enum VehicleLaneAttribute implements LaneTypeFlag {

    /** This lane may be revoked by traffic management. */
    IS_VEHICLE_REVOCABLE_LANE("isVehicleRevocableLane"),

    /** This lane is a fly-over lane (grade separation). */
    IS_VEHICLE_FLY_OVER_LANE("isVehicleFlyOverLane"),

    /** High Occupancy Vehicle (HOV) lane; restricted to qualifying vehicles only. */
    HOV_LANE_USE_ONLY("hovLaneUseOnly"),

    /** Lane is restricted exclusively to bus use. */
    RESTRICTED_TO_BUS_USE("restrictedToBusUse"),

    /** Lane is restricted exclusively to taxi use. */
    RESTRICTED_TO_TAXI_USE("restrictedToTaxiUse"),

    /** Lane is restricted from general public use. */
    RESTRICTED_FROM_PUBLIC_USE("restrictedFromPublicUse"),

    /** Lane is equipped with InfraRed beacon coverage. */
    HAS_IR_BEACON_COVERAGE("hasIRbeaconCoverage"),

    /** Lane access requires explicit permission. */
    PERMISSION_ON_REQUEST("permissionOnRequest");

    private final String jsonValue;

    private static final Map<String, VehicleLaneAttribute> BY_VALUE = new HashMap<>();

    static {
        for (VehicleLaneAttribute vehicleLaneAttribute : values()) {
            BY_VALUE.put(vehicleLaneAttribute.jsonValue, vehicleLaneAttribute);
        }
    }

    VehicleLaneAttribute(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link VehicleLaneAttribute}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static VehicleLaneAttribute fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

