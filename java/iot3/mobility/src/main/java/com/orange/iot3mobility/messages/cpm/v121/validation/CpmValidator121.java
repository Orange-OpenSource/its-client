/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.validation;

import com.orange.iot3mobility.messages.cpm.v121.model.*;

import java.util.List;
import java.util.Objects;

public final class CpmValidator121 {

    private CpmValidator121() {}

    public static void validateEnvelope(CpmEnvelope121 env) {
        requireEquals("type", env.type(), "cam");
        requireEnum("origin", env.origin(),
                List.of("self", "global_application", "mec_application", "on_board_application"));
        requireEquals("version", env.version(), "1.2.1");
        requireNotBlank("source_uuid", env.sourceUuid());
        checkRange("timestamp", env.timestamp(), 1514764800000L, 1830297600000L);
        validateMessage(env.message());
    }

    public static void validateMessage(CpmMessage121 msg) {
        requireNonNull("message", msg);
        //TODO
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CpmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void requireEnum(String field, String actual, List<String> allowed) {
        if (!allowed.contains(actual)) {
            throw new CpmValidationException(field + " must be one of " + allowed);
        }
    }

    private static void checkRange(String field, Integer value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CpmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, Long value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CpmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }
}
