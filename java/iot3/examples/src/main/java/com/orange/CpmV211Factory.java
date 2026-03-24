package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianCoordinateWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3d;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Circular;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.MapReference;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.RoadSegment;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.ObjectClass;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.ObjectClassVru;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.ObjectClassification;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

final class CpmV211Factory {
    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 123456L;
    private static final int SENSOR_ID = 123;
    private static final int MAP_REGION = 123;
    private static final int MAP_ROAD_SEGMENT_ID = 123;

    private CpmV211Factory() {
        // Factory class
    }

    static CpmEnvelope211 createTestCpmEnvelope(
            LatLng position,
            String sourceUuid,
            int pedestrianX,
            int pedestrianY,
            int bicycleX,
            int bicycleY) {
        PerceivedObject pedestrianPo = PerceivedObject.builder()
                .objectId(15)
                .measurementDeltaTime(0)
                .objectAge(1500)
                .position(new CartesianPosition3dWithConfidence(
                        new CartesianCoordinateWithConfidence(pedestrianX, 100),
                        new CartesianCoordinateWithConfidence(pedestrianY, 100),
                        null))
                .classification(List.of(new ObjectClassification(
                        ObjectClass.vru(ObjectClassVru.pedestrian(1)),
                        100)))
                .sensorIdList(List.of(SENSOR_ID))
                .build();

        PerceivedObject bicyclePo = PerceivedObject.builder()
                .objectId(34)
                .measurementDeltaTime(0)
                .objectAge(1500)
                .position(new CartesianPosition3dWithConfidence(
                        new CartesianCoordinateWithConfidence(bicycleX, 100),
                        new CartesianCoordinateWithConfidence(bicycleY, 100),
                        null))
                .classification(List.of(new ObjectClassification(
                        ObjectClass.vru(ObjectClassVru.bicyclistAndLightVruVehicle(1)),
                        100)))
                .sensorIdList(List.of(SENSOR_ID))
                .build();

        return CpmEnvelope211.builder()
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(CpmMessage211.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .managementContainer(ManagementContainer.builder()
                                .referenceTime(TrueTime.getAccurateETSITime())
                                .referencePosition(new ReferencePosition(
                                        EtsiConverter.latitudeEtsi(position.getLatitude()),
                                        EtsiConverter.longitudeEtsi(position.getLongitude()),
                                        new PositionConfidenceEllipse(4095, 4095, 3601),
                                        new Altitude(0, 15)))
                                .build())
                        .originatingRsuContainer(new OriginatingRsuContainer(List.of(
                                MapReference.roadSegment(new RoadSegment(MAP_ROAD_SEGMENT_ID, MAP_REGION)))))
                        .sensorInformationContainer(new SensorInformationContainer(
                                List.of(new SensorInformation(
                                        SENSOR_ID,
                                        4,
                                        Shape.circular(new Circular(
                                                200,
                                                new CartesianPosition3d(0, 0, null),
                                                null)),
                                        100,
                                        true))))
                        .perceivedObjectContainer(new PerceivedObjectContainer(List.of(pedestrianPo, bicyclePo)))
                        .build())
                .build();
    }
}

