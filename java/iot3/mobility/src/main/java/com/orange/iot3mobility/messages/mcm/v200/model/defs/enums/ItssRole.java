/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * ITS-S role enum as defined in MCM TS 103 561.
 * The role of the originating ITS-S can only be identified once the MC is accepted by all relevant participants.
 */
public enum ItssRole {
    NOT_AVAILABLE(0),
    COORDINATING_ITSS(1),
    NOT_COORDINATING_SUBJECT_VEHICLE(2),
    TARGET_VEHICLE(3);

    public final int value;

    ItssRole(int value) {
        this.value = value;
    }

    public static ItssRole fromValue(int value) {
        for (ItssRole role : values()) {
            if (role.value == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown ITS-S role: " + value);
    }
}

