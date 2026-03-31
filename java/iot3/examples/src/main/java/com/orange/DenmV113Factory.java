package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.denm.DenmHelper;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.Origin;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.EventType;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.HazardType;

public class DenmV113Factory {
    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 123456L;
    private static final int LIFETIME = 10;
    private static final int INFO_QUALITY = 7;

    private DenmV113Factory() {
        // Factory class
    }

    static DenmEnvelope113 createTestDenmEnvelope(
            String sourceUuid,
            HazardType hazardType,
            LatLng position,
            StationType stationType) {
        return DenmEnvelope113.builder()
                .origin(Origin.self.name())
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(DenmMessage113.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .managementContainer(ManagementContainer.builder()
                                .actionId(new ActionId(
                                        STATION_ID,
                                        DenmHelper.getNextSequenceNumber()))
                                .detectionTime(TrueTime.getAccurateETSITime())
                                .referenceTime(TrueTime.getAccurateETSITime())
                                .eventPosition(new ReferencePosition(
                                        EtsiConverter.latitudeEtsi(position.getLatitude()),
                                        EtsiConverter.longitudeEtsi(position.getLongitude()),
                                        0
                                ))
                                .validityDuration(LIFETIME)
                                .stationType(stationType.value)
                                .build())
                        .situationContainer(SituationContainer.builder()
                                .informationQuality(INFO_QUALITY)
                                .eventType(new EventType(
                                        hazardType.getCause(),
                                        hazardType.getSubcause()))
                                .build())
                        .build())
                .build();
    }
}
