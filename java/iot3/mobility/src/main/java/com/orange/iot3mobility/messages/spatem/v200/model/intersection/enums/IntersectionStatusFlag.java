/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * General status flags of an intersection controller (ETSI TS 103 301 – SPATEM {@code status}).
 * <p>
 * Used in {@code IntersectionState.status}.
 */
public enum IntersectionStatusFlag {

    /** Manual control of the signal controller is currently enabled. */
    MANUAL_CONTROL_IS_ENABLED("manualControlIsEnabled"),

    /** Stop time is currently activated at this intersection. */
    STOP_TIME_IS_ACTIVATED("stopTimeIsActivated"),

    /** The controller is in failure flash mode. */
    FAILURE_FLASH("failureFlash"),

    /** A signal pre-emption is currently active. */
    PREEMPT_IS_ACTIVE("preemptIsActive"),

    /** Signal priority is currently active. */
    SIGNAL_PRIORITY_IS_ACTIVE("signalPriorityIsActive"),

    /** The controller is operating on fixed time plans. */
    FIXED_TIME_OPERATION("fixedTimeOperation"),

    /** The controller is operating in traffic-dependent mode. */
    TRAFFIC_DEPENDENT_OPERATION("trafficDependentOperation"),

    /** The controller is in standby (reduced operation) mode. */
    STANDBY_OPERATION("standbyOperation"),

    /** The controller is in failure mode. */
    FAILURE_MODE("failureMode"),

    /** The signal is turned off. */
    OFF("off"),

    /** A recent MAP message update has been received. */
    RECENT_MAP_MESSAGE_UPDATE("recentMAPmessageUpdate"),

    /** Lane IDs used in MAP assignments have recently changed. */
    RECENT_CHANGE_IN_MAP_ASSIGNED_LANES_IDS_USED("recentChangeInMAPassignedLanesIDsUsed"),

    /** No valid MAP is available at this time. */
    NO_VALID_MAP_IS_AVAILABLE_AT_THIS_TIME("noValidMAPisAvailableAtThisTime"),

    /** No valid SPAT is available at this time. */
    NO_VALID_SPAT_IS_AVAILABLE_AT_THIS_TIME("noValidSPATisAvailableAtThisTime");

    private final String jsonValue;

    private static final Map<String, IntersectionStatusFlag> BY_VALUE = new HashMap<>();

    static {
        for (IntersectionStatusFlag intersectionStatusFlag : values()) {
            BY_VALUE.put(intersectionStatusFlag.jsonValue, intersectionStatusFlag);
        }
    }

    IntersectionStatusFlag(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the ETSI TS 103 301 SPATEM schema.
     *
     * @return the exact string used in SPATEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link IntersectionStatusFlag}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a SPATEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static IntersectionStatusFlag fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

