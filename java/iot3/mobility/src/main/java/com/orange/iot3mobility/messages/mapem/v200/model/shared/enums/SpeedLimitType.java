/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.shared.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Speed limit type identifiers (DSRC schema – {@code speed_limit_list.type}).
 * <p>
 * Used in {@code SpeedLimit.type}.
 */
public enum SpeedLimitType {

    /** Type is unknown. */
    UNKNOWN("unknown"),

    /** Maximum speed in a school zone. */
    MAX_SPEED_IN_SCHOOL_ZONE("maxSpeedInSchoolZone"),

    /** Maximum speed in a school zone when children are present. */
    MAX_SPEED_IN_SCHOOL_ZONE_WHEN_CHILDREN_ARE_PRESENT("maxSpeedInSchoolZoneWhenChildrenArePresent"),

    /** Maximum speed in a construction zone. */
    MAX_SPEED_IN_CONSTRUCTION_ZONE("maxSpeedInConstructionZone"),

    /** Minimum speed for vehicles. */
    VEHICLE_MIN_SPEED("vehicleMinSpeed"),

    /** Maximum speed for vehicles. */
    VEHICLE_MAX_SPEED("vehicleMaxSpeed"),

    /** Maximum speed for vehicles at night. */
    VEHICLE_NIGHT_MAX_SPEED("vehicleNightMaxSpeed"),

    /** Minimum speed for trucks. */
    TRUCK_MIN_SPEED("truckMinSpeed"),

    /** Maximum speed for trucks. */
    TRUCK_MAX_SPEED("truckMaxSpeed"),

    /** Maximum speed for trucks at night. */
    TRUCK_NIGHT_MAX_SPEED("truckNightMaxSpeed"),

    /** Minimum speed for vehicles with trailers. */
    VEHICLES_WITH_TRAILERS_MIN_SPEED("vehiclesWithTrailersMinSpeed"),

    /** Maximum speed for vehicles with trailers. */
    VEHICLES_WITH_TRAILERS_MAX_SPEED("vehiclesWithTrailersMaxSpeed"),

    /** Maximum speed for vehicles with trailers at night. */
    VEHICLES_WITH_TRAILERS_NIGHT_MAX_SPEED("vehiclesWithTrailersNightMaxSpeed");

    private final String jsonValue;

    private static final Map<String, SpeedLimitType> BY_VALUE = new HashMap<>();

    static {
        for (SpeedLimitType speedLimitType : values()) {
            BY_VALUE.put(speedLimitType.jsonValue, speedLimitType);
        }
    }

    SpeedLimitType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the DSRC schema.
     *
     * @return the exact string used in MAPEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link SpeedLimitType}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static SpeedLimitType fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

