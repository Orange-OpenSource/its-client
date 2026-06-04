/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v230.model.DenmEnvelope230;
import com.orange.iot3mobility.messages.denm.v230.model.DenmMessage230;
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
        switch (denmFrame.version()) {
            case V1_1_3 -> {
                DenmEnvelope113 denmEnvelope113 = (DenmEnvelope113) denmFrame.envelope();
                DenmMessage113 denm113 = denmEnvelope113.message();
                extractFields(denmEnvelope113.timestamp(),
                        denm113.managementContainer().eventPosition().latitude(),
                        denm113.managementContainer().eventPosition().longitude(),
                        denm113.managementContainer().validityDuration(),
                        denm113.situationContainer().eventType().cause(),
                        denm113.situationContainer().eventType().subcause());
            }
            case V2_2_0 -> {
                DenmEnvelope220 denmEnvelope220 = (DenmEnvelope220) denmFrame.envelope();
                DenmMessage220 denm220 = denmEnvelope220.message();
                extractFields(denmEnvelope220.timestamp(),
                        denm220.managementContainer().eventPosition().latitude(),
                        denm220.managementContainer().eventPosition().longitude(),
                        denm220.managementContainer().validityDuration(),
                        denm220.situationContainer().eventType().cause(),
                        denm220.situationContainer().eventType().subcause());
            }
            case V2_3_0 -> {
                DenmEnvelope230 denmEnvelope230 = (DenmEnvelope230) denmFrame.envelope();
                DenmMessage230 denm230 = denmEnvelope230.message();
                extractFields(denmEnvelope230.timestamp(),
                        denm230.managementContainer().eventPosition().latitude(),
                        denm230.managementContainer().eventPosition().longitude(),
                        denm230.managementContainer().validityDuration(),
                        denm230.situationContainer().eventType().cause(),
                        denm230.situationContainer().eventType().subcause());
            }
        }
    }

    private void extractFields(long envelopeTimestamp, int latitude, int longitude,
                               int validityDuration, int cause, int subcause) {
        position = new LatLng(EtsiConverter.latitudeDegrees(latitude),
                EtsiConverter.longitudeDegrees(longitude));
        timestamp = envelopeTimestamp;
        lifetime = validityDuration * 1000;
        hazardType = HazardType.getHazardType(cause, subcause);
    }

    public DenmCodec.DenmFrame<?> getDenmFrame() {
        return denmFrame;
    }

}
