/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

/**
 * Phase of a manoeuvre coordination session, derived 1:1 from the {@code mcm_type} field
 * of the MCM message (ETSI TS 103 561).
 * <p>
 * The phase encodes where the negotiation stands in the MCS (Manoeuvre Coordination Session)
 * lifecycle. {@link #TERMINATION} and {@link #CANCELLATION} phases cause the corresponding
 * {@link ManoeuvreSession} to expire immediately upon reception.
 */
public enum ManoeuvrePhase {

    /** mcm_type = 0. Originating vehicle announces its intended manoeuvre. */
    INTENT(0),

    /** mcm_type = 1. Originating vehicle requests cooperation from others. */
    REQUEST(1),

    /** mcm_type = 2. Vehicle or orchestrator responds to a request or offer. */
    RESPONSE(2),

    /** mcm_type = 3. Vehicle reserves a road space-time slot for its manoeuvre. */
    RESERVATION(3),

    /** mcm_type = 4. Session ended normally. Triggers immediate object expiry. */
    TERMINATION(4),

    /** mcm_type = 5. Cancellation of the coordination session. Triggers immediate object expiry. */
    CANCELLATION(5),

    /** mcm_type = 6. Emergency reservation of a road space-time slot. */
    EMERGENCY_RESERVATION(6),

    /** mcm_type = 7. Ongoing status update during manoeuvre execution. */
    EXECUTION_STATUS(7),

    /** mcm_type = 8. Orchestrator (RSU / central station) proposes a manoeuvre plan. */
    OFFER(8);

    /** Raw {@code mcm_type} integer value from the MCM payload. */
    public final int mcmTypeValue;

    ManoeuvrePhase(int mcmTypeValue) {
        this.mcmTypeValue = mcmTypeValue;
    }

    /**
     * Returns {@code true} if receiving this phase should cause the
     * {@link ManoeuvreSession} to expire immediately, without waiting for the timeout.
     */
    public boolean causesImmediateExpiry() {
        return this == TERMINATION || this == CANCELLATION;
    }

    /**
     * Resolves a {@link ManoeuvrePhase} from a raw {@code mcm_type} integer.
     *
     * @param mcmType the raw value from the MCM payload [0..8]
     * @return the corresponding phase, or {@link #INTENT} as a safe fallback for unknown values
     */
    public static ManoeuvrePhase fromMcmType(int mcmType) {
        for (ManoeuvrePhase phase : values()) {
            if (phase.mcmTypeValue == mcmType) return phase;
        }
        return INTENT; // safe fallback
    }
}



