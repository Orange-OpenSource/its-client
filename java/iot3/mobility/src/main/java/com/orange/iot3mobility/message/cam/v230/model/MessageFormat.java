package com.orange.iot3mobility.message.cam.v230.model;

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
