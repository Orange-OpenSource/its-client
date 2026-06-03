/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * Angular value with confidence (WGS84), e.g. heading.
 *
 * @param value Angle in 0.1° steps WGS84 north [0..3601]. unavailable(3601).
 * @param confidence Angle confidence [1..127].
 */
public record Wgs84Angle(int value, int confidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer value;
        private Integer confidence;

        private Builder() {}

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Builder confidence(int confidence) {
            this.confidence = confidence;
            return this;
        }

        public Wgs84Angle build() {
            return new Wgs84Angle(
                    requireNonNull(value, "value"),
                    requireNonNull(confidence, "confidence"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

