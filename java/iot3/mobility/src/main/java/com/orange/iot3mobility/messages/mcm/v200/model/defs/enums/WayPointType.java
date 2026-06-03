/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Way point type enum as defined in MCM TS 103 561.
 * Represents the type of a single point in a trajectory.
 */
public enum WayPointType {
    STARTING_WAY_POINT(0),
    INTERMEDIATE_WAY_POINT(1),
    ENDING_WAY_POINT(2);

    public final int value;

    WayPointType(int value) {
        this.value = value;
    }

    public static WayPointType fromValue(int value) {
        for (WayPointType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown way point type: " + value);
    }
}

