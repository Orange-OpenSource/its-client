package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.Origin;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.Offset;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClass;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClassVru;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClassification;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObjectConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.DetectionArea;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

final class CpmV121Factory {
    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 123456L;
    private static final int SENSOR_ID = 123;
    private static final int MAP_REGION = 123;
    private static final int MAP_INTERSECTION_ID = 123;
    private static final int MAP_ROAD_SEGMENT_ID = 123;

    private CpmV121Factory() {
        // Factory class
    }

    static CpmEnvelope121 createTestCpmEnvelope(
            LatLng position,
            String sourceUuid,
            int pedestrianX,
            int pedestrianY,
            int bicycleX,
            int bicycleY) {
        PerceivedObject pedestrianPo = PerceivedObject.builder()
                .objectId(15)
                .timeOfMeasurement(0)
                .objectAge(1500)
                .distance(pedestrianX, pedestrianY)
                .speed(0, 0)
                .planarObjectDimension(10, 10)
                .verticalObjectDimension(20)
                .classification(List.of(new ObjectClassification(
                        new ObjectClass(null,
                                new ObjectClassVru(1, null, null, null),
                                null,
                                null),
                        100)))
                .sensorIdList(List.of(SENSOR_ID))
                .confidence(
                        PerceivedObjectConfidence.builder()
                                .object(15)
                                .distance(4095, 4095)
                                .speed(0, 0)
                                .build())
                .build();

        PerceivedObject bicyclePo = PerceivedObject.builder()
                .objectId(34)
                .timeOfMeasurement(0)
                .objectAge(1500)
                .distance(bicycleX, bicycleY)
                .speed(0, 0)
                .planarObjectDimension(20, 20)
                .verticalObjectDimension(15)
                .classification(List.of(new ObjectClassification(
                        new ObjectClass(null,
                                new ObjectClassVru(null, 1, null, null),
                                null,
                                null),
                        100)))
                .sensorIdList(List.of(SENSOR_ID))
                .confidence(
                        PerceivedObjectConfidence.builder()
                                .object(15)
                                .distance(4095, 4095)
                                .speed(0, 0)
                                .build())
                .build();

        return CpmEnvelope121.builder()
                .origin(Origin.SELF.value)
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(CpmMessage121.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .generationDeltaTime(EtsiConverter.generationDeltaTimeEtsi(TrueTime.getAccurateETSITime()))
                        .managementContainer(ManagementContainer.builder()
                                .stationType(com.orange.iot3mobility.messages.StationType.ROAD_SIDE_UNIT.value)
                                .referencePosition(new ReferencePosition(
                                        EtsiConverter.latitudeEtsi(position.getLatitude()),
                                        EtsiConverter.longitudeEtsi(position.getLongitude()),
                                        0))
                                .confidence(new ManagementConfidence(
                                        new com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer
                                                .PositionConfidenceEllipse(4095, 4095, 3601),
                                        15))
                                .build())
                        .stationDataContainer(StationDataContainer.builder()
                                .originatingRsuContainer(new OriginatingRsuContainer(
                                        MAP_REGION,
                                        MAP_INTERSECTION_ID,
                                        MAP_ROAD_SEGMENT_ID))
                                .build())
                        .sensorInformationContainer(new SensorInformationContainer(
                                List.of(new SensorInformation(
                                        SENSOR_ID,
                                        4,
                                        DetectionArea.builder()
                                                .stationarySensorCircular(new AreaCircular(
                                                        new Offset(0, 0, 0), 200))
                                                .build()))))
                        .perceivedObjectContainer(new PerceivedObjectContainer(List.of(pedestrianPo, bicyclePo)))
                        .build())
                .build();
    }
}

