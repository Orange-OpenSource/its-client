/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.WayPointType;

/**
 * A single waypoint in a trajectory.
 *
 * @param wayPointType  startingWayPoint(0), intermediateWayPoint(1), endingWayPoint(2).
 * @param longitude     Longitude in 10^-7 degrees [-1800000000..1800000001].
 * @param latitude      Latitude in 10^-7 degrees [-900000000..900000001].
 * @param altitude      Optional. Altitude in 0.01 m steps [-100000..800001].
 * @param heading       Optional. Heading angle.
 * @param speed         Speed in m/s [0..511].
 */
public record WayPoint(
        int wayPointType,
        int longitude,
        int latitude,
        Integer altitude,
        Wgs84Angle heading,
        int speed) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer wayPointType;
        private Integer longitude;
        private Integer latitude;
        private Integer altitude;
        private Wgs84Angle heading;
        private Integer speed;

        private Builder() {}

        public Builder wayPointType(int wayPointType) {
            this.wayPointType = wayPointType;
            return this;
        }

        /**
         * Sets the way point type using the typed enum constant.
         *
         * @param wayPointType {@link WayPointType} value
         * @return this builder
         */
        public Builder wayPointType(WayPointType wayPointType) {
            this.wayPointType = wayPointType.value;
            return this;
        }

        public Builder longitude(int longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder latitude(int latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder altitude(Integer altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder heading(Wgs84Angle heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public WayPoint build() {
            return new WayPoint(
                    requireNonNull(wayPointType, "way_point_type"),
                    requireNonNull(longitude, "longitude"),
                    requireNonNull(latitude, "latitude"),
                    altitude,
                    heading,
                    requireNonNull(speed, "speed"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

