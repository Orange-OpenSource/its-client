/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

/**
 * Signal phase states as defined in ETSI TS 103 301 / SAE J2735 MovementPhaseState.
 * <p>
 * Each constant carries its ETSI integer value ({@link #etsiValue()}), a simplified
 * {@link SignalColor} ({@link #color()}), and a blinking flag ({@link #isBlinking()}).
 *
 * <pre>
 *  Value │ ETSI name                       │ Color   │ Blinking
 *  ──────┼─────────────────────────────────┼─────────┼─────────
 *    0   │ unavailable                     │ UNKNOWN │ false
 *    1   │ dark                            │ UNKNOWN │ false
 *    2   │ stop-Then-Proceed               │ RED     │ true
 *    3   │ stop-And-Remain                 │ RED     │ false
 *    4   │ pre-Movement                    │ UNKNOWN │ false
 *    5   │ permissive-Movement-Allowed     │ GREEN   │ false
 *    6   │ protected-Movement-Allowed      │ GREEN   │ false
 *    7   │ permissive-clearance            │ YELLOW  │ false
 *    8   │ protected-clearance             │ YELLOW  │ false
 *    9   │ caution-Conflicting-Traffic     │ YELLOW  │ true
 * </pre>
 */
public enum SignalPhase {

    /** State 0 – signal state is not available. */
    UNAVAILABLE(0, SignalColor.UNKNOWN, false),

    /** State 1 – signal head is turned off (dark). */
    DARK(1, SignalColor.UNKNOWN, false),

    /**
     * State 2 – <b>flashing red</b>: vehicles must stop, then may proceed when safe
     * (e.g. failed signal controller).
     */
    STOP_THEN_PROCEED(2, SignalColor.RED, true),

    /** State 3 – solid red: vehicles must stop and remain stopped. */
    STOP_AND_REMAIN(3, SignalColor.RED, false),

    /**
     * State 4 – pre-movement phase: signal is about to transition to a go phase
     * (used in some European deployments, e.g. red+yellow before green).
     */
    PRE_MOVEMENT(4, SignalColor.UNKNOWN, false),

    /**
     * State 5 – permissive green: movement is allowed but subject to conflict
     * (e.g. unprotected left turn on green).
     */
    PERMISSIVE_MOVEMENT_ALLOWED(5, SignalColor.GREEN, false),

    /**
     * State 6 – protected green: movement is fully protected
     * (e.g. dedicated green arrow).
     */
    PROTECTED_MOVEMENT_ALLOWED(6, SignalColor.GREEN, false),

    /**
     * State 7 – permissive clearance (yellow): phase is ending for a permissive movement;
     * vehicles should clear the intersection.
     */
    PERMISSIVE_CLEARANCE(7, SignalColor.YELLOW, false),

    /**
     * State 8 – protected clearance (yellow): phase is ending for a protected movement;
     * vehicles should clear the intersection.
     */
    PROTECTED_CLEARANCE(8, SignalColor.YELLOW, false),

    /**
     * State 9 – <b>flashing yellow</b> / caution: conflicting traffic present; proceed with
     * extreme caution (e.g. flashing yellow or end-of-green with pedestrians in the path).
     */
    CAUTION_CONFLICTING_TRAFFIC(9, SignalColor.YELLOW, true);

    private final int etsiValue;
    private final SignalColor color;
    private final boolean blinking;

    SignalPhase(int etsiValue, SignalColor color, boolean blinking) {
        this.etsiValue = etsiValue;
        this.color = color;
        this.blinking = blinking;
    }

    /** ETSI integer value of this phase (0–9). */
    public int etsiValue() {
        return etsiValue;
    }

    /** Simplified traffic-light color for this phase. */
    public SignalColor color() {
        return color;
    }

    /**
     * Whether this phase is a blinking/flashing state.
     * <p>
     * {@code true} for {@link #STOP_THEN_PROCEED} (flashing red) and
     * {@link #CAUTION_CONFLICTING_TRAFFIC} (flashing yellow).
     */
    public boolean isBlinking() {
        return blinking;
    }

    /**
     * Convert an ETSI {@code event_state} integer to the corresponding {@link SignalPhase}.
     *
     * @param eventState the raw ETSI event_state value [0..9]
     * @return the matching {@link SignalPhase}, or {@link #UNAVAILABLE} if the value is out of range
     */
    public static SignalPhase fromEtsiValue(int eventState) {
        for (SignalPhase phase : values()) {
            if (phase.etsiValue == eventState) return phase;
        }
        return UNAVAILABLE;
    }
}

