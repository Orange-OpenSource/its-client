/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
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
    private final float latitude;
    private final float longitude;
    private final Float altitude;
    private final Float radius;
    private final Float speed;
    private final Date timestamp;

    private LocationUpdate(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.altitude = builder.altitude;
        this.radius = builder.radius;
        this.speed = builder.speed;
        this.timestamp = new Date();
    }

    // Getters for all fields
    public float getLatitude() { return latitude; }
    public float getLongitude() { return longitude; }
    @Nullable
    public Float getAltitude() { return altitude; }
    @Nullable
    public Float getRadius() { return radius; }
    @Nullable
    public Float getSpeed() { return speed; }
    @Nullable
    public Date getTimestamp() { return timestamp; }

    /**
     * Builder for creating LocationUpdate instances.
     * Provides a fluent interface for setting location parameters.
     */
    public static class Builder {
        private float latitude;
        private float longitude;
        @Nullable
        private Float altitude;
        @Nullable
        private Float radius;
        @Nullable
        private Float speed;


        /**
         * Creates a new Builder with mandatory latitude and longitude values.
         *
         * @param latitude  The latitude in degrees (-90 to 90)
         * @param longitude The longitude in degrees (-180 to 180)
         */
        public Builder(float latitude, float longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Sets the altitude value.
         *
         * @param altitude The altitude in meters above sea level
         */
        public Builder altitude(Float altitude) {
            this.altitude = altitude;
            return this;
        }

        /**
         * Sets the radius value representing location uncertainty.
         *
         * @param radius The radius in meters of the circular area of uncertainty
         */
        public Builder radius(Float radius) {
            this.radius = radius;
            return this;
        }

        /**
         * Sets the speed value.
         *
         * @param speed The speed in meters per second
         */
        public Builder speed(Float speed) {
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