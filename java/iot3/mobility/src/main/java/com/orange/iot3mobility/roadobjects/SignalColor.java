/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

/**
 * Simplified traffic-light color derived from a signal phase.
 * <p>
 * Intended as a quick, human-readable indication for SDK consumers who do not need the
 * full ETSI phase granularity. Always use {@link SignalPhase} for precise signal-state
 * semantics (e.g. distinguishing permissive from protected movements).
 */
public enum SignalColor {

    /** The signal is showing some shade of green — movement is allowed. */
    GREEN,

    /** The signal is showing yellow/amber — clearance or caution. */
    YELLOW,

    /** The signal is showing red — stop required or imminent. */
    RED,

    /**
     * The color cannot be determined from the phase state
     * (e.g. the signal is dark, unavailable, or in a pre-movement phase).
     */
    UNKNOWN
}

