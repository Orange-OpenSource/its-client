/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums;

/**
 * Execution status enum as defined in MCM TS 103 561.
 * Only present in MCM type 7 (execution status) and type 4 (termination).
 */
public enum ExecutionStatus {
    STARTED(0),
    IN_PROGRESS(1),
    COMPLETED(2),
    TERMINATED(3),
    CHAINED(4);

    public final int value;

    ExecutionStatus(int value) {
        this.value = value;
    }

    public static ExecutionStatus fromValue(int value) {
        for (ExecutionStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown execution status: " + value);
    }
}

