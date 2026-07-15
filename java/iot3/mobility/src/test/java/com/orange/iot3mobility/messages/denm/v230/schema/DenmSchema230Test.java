/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v230.model.DenmEnvelope230;
import com.orange.iot3mobility.messages.denm.v230.model.DenmMessage230;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v230.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v230.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventPositionHeading;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventSpeed;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.CarryingDangerousGoods;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.ClosedLanes;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.RoadWorks;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.StationaryVehicle;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.VehicleIdentification;
import com.orange.iot3mobility.messages.denm.v230.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.DetectionZone;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.EventZone;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.SituationContainer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class DenmSchema230Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("denm/denm_schema_2-3-0.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        DenmEnvelope230 envelope = minimalEnvelope();
        DenmCodec codec = new DenmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V2_3_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        DenmEnvelope230 envelope = fullyPopulatedEnvelope();

        DenmCodec codec = new DenmCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V2_3_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static DenmEnvelope230 minimalEnvelope() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(validEventPosition())
                .stationType(5)
                .build();
        DenmMessage230 message = DenmMessage230.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        return DenmEnvelope230.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static DenmEnvelope230 fullyPopulatedEnvelope() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(1000L)
                .referenceTime(1000L)
                .termination(0)
                .eventPosition(validEventPosition())
                .awarenessDistance(0)
                .trafficDirection(0)
                .validityDuration(60)
                .transmissionInterval(1000)
                .stationType(5)
                .build();

        SituationContainer situation = SituationContainer.builder()
                .informationQuality(3)
                .eventType(new CauseCode(97, 0))
                .linkedCause(new CauseCode(1, 0))
                .eventZone(List.of(EventZone.builder()
                        .eventPosition(new DeltaReferencePosition(0, 0, 0))
                        .eventDeltaTime(100)
                        .informationQuality(1)
                        .build()))
                .linkedDenms(List.of(new ActionId(2, 1)))
                .eventEnd(100)
                .build();

        LocationContainer location = LocationContainer.builder()
                .eventSpeed(new EventSpeed(500, 1))
                .eventPositionHeading(new EventPositionHeading(900, 1))
                .detectionZonesToEventPosition(List.of(DetectionZone.builder()
                        .path(List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 10)))
                        .build()))
                .roadType(0)
                .build();

        RoadWorks roadWorks = RoadWorks.builder()
                .lightBarSirenInUse(0)
                .closedLanes(new ClosedLanes(1, 3))
                .restriction(List.of(5, 8))
                .speedLimit(50)
                .incidentIndication(new CauseCode(3, 1))
                .recommendedPath(List.of(new DeltaReferencePosition(100, 200, 0)))
                .startingPointSpeedLimit(new DeltaReferencePosition(50, 50, 0))
                .trafficFlowRule(2)
                .referenceDenms(List.of(new ActionId(10, 1)))
                .build();

        StationaryVehicle stationaryVehicle = StationaryVehicle.builder()
                .stationarySince(1)
                .stationaryCause(new CauseCode(94, 2))
                .carryingDangerousGoods(CarryingDangerousGoods.builder()
                        .dangerousGoodsType(9)
                        .unNumber(1234)
                        .elevatedTemperature(true)
                        .tunnelsRestricted(false)
                        .limitedQuantity(false)
                        .emergencyActionCode("3YE")
                        .phoneNumber("+33600000000")
                        .companyName("Orange")
                        .build())
                .numberOfOccupants(1)
                .vehicleIdentification(new VehicleIdentification("WBA", "123456"))
                .energyStorageType(4)
                .build();

        AlacarteContainer alacarte = AlacarteContainer.builder()
                .lanePosition(0)
                .roadWorks(roadWorks)
                .positioningSolution(1)
                .stationaryVehicle(stationaryVehicle)
                .build();

        DenmMessage230 message = DenmMessage230.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .locationContainer(location)
                .alacarteContainer(alacarte)
                .build();

        return DenmEnvelope230.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static ReferencePosition validEventPosition() {
        return ReferencePosition.builder()
                .latitude(0)
                .longitude(0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
    }
}
