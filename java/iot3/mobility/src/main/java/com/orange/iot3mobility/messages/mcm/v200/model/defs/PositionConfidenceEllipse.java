/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * A confidence ellipse for horizontal position accuracy (95% confidence level).
 *
 * @param semiMajorConfidence Confidence in the semi-major axis direction, in cm [0..4095].
 * @param semiMinorConfidence Confidence in the semi-minor axis direction, in cm [0..4095].
 * @param semiMajorOrientation Orientation of the semi-major axis in 0.1° steps WGS84 north [0..3601].
 */
public record PositionConfidenceEllipse(
        int semiMajorConfidence,
        int semiMinorConfidence,
        int semiMajorOrientation) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer semiMajorConfidence;
        private Integer semiMinorConfidence;
        private Integer semiMajorOrientation;

        private Builder() {}

        public Builder semiMajorConfidence(int semiMajorConfidence) {
            this.semiMajorConfidence = semiMajorConfidence;
            return this;
        }

        public Builder semiMinorConfidence(int semiMinorConfidence) {
            this.semiMinorConfidence = semiMinorConfidence;
            return this;
        }

        public Builder semiMajorOrientation(int semiMajorOrientation) {
            this.semiMajorOrientation = semiMajorOrientation;
            return this;
        }

        public PositionConfidenceEllipse build() {
            return new PositionConfidenceEllipse(
                    requireNonNull(semiMajorConfidence, "semi_major_confidence"),
                    requireNonNull(semiMinorConfidence, "semi_minor_confidence"),
                    requireNonNull(semiMajorOrientation, "semi_major_orientation"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

