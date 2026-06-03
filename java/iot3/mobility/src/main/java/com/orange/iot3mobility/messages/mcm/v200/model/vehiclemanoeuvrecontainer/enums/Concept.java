/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums;

/**
 * Manoeuvre coordination concept enum as defined in MCM TS 103 561.
 */
public enum Concept {
    AGREEMENT_SEEKING(0),
    PRESCRIPTIVE(1);

    public final int value;

    Concept(int value) {
        this.value = value;
    }

    public static Concept fromValue(int value) {
        for (Concept concept : values()) {
            if (concept.value == value) {
                return concept;
            }
        }
        throw new IllegalArgumentException("Unknown concept: " + value);
    }
}

