/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadHazard {

    private final String uuid;
    private LatLng position;
    private long timestamp;
    private int lifetime;
    private HazardType hazardType;
    private DenmCodec.DenmFrame<?> denmFrame;

    public RoadHazard(String uuid, DenmCodec.DenmFrame<?> denmFrame) {
        this.uuid = uuid;
        setDenmFrame(denmFrame);
    }

    public String getUuid() {
        return uuid;
    }

    public LatLng getPosition() {
        return position;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean stillLiving() {
        return TrueTime.getAccurateTime() - timestamp < lifetime;
    }

    public HazardType getType() {
        return hazardType;
    }

    public void setDenmFrame(DenmCodec.DenmFrame<?> denmFrame) {
        this.denmFrame = denmFrame;
        if(denmFrame.version().equals(DenmVersion.V1_1_3)) {
            DenmEnvelope113 denmEnvelope113 = (DenmEnvelope113) denmFrame.envelope();
            DenmMessage113 denm113 = denmEnvelope113.message();
            position = new LatLng(EtsiConverter.latitudeDegrees(denm113.managementContainer().eventPosition().latitude()),
                    EtsiConverter.longitudeDegrees(denm113.managementContainer().eventPosition().longitude()));
            timestamp = denmEnvelope113.timestamp();
            lifetime = denm113.managementContainer().validityDuration() * 1000;
            hazardType = HazardType.getHazardType(denm113.situationContainer().eventType().cause(),
                    denm113.situationContainer().eventType().subcause());
        } else if(denmFrame.version().equals(DenmVersion.V2_2_0)) {
            DenmEnvelope220 denmEnvelope220 = (DenmEnvelope220) denmFrame.envelope();
            DenmMessage220 denm220 = denmEnvelope220.message();
            position = new LatLng(EtsiConverter.latitudeDegrees(denm220.managementContainer().eventPosition().latitude()),
                    EtsiConverter.longitudeDegrees(denm220.managementContainer().eventPosition().longitude()));
            timestamp = denmEnvelope220.timestamp();
            lifetime = denm220.managementContainer().validityDuration() * 1000;
            hazardType = HazardType.getHazardType(denm220.situationContainer().eventType().cause(),
                    denm220.situationContainer().eventType().subcause());
        }
    }

    public DenmCodec.DenmFrame<?> getDenmFrame() {
        return denmFrame;
    }

}
