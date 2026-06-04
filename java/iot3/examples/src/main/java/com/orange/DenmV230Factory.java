/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.denm.DenmHelper;
import com.orange.iot3mobility.messages.denm.v230.model.DenmEnvelope230;
import com.orange.iot3mobility.messages.denm.v230.model.DenmMessage230;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.RoadWorks;
import com.orange.iot3mobility.messages.denm.v230.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v230.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.HazardType;

import java.util.List;

public class DenmV230Factory {
    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 123456L;
    private static final int LIFETIME = 10;
    private static final int INFO_QUALITY = 7;

    private DenmV230Factory() {
        // Factory class
    }

    static DenmEnvelope230 createTestDenmEnvelope(
            String sourceUuid,
            HazardType hazardType,
            LatLng position,
            StationType stationType) {
        return DenmEnvelope230.builder()
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(DenmMessage230.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .managementContainer(ManagementContainer.builder()
                                .actionId(new ActionId(
                                        STATION_ID,
                                        DenmHelper.getNextSequenceNumber()))
                                .detectionTime(TrueTime.getAccurateETSITime())
                                .referenceTime(TrueTime.getAccurateETSITime())
                                .eventPosition(ReferencePosition.builder()
                                        .latitude(EtsiConverter.latitudeEtsi(position.getLatitude()))
                                        .longitude(EtsiConverter.longitudeEtsi(position.getLongitude()))
                                        .positionConfidenceEllipse(new PositionConfidenceEllipse(
                                                4095, 4095, 3601))
                                        .altitude(new Altitude(
                                                0, 15))
                                        .build())
                                .validityDuration(LIFETIME)
                                .stationType(stationType.value)
                                .build())
                        .situationContainer(SituationContainer.builder()
                                .informationQuality(INFO_QUALITY)
                                .eventType(new CauseCode(
                                        hazardType.getCause(),
                                        hazardType.getSubcause()))
                                .build())
                        .alacarteContainer(AlacarteContainer.builder()
                                .roadWorks(RoadWorks.builder()
                                        .lightBarSirenInUse(1)
                                        .speedLimit(30)
                                        .trafficFlowRule(0)
                                        .restriction(List.of(8))
                                        .build())
                                .positioningSolution(1)
                                .build())
                        .build())
                .build();
    }
}

