/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * ReferencePosition
 * <p>
 * Position within a geographic coordinate system together with a confidence ellipse.
 *
 * @param latitude Latitude of the geographical point. Unit: 0,1 microdegree
 * @param longitude Longitude of the geographical point. Unit: 0,1 microdegree
 * @param positionConfidenceEllipse {@link PositionConfidenceEllipse}
 * @param altitude {@link Altitude}
 */
public record ReferencePosition(
        int latitude,
        int longitude,
        PositionConfidenceEllipse positionConfidenceEllipse,
        Altitude altitude) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ReferencePosition.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>latitude</li>
     * <li>longitude</li>
     * <li>positionConfidenceEllipse</li>
     * <li>altitude</li>
     * </ul>
     */
    public static final class Builder {
        private Integer latitude;
        private Integer longitude;
        private PositionConfidenceEllipse positionConfidenceEllipse;
        private Altitude altitude;

        private Builder() {}

        public Builder latitudeLongitude(int latitude, int longitude) {
            this.latitude = latitude;
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
                    requireNonNull(altitude, "altitude"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
