/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums;

/**
 * Vehicle length confidence indication enum as defined in MCM TS 103 561.
 * Provides information about the presence of a trailer.
 */
public enum VehicleLengthConfidenceIndication {
    NO_TRAILER_PRESENT(0),
    TRAILER_PRESENT_WITH_KNOWN_LENGTH(1),
    TRAILER_PRESENT_WITH_UNKNOWN_LENGTH(2),
    TRAILER_PRESENCE_IS_UNKNOWN(3),
    UNAVAILABLE(4);

    public final int value;

    VehicleLengthConfidenceIndication(int value) {
        this.value = value;
    }

    public static VehicleLengthConfidenceIndication fromValue(int value) {
        for (VehicleLengthConfidenceIndication indication : values()) {
            if (indication.value == value) {
                return indication;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle length confidence indication: " + value);
    }
}

