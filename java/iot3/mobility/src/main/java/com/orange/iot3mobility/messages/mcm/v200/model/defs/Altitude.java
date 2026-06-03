/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * Altitude with confidence level.
 *
 * @param altitudeValue Altitude in WGS84, in 0.01 metre steps [-100000..800001].
 *                      negativeOutOfRange(-100000), positiveOutOfRange(800000), unavailable(800001).
 * @param altitudeConfidence Altitude confidence level [0..15]. unavailable(15).
 */
public record Altitude(int altitudeValue, int altitudeConfidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer altitudeValue;
        private Integer altitudeConfidence;

        private Builder() {}

        public Builder altitudeValue(int altitudeValue) {
            this.altitudeValue = altitudeValue;
            return this;
        }

        public Builder altitudeConfidence(int altitudeConfidence) {
            this.altitudeConfidence = altitudeConfidence;
            return this;
        }

        public Altitude build() {
            return new Altitude(
                    requireNonNull(altitudeValue, "altitude_value"),
                    requireNonNull(altitudeConfidence, "altitude_confidence"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

