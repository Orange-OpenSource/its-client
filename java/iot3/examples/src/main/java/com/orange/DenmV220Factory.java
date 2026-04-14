package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.denm.DenmHelper;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.HazardType;

public class DenmV220Factory {
    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 123456L;
    private static final int LIFETIME = 10;
    private static final int INFO_QUALITY = 7;

    private DenmV220Factory() {
        // Factory class
    }

    static DenmEnvelope220 createTestDenmEnvelope(
            String sourceUuid,
            HazardType hazardType,
            LatLng position,
            StationType stationType) {
        return DenmEnvelope220.builder()
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(DenmMessage220.builder()
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
                        .build())
                .build();
    }
}
