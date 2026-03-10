/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model;

/**
 * MessageFormat v2.3.0
 * <p>
 * json/raw or asn1/uper
 */
public enum MessageFormat {
    JSON_RAW("json/raw"),
    ASN1_UPER("asn1/uper");

    public final String value;

    MessageFormat(String value) {
        this.value = value;
    }
}
