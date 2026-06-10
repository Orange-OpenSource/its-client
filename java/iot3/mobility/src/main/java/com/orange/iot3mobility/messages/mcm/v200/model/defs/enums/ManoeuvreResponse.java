/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs.enums;

/**
 * Manoeuvre response string enum as defined in MCM TS 103 561.
 */
public enum ManoeuvreResponse {
    OFFER_ACCEPTATION("offer_acceptation"),
    OFFER_DECLINE("offer_decline"),
    REQUEST_ACCEPTATION("request_acceptation"),
    REQUEST_DECLINE("request_decline");

    public final String value;

    ManoeuvreResponse(String value) {
        this.value = value;
    }

    public static ManoeuvreResponse fromValue(String value) {
        for (ManoeuvreResponse response : values()) {
            if (response.value.equals(value)) {
                return response;
            }
        }
        throw new IllegalArgumentException("Unknown manoeuvre response: " + value);
    }
}

