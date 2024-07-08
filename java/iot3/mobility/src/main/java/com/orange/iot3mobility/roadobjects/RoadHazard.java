package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.its.HazardType;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadHazard {

    private final String uuid;
    private final int cause;
    private final int subcause;
    private LatLng position;
    private long timestamp;
    private int lifetime;
    private HazardType hazardType;
    private DENM denm;

    public RoadHazard(String uuid, int cause, int subcause, LatLng position, int lifetime, DENM denm) {
        this.uuid = uuid;
        this.cause = cause;
        this.subcause = subcause;
        this.position = position;
        this.lifetime = lifetime;
        this.denm = denm;
        updateTimestamp();
        findHazardType();
    }

    public String getUuid() {
        return uuid;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public boolean stillLiving() {
        return System.currentTimeMillis() - timestamp < lifetime;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setDenm(DENM denm) {
        this.denm = denm;
    }

    public DENM getDenm() {
        return denm;
    }

    private void findHazardType() {
        for(HazardType hazard: HazardType.values()) {
            if(hazard.getCause() == cause && hazard.getSubcause() == subcause) {
                hazardType = hazard;
                break;
            }
        }
        if(hazardType == null) hazardType = HazardType.UNDEFINED;
    }

}
