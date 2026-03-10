/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.core;

/**
 * Supported CAM JSON envelope versions.
 */
public enum CamVersion {
    V1_1_3("1.1.3"),
    V2_3_0("2.3.0");

    private final String jsonValue;

    CamVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static CamVersion fromJsonValue(String value) {
        for (CamVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new CamException("Unsupported CAM version: " + value);
    }
}
