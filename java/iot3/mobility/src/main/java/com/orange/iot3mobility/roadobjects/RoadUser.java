/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadUser {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private StationType stationType;
    private LatLng position;
    private double speed; // m/s
    private double heading; // degree
    private long timestamp;
    private CamCodec.CamFrame<?> camFrame;

    public RoadUser(String uuid, StationType stationType, LatLng position, double speed, double heading, CamCodec.CamFrame<?> camFrame) {
        this.uuid = uuid;
        this.setStationType(stationType);
        this.position = position;
        this.speed = speed;
        this.heading = heading;
        this.camFrame = camFrame;
        updateTimestamp();
    }

    public String getUuid() {
        return uuid;
    }

    public StationType getStationType() {
        return stationType;
    }

    public void setStationType(StationType stationType) {
        this.stationType = stationType;
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

    public void setCamFrame(CamCodec.CamFrame<?> camFrame) {
        this.camFrame = camFrame;
    }

    public CamCodec.CamFrame<?> getCamFrame() {
        return camFrame;
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

    public boolean isVulnerable() {
        return stationType.equals(StationType.CYCLIST) || stationType.equals(StationType.PEDESTRIAN);
    }

}
