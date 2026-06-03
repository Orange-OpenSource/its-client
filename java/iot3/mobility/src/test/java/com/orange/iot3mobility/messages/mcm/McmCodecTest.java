/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.messages.mcm.core.McmException;
import com.orange.iot3mobility.messages.mcm.core.McmVersion;
import com.orange.iot3mobility.messages.mcm.v200.model.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.*;
import com.orange.iot3mobility.messages.mcm.v200.validation.McmValidationException;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class McmCodecTest {

    // -------------------------------------------------------------------------
    // Round-trip: write then read must produce the same values
    // -------------------------------------------------------------------------

    @Test
    void writeReadVehicleManoeuvreContainerRoundTrip() throws Exception {
        McmEnvelope200 envelope = validVehicleEnvelope();
        McmCodec codec = new McmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(McmVersion.V2_0_0, envelope, out);

        McmCodec.McmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(McmVersion.V2_0_0, frame.version());
        assertTrue(frame.envelope() instanceof McmEnvelope200);

        McmEnvelope200 parsed = (McmEnvelope200) frame.envelope();
        assertEquals("mcm", parsed.messageType());
        assertEquals("json/raw", parsed.messageFormat());
        assertEquals("test-source-uuid", parsed.sourceUuid());
        assertEquals(1_600_000_000_000L, parsed.timestamp());
        assertEquals("2.0.0", parsed.version());

        McmMessage200 parsedMessage = parsed.message();
        assertEquals(1, parsedMessage.protocolVersion());
        assertEquals(42L, parsedMessage.stationId());
        assertEquals(100, parsedMessage.generationDeltaTime());
        assertEquals(1, parsedMessage.stationType());
        assertEquals(1, parsedMessage.itssRole());

        assertNotNull(parsedMessage.position());
        assertEquals(487_417_800, parsedMessage.position().latitude());
        assertEquals(22_415_320, parsedMessage.position().longitude());

        assertNotNull(parsedMessage.mcmData().vehicleManoeuvreContainer());
        assertNull(parsedMessage.mcmData().advisedManoeuvreContainer());

        VehicleManoeuvreContainer vmc = parsedMessage.mcmData().vehicleManoeuvreContainer();
        assertEquals(0, vmc.mcmGenericCurrentStateContainer().mcmType());
        assertEquals(7, vmc.mcmGenericCurrentStateContainer().manoeuvreId());
        assertEquals(0, vmc.mcmGenericCurrentStateContainer().concept());
        assertNull(vmc.mcmGenericCurrentStateContainer().rational());
        assertNull(vmc.mcmGenericCurrentStateContainer().executionStatus());

        assertEquals(ManoeuvreStrategy.DRIVE_STRAIGHT,
                vmc.vehicleCurrentStateContainer().manoeuvreOverallStrategy());
        assertEquals(500, vmc.vehicleCurrentStateContainer().vehicleSpeed().speedValue());
        assertEquals(127, vmc.vehicleCurrentStateContainer().vehicleSpeed().speedConfidence());
        assertEquals(900, vmc.vehicleCurrentStateContainer().vehicleHeading().value());
        assertEquals(10, vmc.vehicleCurrentStateContainer().vehicleHeading().confidence());

        assertEquals(1, vmc.submaneuvres().size());
        Submanoeuvre submanoeuvre = vmc.submaneuvres().get(0);
        assertEquals(0, submanoeuvre.submanoeuvreId());
        assertEquals(0, submanoeuvre.temporalCharacteristics().trrOccupancyStartTime());
        assertEquals(5000, submanoeuvre.temporalCharacteristics().trrOccupancyEndTime());
        assertNotNull(submanoeuvre.kinematicsCharacteristics());
    }

    @Test
    void writeReadAdvisedManoeuvreContainerRoundTrip() throws Exception {
        McmEnvelope200 envelope = validAdvisedEnvelope();
        McmCodec codec = new McmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(McmVersion.V2_0_0, envelope, out);

        McmCodec.McmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(McmVersion.V2_0_0, frame.version());
        McmEnvelope200 parsed = (McmEnvelope200) frame.envelope();

        assertNull(parsed.message().mcmData().vehicleManoeuvreContainer());
        assertNotNull(parsed.message().mcmData().advisedManoeuvreContainer());
        assertEquals(1, parsed.message().mcmData().advisedManoeuvreContainer().size());

        ManoeuvreAdvice advice = parsed.message().mcmData().advisedManoeuvreContainer().get(0);
        assertEquals(99L, advice.executantId());
        assertEquals("stay_in_lane", advice.currentStateAdvisedChange());
        assertEquals(1, advice.submaneuvres().size());
        assertEquals(0, advice.submaneuvres().get(0).submanoeuvreId());
    }

    @Test
    void helperParseAndToJsonRoundTrip() throws Exception {
        McmHelper helper = new McmHelper();
        McmEnvelope200 original = validVehicleEnvelope();

        String json = helper.toJson(original);
        assertNotNull(json);
        assertTrue(json.contains("\"message_type\":\"mcm\""));
        assertTrue(json.contains("\"version\":\"2.0.0\""));

        McmCodec.McmFrame<?> frame = helper.parse(json);
        assertEquals(McmVersion.V2_0_0, frame.version());
        McmEnvelope200 parsed = (McmEnvelope200) frame.envelope();
        assertEquals(original.sourceUuid(), parsed.sourceUuid());
        assertEquals(original.timestamp(), parsed.timestamp());
    }

    @Test
    void parse200ShortcutMethod() throws Exception {
        McmHelper helper = new McmHelper();
        String json = helper.toJson(validVehicleEnvelope());
        McmEnvelope200 envelope = helper.parse200(json);
        assertEquals("test-source-uuid", envelope.sourceUuid());
    }

    // -------------------------------------------------------------------------
    // Version detection
    // -------------------------------------------------------------------------

    @Test
    void detectVersionReturnsV200() throws Exception {
        McmHelper helper = new McmHelper();
        String json = helper.toJson(validVehicleEnvelope());
        McmCodec.McmFrame<?> frame = helper.parse(json);
        assertEquals(McmVersion.V2_0_0, frame.version());
    }

    @Test
    void unknownVersionThrowsMcmException() {
        String json = "{\"message_type\":\"mcm\",\"message_format\":\"json/raw\","
                + "\"source_uuid\":\"x\",\"timestamp\":1600000000000,\"version\":\"9.9.9\","
                + "\"message\":{}}";
        McmHelper helper = new McmHelper();
        assertThrows(McmException.class, () -> helper.parse(json));
    }

    @Test
    void missingVersionFieldThrowsMcmException() {
        String json = "{\"message_type\":\"mcm\",\"source_uuid\":\"x\"}";
        McmHelper helper = new McmHelper();
        assertThrows(McmException.class, () -> helper.parse(json));
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void wrongMessageTypeThrowsValidationException() {
        // The type is hardcoded "mcm" in the builder's private constructor —
        // the only way to get a wrong type is by constructing the record directly.
        McmEnvelope200 badTypeEnvelope = new McmEnvelope200(
                "cam", // wrong type
                "json/raw", "x", 1_600_000_000_000L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class, () ->
                com.orange.iot3mobility.messages.mcm.v200.validation.McmValidator200
                        .validateEnvelope(badTypeEnvelope));
        // Verify the builder always produces the correct type
        McmEnvelope200 envelope = validVehicleEnvelope();
        assertEquals("mcm", envelope.messageType());
    }

    @Test
    void invalidTimestampThrowsValidationException() throws Exception {
        McmEnvelope200 envelope = validVehicleEnvelope();
        McmHelper helper = new McmHelper();
        String json = helper.toJson(envelope)
                .replace("\"timestamp\":" + envelope.timestamp(), "\"timestamp\":0");
        assertThrows(McmValidationException.class, () -> helper.parse(json));
    }

    @Test
    void vehicleSizeOutOfRangeThrowsValidationException() {
        // Validation happens in McmValidator200, not the builder.
        // Build a technically constructable object with out-of-range vehicleLength.
        VehicleSize badSize = new VehicleSize(0,
                null,
                new VehicleLength(1024, 2), // max is 1023
                10, 10);
        assertThrows(McmValidationException.class, () ->
                com.orange.iot3mobility.messages.mcm.v200.validation.McmValidator200
                        .validateEnvelope(validVehicleEnvelopeWithVehicleSize(badSize)));
    }

    @Test
    void missingMcmDataThrowsValidationException() {
        String json = "{\"message_type\":\"mcm\",\"message_format\":\"json/raw\","
                + "\"source_uuid\":\"x\",\"timestamp\":1600000000000,\"version\":\"2.0.0\","
                + "\"message\":{\"protocol_version\":1,\"station_id\":1,"
                + "\"generation_delta_time\":0,\"station_type\":1,\"itss_role\":0,"
                + "\"position\":{\"latitude\":487417800,\"longitude\":22415320,"
                + "\"position_confidence_ellipse\":{\"semi_major_confidence\":10,"
                + "\"semi_minor_confidence\":10,\"semi_major_orientation\":900}}}}";
        McmHelper helper = new McmHelper();
        assertThrows(Exception.class, () -> helper.parse(json));
    }

    // -------------------------------------------------------------------------
    // Builder guards
    // -------------------------------------------------------------------------

    @Test
    void builderMissingFieldThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () ->
                McmEnvelope200.builder()
                        .messageFormat("json/raw")
                        // missing sourceUuid, timestamp, message
                        .build());
    }

    @Test
    void mcmDataBothContainersPresentShouldFailValidation() {
        McmHelper helper = new McmHelper();
        // Directly construct an invalid McmData (both non-null) and verify the validator rejects it
        McmData badData = new McmData(
                validVehicleManoeuvreContainer(),
                List.of(validManoeuvreAdvice()));
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0,
                        validReferencePosition(), badData));
        assertThrows(McmValidationException.class, () -> helper.toJson(envelope));
    }

    // -------------------------------------------------------------------------
    // Builders for test fixtures
    // -------------------------------------------------------------------------

    private McmEnvelope200 validVehicleEnvelope() {
        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid("test-source-uuid")
                .timestamp(1_600_000_000_000L)
                .message(validMessage())
                .build();
    }

    private McmEnvelope200 validAdvisedEnvelope() {
        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid("test-source-uuid")
                .timestamp(1_600_000_000_000L)
                .message(McmMessage200.builder()
                        .protocolVersion(1)
                        .stationId(42L)
                        .generationDeltaTime(100)
                        .stationType(2) // RSU
                        .itssRole(0)
                        .position(validReferencePosition())
                        .mcmData(McmData.ofAdvised(List.of(validManoeuvreAdvice())))
                        .build())
                .build();
    }

    private McmMessage200 validMessage() {
        return McmMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .generationDeltaTime(100)
                .stationType(1)
                .itssRole(1)
                .position(validReferencePosition())
                .mcmData(McmData.ofVehicle(validVehicleManoeuvreContainer()))
                .build();
    }

    private ReferencePosition validReferencePosition() {
        return ReferencePosition.builder()
                .latitude(487_417_800)
                .longitude(22_415_320)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(10, 10, 900))
                .build();
    }

    private VehicleManoeuvreContainer validVehicleManoeuvreContainer() {
        return VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(McmGenericCurrentStateContainer.builder()
                        .mcmType(0)
                        .manoeuvreId(7)
                        .concept(0)
                        .build())
                .vehicleCurrentStateContainer(VehicleCurrentStateContainer.builder()
                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                        .vehicleSpeed(new Speed(500, 127))
                        .vehicleHeading(new Wgs84Angle(900, 10))
                        .vehicleSize(VehicleSize.builder()
                                .vehicleType(1)
                                .vehicleLength(VehicleLength.builder()
                                        .vehicleLengthValue(40)
                                        .vehicleLengthConfidenceIndication(0)
                                        .build())
                                .vehicleWidth(18)
                                .vehicleHeight(15)
                                .build())
                        .build())
                .submaneuvres(List.of(validSubmanoeuvre()))
                .build();
    }

    private Submanoeuvre validSubmanoeuvre() {
        return Submanoeuvre.builder()
                .submanoeuvreId(0)
                .temporalCharacteristics(new TemporalCharacteristics(0, 5000))
                .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                .build();
    }

    private ManoeuvreAdvice validManoeuvreAdvice() {
        return ManoeuvreAdvice.builder()
                .executantId(99L)
                .currentStateAdvisedChange("stay_in_lane")
                .submaneuvres(List.of(
                        AdvisedSubmanoeuvre.builder()
                                .submanoeuvreId(0)
                                .build()))
                .build();
    }

    private McmEnvelope200 validVehicleEnvelopeWithVehicleSize(VehicleSize vehicleSize) {
        return new McmEnvelope200("mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0,
                        validReferencePosition(),
                        McmData.ofVehicle(VehicleManoeuvreContainer.builder()
                                .mcmGenericCurrentStateContainer(McmGenericCurrentStateContainer.builder()
                                        .mcmType(0).manoeuvreId(0).concept(0).build())
                                .vehicleCurrentStateContainer(VehicleCurrentStateContainer.builder()
                                        .manoeuvreOverallStrategy(ManoeuvreStrategy.STOP)
                                        .vehicleSpeed(new Speed(0, 1))
                                        .vehicleHeading(new Wgs84Angle(0, 1))
                                        .vehicleSize(vehicleSize)
                                        .build())
                                .submaneuvres(List.of(validSubmanoeuvre()))
                                .build())));
    }
}

