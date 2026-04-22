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
    private SensorObjectType type;
    private LatLng position;
    private double speed; // m/s
    private double heading; // degree
    private int infoQuality;
    private long timestamp;

    public SensorObject(String uuid, SensorObjectType type, LatLng position, double speed, double heading, int infoQuality) {
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

    public SensorObjectType getType() {
        return type;
    }

    public void setType(SensorObjectType type) {
        this.type = type;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSpeedKmh() {
        return speed * 3.6;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
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
