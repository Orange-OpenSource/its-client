/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums;

/**
 * MCM message type enum as defined in MCM TS 103 561.
 * Represents the type of manoeuvre coordination message.
 */
public enum McmType {
    INTENT(0),
    REQUEST(1),
    RESPONSE(2),
    RESERVATION(3),
    TERMINATION(4),
    CANCELLATION_REQUEST(5),
    EMERGENCY_MANOEUVRE_RESERVATION(6),
    EXECUTION_STATUS(7),
    OFFER(8);

    public final int value;

    McmType(int value) {
        this.value = value;
    }

    public static McmType fromValue(int value) {
        for (McmType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MCM type: " + value);
    }
}

