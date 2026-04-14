/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.managementcontainer;

import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;

/**
 * ReferencePosition - event position with confidence.
 *
 * @param latitude Latitude of the geographical point. Range: -900000000 to 900000001
 * @param longitude Longitude of the geographical point. Range: -1800000000 to 1800000001
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

    public static final class Builder {
        private Integer latitude;
        private Integer longitude;
        private PositionConfidenceEllipse positionConfidenceEllipse;
        private Altitude altitude;

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
