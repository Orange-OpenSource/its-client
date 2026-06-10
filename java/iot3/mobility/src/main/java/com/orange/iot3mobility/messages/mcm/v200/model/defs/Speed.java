/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * Speed value with confidence.
 *
 * @param speedValue Speed magnitude in 0.02 m/s steps [0..16383].
 * @param speedConfidence Speed confidence [1..127].
 */
public record Speed(int speedValue, int speedConfidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer speedValue;
        private Integer speedConfidence;

        private Builder() {}

        public Builder speedValue(int speedValue) {
            this.speedValue = speedValue;
            return this;
        }

        public Builder speedConfidence(int speedConfidence) {
            this.speedConfidence = speedConfidence;
            return this;
        }

        public Speed build() {
            return new Speed(
                    requireNonNull(speedValue, "speed_value"),
                    requireNonNull(speedConfidence, "speed_confidence"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

