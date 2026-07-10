/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.messages.mcm.core.McmVersion;
import com.orange.iot3mobility.messages.mcm.v200.model.McmData;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmMessage200;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.McmGenericCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Submanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleLength;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleSize;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.WayPoint;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Rational;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class McmSchema200Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("mcm/mcm_schema_2-0-0.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_vehicleManoeuvreEnvelope_conformsToSchema() throws Exception {
        McmEnvelope200 envelope = vehicleEnvelope();
        McmCodec codec = new McmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(McmVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_advisedManoeuvreEnvelope_conformsToSchema() throws Exception {
        McmEnvelope200 envelope = advisedEnvelope();
        McmCodec codec = new McmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(McmVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedVehicleEnvelope_conformsToSchema() throws Exception {
        McmGenericCurrentStateContainer genericState = McmGenericCurrentStateContainer.builder()
                .mcmType(1)
                .manoeuvreId(42)
                .concept(0)
                .rational(Rational.ofGoal(2))
                .executionStatus(1)
                .build();

        VehicleCurrentStateContainer vehicleState = VehicleCurrentStateContainer.builder()
                .manoeuvreOverallStrategy(ManoeuvreStrategy.OVERTAKE)
                .vehicleSpeed(new Speed(800, 10))
                .vehicleHeading(new Wgs84Angle(1800, 5))
                .vehicleSize(VehicleSize.builder()
                        .vehicleType(6)
                        .vehicleLength(VehicleLength.builder()
                                .vehicleLengthValue(100)
                                .vehicleLengthConfidenceIndication(1)
                                .build())
                        .vehicleWidth(20)
                        .vehicleHeight(30)
                        .build())
                .build();

        WayPoint wayPoint = WayPoint.builder()
                .wayPointType(0)
                .latitude(487_417_800)
                .longitude(22_415_320)
                .altitude(-100000)
                .heading(new Wgs84Angle(900, 10))
                .speed(30)
                .build();

        Submanoeuvre submanoeuvre = Submanoeuvre.builder()
                .submanoeuvreId(0)
                .submanoeuvreStrategy(ManoeuvreStrategy.GO_TO_LEFT_LANE)
                .referenceTrajectory(List.of(wayPoint))
                .temporalCharacteristics(new TemporalCharacteristics(0, 3000))
                .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                .build();

        ManoeuvreAdvice advice = ManoeuvreAdvice.builder()
                .executantId(4294967295L)
                .currentStateAdvisedChange("stay_in_lane")
                .submaneuvres(List.of(AdvisedSubmanoeuvre.builder().submanoeuvreId(0).build()))
                .build();

        VehicleManoeuvreContainer vmc = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(genericState)
                .vehicleCurrentStateContainer(vehicleState)
                .submaneuvres(List.of(submanoeuvre))
                .manoeuvreAdvice(List.of(advice))
                .build();

        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "com_car_42", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(2, 4294967295L, 65535, 1, 1,
                        new ReferencePosition(487_417_800, 22_415_320,
                                new PositionConfidenceEllipse(10, 10, 900), null),
                        McmData.ofVehicle(vmc)));

        McmCodec codec = new McmCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(McmVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static McmEnvelope200 vehicleEnvelope() {
        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1_600_000_000_000L)
                .message(McmMessage200.builder()
                        .protocolVersion(1)
                        .stationId(1L)
                        .generationDeltaTime(0)
                        .stationType(1)
                        .itssRole(0)
                        .position(validReferencePosition())
                        .mcmData(McmData.ofVehicle(VehicleManoeuvreContainer.builder()
                                .mcmGenericCurrentStateContainer(McmGenericCurrentStateContainer.builder()
                                        .mcmType(1)
                                        .manoeuvreId(42)
                                        .concept(0)
                                        .build())
                                .vehicleCurrentStateContainer(VehicleCurrentStateContainer.builder()
                                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                                        .vehicleSpeed(new Speed(0, 1))
                                        .vehicleHeading(new Wgs84Angle(0, 1))
                                        .vehicleSize(new VehicleSize(1, null, new VehicleLength(10, 0), 10, 10))
                                        .build())
                                .submaneuvres(List.of(new Submanoeuvre(0, null, null, null,
                                        new TemporalCharacteristics(0, 1000),
                                        KinematicsCharacteristics.INSTANCE)))
                                .build()))
                        .build())
                .build();
    }

    private static McmEnvelope200 advisedEnvelope() {
        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1_600_000_000_000L)
                .message(McmMessage200.builder()
                        .protocolVersion(1)
                        .stationId(99L)
                        .generationDeltaTime(0)
                        .stationType(2)
                        .itssRole(0)
                        .position(validReferencePosition())
                        .mcmData(McmData.ofAdvised(List.of(
                                ManoeuvreAdvice.builder()
                                        .executantId(99L)
                                        .currentStateAdvisedChange("stay_in_lane")
                                        .submaneuvres(List.of(
                                                AdvisedSubmanoeuvre.builder()
                                                        .submanoeuvreId(0)
                                                        .build()))
                                        .build())))
                        .build())
                .build();
    }

    private static ReferencePosition validReferencePosition() {
        return ReferencePosition.builder()
                .latitude(487_417_800)
                .longitude(22_415_320)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(10, 10, 900))
                .build();
    }
}
