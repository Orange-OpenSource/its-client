/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.quadkey.LatLng;

public class SensorObject {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private int type;
    private LatLng position;
    private float speed; // m/s
    private float heading; // degree
    private int infoQuality;
    private long timestamp;

    public SensorObject(String uuid, int type, LatLng position, float speed, float heading) {
        this(uuid, type, position, speed, heading, 3);
    }

    public SensorObject(String uuid, int type, LatLng position, float speed, float heading, int infoQuality) {
        this.uuid = uuid;
        this.type = type;
        this.position = position;
        this.speed = speed;
        this.heading = heading;
        this.infoQuality = infoQuality;
        updateTimestamp();
    }

    public String getUuid() {
        return uuid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSpeedKmh() {
        return speed * 3.6f;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getHeading() {
        return heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public int getInfoQuality() {
        return infoQuality;
    }

    public void setInfoQuality(int infoQuality) {
        this.infoQuality = infoQuality;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public boolean stillLiving() {
        return System.currentTimeMillis() - timestamp < LIFETIME;
    }

}
