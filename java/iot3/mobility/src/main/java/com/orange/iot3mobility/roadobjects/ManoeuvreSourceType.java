/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

/**
 * Type of ITS-S that originated a {@link ManoeuvreSession}, derived from the
 * {@code station_type} field of the MCM message.
 * <p>
 * This determines the lifetime of the road object:
 * {@link #VRU} and {@link #VEHICLE} use a 1 500 ms rolling timeout (they must keep emitting),
 * while {@link #ROADSIDE_UNIT} and {@link #CENTRAL_STATION} use a 5 000 ms timeout
 * (infrastructure nodes emit less frequently).
 */
public enum ManoeuvreSourceType {

    /** station_type = 0. Vulnerable Road User (pedestrian, cyclist…). */
    VRU(0),

    /** station_type = 1. Motor vehicle. */
    VEHICLE(1),

    /** station_type = 2. Road-side unit (infrastructure). */
    ROADSIDE_UNIT(2),

    /** station_type = 3. Central / back-office station. */
    CENTRAL_STATION(3);

    /** Raw {@code station_type} value from the MCM payload. */
    public final int stationTypeValue;

    ManoeuvreSourceType(int stationTypeValue) {
        this.stationTypeValue = stationTypeValue;
    }

    /**
     * Returns {@code true} for mobile road users (VRU or vehicle) whose objects expire
     * after {@link ManoeuvreSession#MOBILE_LIFETIME_MS} without a new MCM.
     */
    public boolean isMobile() {
        return this == VRU || this == VEHICLE;
    }

    /**
     * Resolves a {@link ManoeuvreSourceType} from a raw {@code station_type} integer.
     *
     * @param stationType the raw value from the MCM payload [0..3]
     * @return the corresponding source type, or {@link #VEHICLE} as a safe fallback
     */
    public static ManoeuvreSourceType fromStationType(int stationType) {
        for (ManoeuvreSourceType type : values()) {
            if (type.stationTypeValue == stationType) return type;
        }
        return VEHICLE; // safe fallback
    }
}



