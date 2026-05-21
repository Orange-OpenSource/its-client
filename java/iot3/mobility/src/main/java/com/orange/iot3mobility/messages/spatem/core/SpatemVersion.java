/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.core;

/**
 * Supported SPATEM JSON envelope versions.
 */
public enum SpatemVersion {
    V2_0_0("2.0.0");

    private final String jsonValue;

    SpatemVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static SpatemVersion fromJsonValue(String value) {
        for (SpatemVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new SpatemException("Unsupported SPATEM version: " + value);
    }
}

