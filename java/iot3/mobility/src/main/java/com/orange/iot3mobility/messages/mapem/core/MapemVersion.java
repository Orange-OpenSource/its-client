/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.core;

/**
 * Supported MAPEM JSON envelope versions.
 */
public enum MapemVersion {
    V2_0_0("2.0.0");

    private final String jsonValue;

    MapemVersion(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String jsonValue() {
        return jsonValue;
    }

    public static MapemVersion fromJsonValue(String value) {
        for (MapemVersion version : values()) {
            if (version.jsonValue.equals(value)) {
                return version;
            }
        }
        throw new MapemException("Unsupported MAPEM version: " + value);
    }
}

