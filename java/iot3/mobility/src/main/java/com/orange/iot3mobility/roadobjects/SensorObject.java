/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.Utils;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

public class SensorObject {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private SensorObjectType type;
    private LatLng position;
    private double speed; // m/s
    private double bearing; // degree
    private int infoQuality;
    private Double length; // meter
    private Double width; // meter
    private Double orientation; // degree
    private List<LatLng> footprint;
    private long timestamp;

    public SensorObject(String uuid, SensorObjectType type, LatLng position, double speed, double bearing,
                        int infoQuality) {
        this(uuid, type, position, speed, bearing, infoQuality, null, null, null);
    }

    public SensorObject(String uuid, SensorObjectType type, LatLng position, double speed, double bearing,
                        int infoQuality, Double length, Double width, Double orientation) {
        this.uuid = uuid;
        this.type = type;
        this.position = position;
        this.speed = speed;
        this.bearing = bearing;
        this.infoQuality = infoQuality;
        this.length = length;
        this.width = width;
        this.orientation = orientation;
        computeFootprint();
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

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public int getInfoQuality() {
        return infoQuality;
    }

    public void setInfoQuality(int infoQuality) {
        this.infoQuality = infoQuality;
    }

    public void setDimensions(Double length, Double width) {
        this.length = length;
        this.width = width;
        computeFootprint();
    }

    public Double getLength() {
        return length;
    }

    public Double getWidth() {
        return width;
    }

    public Double getOrientation() {
        return orientation;
    }

    public void setOrientation(Double orientation) {
        this.orientation = orientation;
    }

    private void computeFootprint() {
        if(length != null && width != null && orientation != null) {
            LatLng frontCenter = Utils.pointFromPosition(position, orientation, length / 2);
            LatLng frontLeft = Utils.pointFromPosition(frontCenter, (orientation - 90 + 360) % 360, width / 2);
            LatLng frontRight = Utils.pointFromPosition(frontCenter, (orientation + 90 + 360) % 360, width / 2);
            LatLng rearCenter = Utils.pointFromPosition(position, (orientation + 180 + 360) % 360, length / 2);
            LatLng rearLeft = Utils.pointFromPosition(rearCenter, (orientation - 90 + 360) % 360, width / 2);
            LatLng rearRight = Utils.pointFromPosition(rearCenter, (orientation + 90 + 360) % 360, width / 2);
            footprint = List.of(frontLeft, frontRight, rearRight, rearLeft);
        }
    }

    public List<LatLng> getFootprint() {
        return footprint;
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
