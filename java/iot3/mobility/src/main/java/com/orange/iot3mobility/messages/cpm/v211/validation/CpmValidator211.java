/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.validation;

import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;

import java.util.Objects;

/**
 * Static validation utility for CPM 2.1.1 envelope
 */
public final class CpmValidator211 {

    private static final long MIN_TIMESTAMP = 1514764800000L;   // 2018-01-01

    private CpmValidator211() {
    }

    public static void validateEnvelope(CpmEnvelope211 envelope) {
        requireNonNull("envelope", envelope);
        requireEquals("message_type", envelope.messageType(), "cam");
        requireNotBlank("source_uuid", envelope.sourceUuid());
        checkMin("timestamp", envelope.timestamp(), MIN_TIMESTAMP);
        requireEquals("version", envelope.version(), "2.1.1");

        //TODO
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static String requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CpmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void checkRange(String field, long value, long min, long max) {
        if (value < min || value > max) {
            throw new CpmValidationException(field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkMin(String field, long value, long min) {
        if (value < min) {
            throw new CpmValidationException(field + " inferior to min [" + min + "] (actual=" + value + ")");
        }
    }

    private static void checkStringLength(String field, String value, int min, int max) {
        requireNotBlank(field, value);
        int len = value.length();
        if (len < min || len > max) {
            throw new CpmValidationException(field + " length out of range [" + min + ", " + max + "] (actual=" + len + ")");
        }
    }
}
