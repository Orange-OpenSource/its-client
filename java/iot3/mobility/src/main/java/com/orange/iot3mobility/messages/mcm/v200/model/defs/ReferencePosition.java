/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * A geographic position with a position confidence ellipse (WGS84).
 *
 * @param latitude   Latitude in 10^-7 degrees [-900000000..900000001]. unavailable(900000001).
 * @param longitude  Longitude in 10^-7 degrees [-1800000000..1800000001]. unavailable(1800000001).
 * @param positionConfidenceEllipse Horizontal position confidence ellipse (95% confidence level).
 * @param altitude   Optional. Altitude with confidence level.
 */
public record ReferencePosition(
        int latitude,
        int longitude,
        PositionConfidenceEllipse positionConfidenceEllipse,
        Altitude altitude) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer latitude;
        private Integer longitude;
        private PositionConfidenceEllipse positionConfidenceEllipse;
        private Altitude altitude;

        private Builder() {}

        public Builder latitude(int latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(int longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder positionConfidenceEllipse(PositionConfidenceEllipse positionConfidenceEllipse) {
            this.positionConfidenceEllipse = positionConfidenceEllipse;
            return this;
        }

        public Builder altitude(Altitude altitude) {
            this.altitude = altitude;
            return this;
        }

        public ReferencePosition build() {
            return new ReferencePosition(
                    requireNonNull(latitude, "latitude"),
                    requireNonNull(longitude, "longitude"),
                    requireNonNull(positionConfidenceEllipse, "position_confidence_ellipse"),
                    altitude);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

