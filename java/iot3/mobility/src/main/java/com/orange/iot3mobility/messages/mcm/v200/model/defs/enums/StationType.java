/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Station type enum as defined in MCM TS 103 561.
 * Represents the type of originating ITS-S.
 */
public enum StationType {
    VRU_PORTABLE_DEVICE(0),
    VEHICLE(1),
    ROADSIDE_UNIT(2),
    CENTRAL_STATION(3);

    public final int value;

    StationType(int value) {
        this.value = value;
    }

    public static StationType fromValue(int value) {
        for (StationType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown station type: " + value);
    }
}

