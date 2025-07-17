/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadHazard {

    private final String uuid;
    private HazardType hazardType;
    private DENM denm;

    public RoadHazard(String uuid, DENM denm) {
        this.uuid = uuid;
        this.denm = denm;
        findHazardType();
    }

    public String getUuid() {
        return uuid;
    }

    public LatLng getPosition() {
        return new LatLng(denm.getManagementContainer().getEventPosition().getLatitudeDegree(),
                denm.getManagementContainer().getEventPosition().getLongitudeDegree());
    }

    public long getTimestamp() {
        return denm.getTimestamp();
    }

    public boolean stillLiving() {
        return TrueTime.getAccurateTime() - getTimestamp() < denm.getManagementContainer().getValidityDuration() * 1000L;
    }

    public HazardType getType() {
        return hazardType;
    }

    public void setDenm(DENM denm) {
        this.denm = denm;
    }

    public DENM getDenm() {
        return denm;
    }

    private void findHazardType() {
        int cause = denm.getSituationContainer().getEventType().getCause();
        int subcause = denm.getSituationContainer().getEventType().getSubcause();
        for(HazardType hazard: HazardType.values()) {
            if(hazard.getCause() == cause && hazard.getSubcause() == subcause) {
                hazardType = hazard;
                break;
            }
        }
        if(hazardType == null) hazardType = HazardType.UNDEFINED;
    }

}
