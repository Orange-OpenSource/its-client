/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

import java.util.Date;

/**
 * A wrapper class for the LwM2M Location Object (ID: 6)
 * This implementation handles location data updates and provides
 * an easy-to-use interface for IoT3 Core users.
 */
public class Lwm2mLocation extends BaseInstanceEnabler {
    private Float latitude;
    private Float longitude;
    private Float altitude;
    private Float radius;
    private Date timestamp;
    private Float speed;

    private static final int LATITUDE_RESOURCE_ID = 0;
    private static final int LONGITUDE_RESOURCE_ID = 1;
    private static final int ALTITUDE_RESOURCE_ID = 2;
    private static final int RADIUS_RESOURCE_ID = 3;
    private static final int TIMESTAMP_RESOURCE_ID = 5;
    private static final int SPEED_RESOURCE_ID = 6;

    public Lwm2mLocation() {
        // Initialize with default values
        this.latitude = 0.0f;
        this.longitude = 0.0f;
        this.altitude = null;
        this.radius = null;
        this.timestamp = new Date();
        this.speed = null;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceId) {
        switch (resourceId) {
            case LATITUDE_RESOURCE_ID -> {
                return ReadResponse.success(resourceId, latitude);
            }
            case LONGITUDE_RESOURCE_ID -> {
                return ReadResponse.success(resourceId, longitude);
            }
            case ALTITUDE_RESOURCE_ID -> {
                return this.altitude != null
                        ? ReadResponse.success(resourceId, altitude)
                        : ReadResponse.notFound();
            }
            case RADIUS_RESOURCE_ID -> {
                return this.radius != null
                        ? ReadResponse.success(resourceId, radius)
                        : ReadResponse.notFound();
            }
            case TIMESTAMP_RESOURCE_ID -> {
                return ReadResponse.success(resourceId, timestamp);
            }
            case SPEED_RESOURCE_ID -> {
                return this.speed != null
                        ? ReadResponse.success(resourceId, speed)
                        : ReadResponse.notFound();
            }
            default -> {
                return super.read(identity, resourceId);
            }
        }
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
        switch (resourceId) {
            case LATITUDE_RESOURCE_ID -> latitude = (Float) value.getValue();
            case LONGITUDE_RESOURCE_ID -> longitude = (Float) value.getValue();
            case ALTITUDE_RESOURCE_ID -> altitude = (Float) value.getValue();
            case RADIUS_RESOURCE_ID -> radius = (Float) value.getValue();
            case TIMESTAMP_RESOURCE_ID -> timestamp = (Date) value.getValue();
            case SPEED_RESOURCE_ID -> speed = (Float) value.getValue();
            default -> {
                return super.write(identity, resourceId, value);
            }
        }
        fireResourcesChange(resourceId);
        return WriteResponse.success();
    }

    /**
     * Updates the location object with new location parameters.
     * This method updates all relevant resources and notifies the LwM2M server
     * about the changes.
     *
     * @param update The LocationUpdate object containing the new location parameters
     */
    public void updateLocation(LocationUpdate update) {
        this.latitude = update.getLatitude();
        this.longitude = update.getLongitude();
        this.altitude = update.getAltitude();
        this.radius = update.getRadius();
        this.speed = update.getSpeed();
        this.timestamp = update.getTimestamp();

        fireResourcesChange(LATITUDE_RESOURCE_ID, LONGITUDE_RESOURCE_ID, TIMESTAMP_RESOURCE_ID);
        if (update.getAltitude() != null) fireResourcesChange(ALTITUDE_RESOURCE_ID);
        if (update.getRadius() != null) fireResourcesChange(RADIUS_RESOURCE_ID);
        if (update.getSpeed() != null) fireResourcesChange(SPEED_RESOURCE_ID);
    }

    @Override
    public void onDelete(ServerIdentity identity) {
        super.onDelete(identity);
    }

}
