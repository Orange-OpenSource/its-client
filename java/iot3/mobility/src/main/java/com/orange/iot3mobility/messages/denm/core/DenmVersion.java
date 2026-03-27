/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.core;

/**
 * Supported DENM JSON envelope versions.
 */
public enum DenmVersion {
    V1_1_3("1.1.3"),
    V2_2_0("2.2.0");

    private final String jsonValue;

    DenmVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static DenmVersion fromJsonValue(String value) {
        for (DenmVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new DenmException("Unsupported DENM version: " + value);
    }
}

