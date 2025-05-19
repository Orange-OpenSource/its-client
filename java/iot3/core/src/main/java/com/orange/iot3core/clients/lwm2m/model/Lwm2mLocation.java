/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * A wrapper class for the LwM2M Location Object (ID: 6)
 * This implementation handles location data updates and provides
 * an easy-to-use interface for IoT3 Core users.
 */
public class Lwm2mLocation extends Lwm2mInstance {
    // 0 [R] mandatory
    private double latitude;
    // 1 [R] mandatory
    private double longitude;
    @Nullable // 2 [R] optional, in meters
    private Double altitude;
    @Nullable // 3 [R] optional, in meters
    private Double radius;
    @Nullable // 3 [R] optional
    private byte[] velocity;
    // 5 [R] mandatory
    private Date timestamp;
    @Nullable // 6 [R] optional, in m/s
    private Double speed;

    private static final int LATITUDE_RES_ID = 0;
    private static final int LONGITUDE_RES_ID = 1;
    private static final int ALTITUDE_RES_ID = 2;
    private static final int RADIUS_RES_ID = 3;
    private static final int VELOCITY_RES_ID = 4;
    private static final int TIMESTAMP_RES_ID = 5;
    private static final int SPEED_RES_ID = 6;

    public Lwm2mLocation() {
        super(ObjectId.LOCATION);
        // Initialize with default values
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.altitude = null;
        this.radius = null;
        this.velocity = null;
        this.timestamp = new Date();
        this.speed = null;
    }

    @Override
    @Nullable
    public ResponseValue read(int resourceId) {
        return switch (resourceId) {
            case LATITUDE_RES_ID -> getResponseValue(latitude, true);
            case LONGITUDE_RES_ID -> getResponseValue(longitude, true);
            case ALTITUDE_RES_ID -> getResponseValue(altitude);
            case RADIUS_RES_ID -> getResponseValue(radius);
            case VELOCITY_RES_ID -> getResponseValue(velocity);
            case TIMESTAMP_RES_ID -> getResponseValue(timestamp, true);
            case SPEED_RES_ID -> getResponseValue(speed);
            default -> null;
        };
    }

    /**
     * Updates the location object with new location parameters.
     * This method updates all relevant resources and notifies the LwM2M server
     * about the changes.
     *
     * @param update The LocationUpdate object containing the new location parameters
     */
    public void update(LocationUpdate update) {
        this.latitude = update.getLatitude();
        this.longitude = update.getLongitude();
        this.timestamp = update.getTimestamp();
        onResourcesChange(LATITUDE_RES_ID, LONGITUDE_RES_ID, TIMESTAMP_RES_ID);

        if (update.getAltitude() != null) {
            this.altitude = update.getAltitude();
            onResourcesChange(ALTITUDE_RES_ID);
        }
        if (update.getRadius() != null) {
            this.radius = update.getRadius();
            onResourcesChange(RADIUS_RES_ID);
        }
        if (update.getVelocity() != null) {
            this.velocity = update.getVelocity();
            onResourcesChange(VELOCITY_RES_ID);
        }
        if (update.getSpeed() != null) {
            this.speed = update.getSpeed();
            onResourcesChange(SPEED_RES_ID);
        }
    }

}