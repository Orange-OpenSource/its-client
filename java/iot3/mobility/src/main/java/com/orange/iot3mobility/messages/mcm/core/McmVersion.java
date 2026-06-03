/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.core;

/**
 * Supported MCM JSON envelope versions.
 */
public enum McmVersion {
    V2_0_0("2.0.0");

    private final String jsonValue;

    McmVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static McmVersion fromJsonValue(String value) {
        for (McmVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new McmException("Unsupported MCM version: " + value);
    }
}

