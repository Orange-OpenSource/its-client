/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Target Road Resource (TRR) type enum as defined in MCM TS 103 561.
 * TRR describes the geographical characteristics of a manoeuvre.
 */
public enum TrrType {
    /**
     * Type 1: Box-shaped area expressed by mainly positioning attribute.
     */
    TRR_TYPE_1(0),
    /**
     * Type 2: Box-shaped area expressed by mainly one or two vehicles surrounding the area.
     */
    TRR_TYPE_2(1),
    /**
     * Type 3: Trajectory-based area.
     */
    TRR_TYPE_3(2);

    public final int value;

    TrrType(int value) {
        this.value = value;
    }

    public static TrrType fromValue(int value) {
        for (TrrType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TRR type: " + value);
    }
}

