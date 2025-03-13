/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

import java.util.Date;

/**
 * Represents a location update operation for LwM2M Location object.
 * This class encapsulates all possible location parameters that can be updated
 * and provides a builder pattern for convenient object creation.
 */
public class LocationUpdate {
    private final double latitude;
    private final double longitude;
    @Nullable
    private final Double altitude;
    @Nullable
    private final Double radius;
    @Nullable
    private final byte[] velocity;
    private final Date timestamp;
    @Nullable
    private final Double speed;

    private LocationUpdate(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.altitude = builder.altitude;
        this.radius = builder.radius;
        this.velocity = builder.velocity;
        this.speed = builder.speed;
        this.timestamp = new Date();
    }

    // Getters for all fields
    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    @Nullable
    public Double getAltitude() { return altitude; }

    @Nullable
    public Double getRadius() { return radius; }

    @Nullable
    public byte[] getVelocity() { return velocity; }

    @Nullable
    public Date getTimestamp() { return timestamp; }

    @Nullable
    public Double getSpeed() { return speed; }

    /**
     * Builder for creating LocationUpdate instances.
     * Provides a fluent interface for setting location parameters.
     */
    public static class Builder {
        private double latitude;
        private double longitude;
        @Nullable
        private Double altitude;
        @Nullable
        private Double radius;
        @Nullable
        private byte[] velocity;
        @Nullable
        private Double speed;

        /**
         * Creates a new Builder with mandatory latitude and longitude values.
         *
         * @param latitude  The latitude in degrees (-90 to 90)
         * @param longitude The longitude in degrees (-180 to 180)
         */
        public Builder(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Sets the altitude value.
         *
         * @param altitude The altitude in meters above sea level
         */
        public Builder altitude(Double altitude) {
            this.altitude = altitude;
            return this;
        }

        /**
         * Sets the radius value representing location uncertainty.
         *
         * @param radius The radius in meters of the circular area of uncertainty
         */
        public Builder radius(Double radius) {
            this.radius = radius;
            return this;
        }

        /**
         * Sets the velocity value.
         *
         * @param velocity The velocity described in 3GPP TS 23.032
         */
        public Builder velocity(byte[] velocity) {
            this.velocity = velocity;
            return this;
        }

        /**
         * Sets the speed value.
         *
         * @param speed The speed in meters per second
         */
        public Builder speed(Double speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Builds the LocationUpdate instance with the current builder values.
         *
         * @return A new LocationUpdate instance
         */
        public LocationUpdate build() {
            return new LocationUpdate(this);
        }
    }

}