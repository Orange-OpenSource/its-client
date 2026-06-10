/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Request decline reason enum as defined in MCM TS 103 561.
 * Provides reasons for declining a manoeuvre coordination request.
 */
public enum RequestDeclineReason {
    AGREEMENT_SEEKING_UNKNOWN(0),
    ALL_UNABLE_TO_CONFORM(1),
    AGREEMENT_SEEKING_UNWANTED(2),
    ALL_UNABLE_TO_UNDERSTAND(3),
    ALL_ERRONEOUS_BEHAVIOUR_DETECTED(4),
    ALL_MISBEHAVIOUR_DETECTED(5);

    public final int value;

    RequestDeclineReason(int value) {
        this.value = value;
    }

    public static RequestDeclineReason fromValue(int value) {
        for (RequestDeclineReason reason : values()) {
            if (reason.value == value) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown request decline reason: " + value);
    }
}

