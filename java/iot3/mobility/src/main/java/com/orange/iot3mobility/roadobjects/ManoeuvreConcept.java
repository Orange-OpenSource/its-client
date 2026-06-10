/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

/**
 * Coordination concept of a manoeuvre session, derived from the {@code concept} field
 * inside {@code mcm_generic_current_state_container} (ETSI TS 103 561).
 */
public enum ManoeuvreConcept {

    /**
     * concept = 0. Participants negotiate to reach a mutually acceptable agreement.
     * Typically used between peer vehicles.
     */
    AGREEMENT_SEEKING(0),

    /**
     * concept = 1. The coordinating ITS-S prescribes the manoeuvre without negotiation.
     * Typically used by orchestrators (RSU, central station) towards subject vehicles.
     */
    PRESCRIPTIVE(1);

    /** Raw {@code concept} value from the MCM payload. */
    public final int conceptValue;

    ManoeuvreConcept(int conceptValue) {
        this.conceptValue = conceptValue;
    }

    /**
     * Resolves a {@link ManoeuvreConcept} from a raw {@code concept} integer.
     *
     * @param concept the raw value from the MCM payload [0..1]
     * @return the corresponding concept, or {@link #AGREEMENT_SEEKING} as a safe fallback
     */
    public static ManoeuvreConcept fromValue(int concept) {
        for (ManoeuvreConcept manoeuvreConcept : values()) {
            if (manoeuvreConcept.conceptValue == concept) return manoeuvreConcept;
        }
        return AGREEMENT_SEEKING; // safe fallback
    }
}

