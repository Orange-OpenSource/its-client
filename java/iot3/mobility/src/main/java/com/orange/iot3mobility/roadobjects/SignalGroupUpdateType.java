/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.managers.IoT3SignalControllerCallback;

/**
 * Describes what changed in a {@link SignalGroup} update delivered via
 * {@link IoT3SignalControllerCallback#signalGroupUpdated}.
 */
public enum SignalGroupUpdateType {

    /** Only the signal phase (event state) changed. */
    PHASE,

    /** Only the timing information (minEndTime / maxEndTime) changed. */
    TIMING,

    /** Both the signal phase and the timing information changed. */
    PHASE_AND_TIMING,

    /**
     * The stop-line position was resolved for the first time from MAPEM data.
     * The phase and timing are unchanged.
     */
    POSITION
}

