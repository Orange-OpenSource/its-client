/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.core;

/**
 * Supported CPM JSON envelope versions.
 */
public enum CpmVersion {
    V1_2_1("1.2.1"),
    V2_1_1("2.1.1");

    private final String jsonValue;

    CpmVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static CpmVersion fromJsonValue(String value) {
        for (CpmVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new CpmException("Unsupported CPM version: " + value);
    }
}
