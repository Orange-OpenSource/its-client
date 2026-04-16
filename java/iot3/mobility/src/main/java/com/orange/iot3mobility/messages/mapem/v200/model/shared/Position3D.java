/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.shared;

/**
 * 3D geographic position used as a reference point (anchor) for offset-based lane geometry.
 *
 * @param latitude Latitude in units of 1/10th micro-degree. Range: -900000000..900000001.
 * @param longitude Longitude in units of 1/10th micro-degree. Range: -1800000000..1800000001.
 * @param elevation Optional. Elevation in decimeters above the reference ellipsoid. Range: -4096..61439.
 */
public record Position3D(int latitude, int longitude, Integer elevation) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer latitude;
        private Integer longitude;
        private Integer elevation;

        private Builder() {}

        public Builder latitude(int latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(int longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder elevation(Integer elevation) {
            this.elevation = elevation;
            return this;
        }

        public Position3D build() {
            return new Position3D(
                    requireNonNull(latitude, "latitude"),
                    requireNonNull(longitude, "longitude"),
                    elevation);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

