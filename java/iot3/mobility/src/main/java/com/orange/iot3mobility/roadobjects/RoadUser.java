package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadUser {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private StationType stationType;
    private LatLng position;
    private float speed; // m/s
    private float heading; // degree
    private long timestamp;
    private CAM cam;

    public RoadUser(String uuid, StationType stationType, LatLng position, float speed, float heading, CAM cam) {
        this.uuid = uuid;
        this.setStationType(stationType);
        this.position = position;
        this.speed = speed;
        this.heading = heading;
        this.cam = cam;
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

    public void setCam(CAM cam) {
        this.cam = cam;
    }

    public CAM getCam() {
        return cam;
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
