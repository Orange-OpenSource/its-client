/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.validation;

import com.orange.iot3mobility.messages.mcm.v200.model.McmData;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmMessage200;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Altitude;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.KinematicsCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Speed;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TemporalCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TrrDescription;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.WayPoint;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Wgs84Angle;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedTrrContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.McmGenericCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Rational;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Submanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleLength;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleSize;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class McmValidator200Test {

    // -------------------------------------------------------------------------
    // Minimal valid envelope
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(validVehicleEnvelope()));
    }

    // -------------------------------------------------------------------------
    // Fully populated valid envelope
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeAcceptsFullyPopulatedVehicleMessage() {
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

        TrrDescription trrDescription = TrrDescription.builder()
                .trrType(2)
                .laneCount(2)
                .startingLaneNumber(1)
                .endingLaneNumber(2)
                .waypoints(List.of(wayPoint))
                .trrWidth(3)
                .trrLength(500)
                .build();

        Submanoeuvre submanoeuvre = Submanoeuvre.builder()
                .submanoeuvreId(0)
                .submanoeuvreStrategy(ManoeuvreStrategy.GO_TO_LEFT_LANE)
                .referenceTrajectory(List.of(wayPoint))
                .targetRoadResourceIContainer(trrDescription)
                .temporalCharacteristics(new TemporalCharacteristics(0, 3000))
                .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                .build();

        ManoeuvreAdvice advice = ManoeuvreAdvice.builder()
                .executantId(4294967295L)
                .currentStateAdvisedChange("stay_in_lane")
                .submaneuvres(List.of(
                        AdvisedSubmanoeuvre.builder()
                                .submanoeuvreId(0)
                                .advisedTrajectory(List.of(wayPoint))
                                .advisedTargetRoadResource(AdvisedTrrContainer.builder()
                                        .trrDescription(trrDescription)
                                        .temporalCharacteristics(new TemporalCharacteristics(0, 1000))
                                        .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                                        .build())
                                .build()))
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
                        validReferencePositionWithAltitude(),
                        McmData.ofVehicle(vmc)));

        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeAcceptsAdvisedManoeuvreContainer() {
        ManoeuvreAdvice advice = ManoeuvreAdvice.builder()
                .executantId(99L)
                .currentStateAdvisedChange("follow_me_with_min_time_inter_distance")
                .submaneuvres(List.of(AdvisedSubmanoeuvre.builder().submanoeuvreId(0).build()))
                .build();

        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "com_rsu_1", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 2, 0,
                        validReferencePosition(),
                        McmData.ofAdvised(List.of(advice))));

        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Envelope-level rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsWrongMessageType() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "cam", "json/raw", "x", 1_600_000_000_000L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsInvalidMessageFormat() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "xml", "x", 1_600_000_000_000L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsBlankSourceUuid() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "  ", 1_600_000_000_000L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsTimestampTooLow() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 0L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsTimestampTooHigh() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 9_999_999_999_999L, "2.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsWrongVersion() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "1.0.0", validMessage());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Message-level rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsProtocolVersionOutOfRange() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(256, 1L, 0, 1, 0, validReferencePosition(), validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsStationIdOutOfRange() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 4294967296L, 0, 1, 0, validReferencePosition(), validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsGenerationDeltaTimeOutOfRange() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 65536, 1, 0, validReferencePosition(), validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsStationTypeOutOfRange() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 4, 0, validReferencePosition(), validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsItssRoleOutOfRange() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 4, validReferencePosition(), validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Position rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsLatitudeOutOfRange() {
        ReferencePosition badPosition = new ReferencePosition(
                900000002, 0, new PositionConfidenceEllipse(0, 0, 0), null);
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, badPosition, validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsLongitudeOutOfRange() {
        ReferencePosition badPosition = new ReferencePosition(
                0, 1800000002, new PositionConfidenceEllipse(0, 0, 0), null);
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, badPosition, validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsSemiMajorConfidenceOutOfRange() {
        ReferencePosition badPosition = new ReferencePosition(
                0, 0, new PositionConfidenceEllipse(4096, 0, 0), null);
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, badPosition, validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsAltitudeValueOutOfRange() {
        ReferencePosition badPosition = new ReferencePosition(
                0, 0, new PositionConfidenceEllipse(0, 0, 0), new Altitude(800002, 0));
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, badPosition, validMcmData()));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // McmData rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsMcmDataWithNoContainer() {
        McmData emptyData = new McmData(null, null);
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, validReferencePosition(), emptyData));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsMcmDataWithBothContainers() {
        McmData bothData = new McmData(validVehicleManoeuvreContainer(), List.of(validManoeuvreAdvice()));
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0, validReferencePosition(), bothData));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Generic state rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsMcmTypeOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(9, 0, 0, null, null));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsManoeuvreIdOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 256, 0, null, null));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsConceptOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 0, 2, null, null));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsRationalGoalOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 0, 0, Rational.ofGoal(7), null));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsRationalCostOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 0, 0, Rational.ofCost(1001), null));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsExecutionStatusOutOfRange() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 0, 0, null, 5));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Vehicle current state rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsSpeedValueOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleState(
                VehicleCurrentStateContainer.builder()
                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                        .vehicleSpeed(new Speed(16384, 1))
                        .vehicleHeading(new Wgs84Angle(0, 1))
                        .vehicleSize(validVehicleSize())
                        .build());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsSpeedConfidenceOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleState(
                VehicleCurrentStateContainer.builder()
                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                        .vehicleSpeed(new Speed(0, 128))
                        .vehicleHeading(new Wgs84Angle(0, 1))
                        .vehicleSize(validVehicleSize())
                        .build());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsHeadingValueOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleState(
                VehicleCurrentStateContainer.builder()
                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                        .vehicleSpeed(new Speed(0, 1))
                        .vehicleHeading(new Wgs84Angle(3602, 1))
                        .vehicleSize(validVehicleSize())
                        .build());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsHeadingConfidenceOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleState(
                VehicleCurrentStateContainer.builder()
                        .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                        .vehicleSpeed(new Speed(0, 1))
                        .vehicleHeading(new Wgs84Angle(0, 128))
                        .vehicleSize(validVehicleSize())
                        .build());
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Vehicle size rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsVehicleTypeOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(256, null, new VehicleLength(1, 0), 1, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleLengthValueOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(1024, 0), 1, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleLengthValueBelowMinimum() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(0, 0), 1, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleLengthConfidenceOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(1, 5), 1, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleWidthOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(1, 0), 63, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleWidthBelowMinimum() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(1, 0), 0, 1));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsVehicleHeightOutOfRange() {
        McmEnvelope200 envelope = envelopeWithVehicleSize(
                new VehicleSize(0, null, new VehicleLength(1, 0), 1, 129));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Submanoeuvre rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsSubmanoeuvreIdOutOfRange() {
        Submanoeuvre badSubmanoeuvre = new Submanoeuvre(
                256, null, null, null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(badSubmanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsTemporalCharacteristicsStartTimeOutOfRange() {
        Submanoeuvre badSubmanoeuvre = new Submanoeuvre(
                0, null, null, null,
                new TemporalCharacteristics(65536, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(badSubmanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsEmptyReferenceTrajectory() {
        Submanoeuvre badSubmanoeuvre = new Submanoeuvre(
                0, null, List.of(), null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(badSubmanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsReferenceTrajectoryExceedingMaxSize() {
        List<WayPoint> tooManyWayPoints = List.of(
                validWayPoint(), validWayPoint(), validWayPoint(), validWayPoint(), validWayPoint(),
                validWayPoint(), validWayPoint(), validWayPoint(), validWayPoint(), validWayPoint(),
                validWayPoint());
        Submanoeuvre badSubmanoeuvre = new Submanoeuvre(
                0, null, tooManyWayPoints, null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(badSubmanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsWayPointTypeOutOfRange() {
        WayPoint badWayPoint = new WayPoint(3, 0, 0, null, null, 0);
        Submanoeuvre submanoeuvre = new Submanoeuvre(
                0, null, List.of(badWayPoint), null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(submanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsWayPointSpeedOutOfRange() {
        WayPoint badWayPoint = new WayPoint(0, 0, 0, null, null, 512);
        Submanoeuvre submanoeuvre = new Submanoeuvre(
                0, null, List.of(badWayPoint), null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
        McmEnvelope200 envelope = envelopeWithSubmaneuvres(List.of(submanoeuvre));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Manoeuvre advice rejections
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeRejectsUnknownCurrentStateAdvisedChange() {
        ManoeuvreAdvice badAdvice = new ManoeuvreAdvice(
                1L, "unknown_action", List.of());
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0,
                        validReferencePosition(),
                        McmData.ofAdvised(List.of(badAdvice))));
        assertThrows(McmValidationException.class,
                () -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Boundary: valid edge values accepted
    // -------------------------------------------------------------------------

    @Test
    void validateEnvelopeAcceptsTimestampAtLowerBound() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1514764800000L, "2.0.0", validMessage());
        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeAcceptsTimestampAtUpperBound() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "json/raw", "x", 1830297600000L, "2.0.0", validMessage());
        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeAcceptsRationalWithNegativeCost() {
        McmEnvelope200 envelope = envelopeWithGenericState(
                new McmGenericCurrentStateContainer(0, 0, 0, Rational.ofCost(-1000), null));
        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeAcceptsAllValidCurrentStateAdvisedChanges() {
        List<String> validChanges = List.of(
                "transit_to_human_driving_mode",
                "transit_to_automated_driving_mode",
                "leave_group",
                "emergency_brake_triggering",
                "stay_in_lane",
                "stop",
                "reset_stop",
                "reset_stay_in_lane",
                "stay_away_of_vehicle_sith_station_id",
                "reset_stay_away_of_vehicle",
                "follow_me_with_min_time_inter_distance",
                "join_group");

        for (String change : validChanges) {
            ManoeuvreAdvice advice = new ManoeuvreAdvice(1L, change, List.of());
            McmEnvelope200 envelope = new McmEnvelope200(
                    "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                    new McmMessage200(1, 1L, 0, 1, 0,
                            validReferencePosition(),
                            McmData.ofAdvised(List.of(advice))));
            assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope),
                    "Should accept advised change: " + change);
        }
    }

    @Test
    void validateEnvelopeAcceptsAsn1UperMessageFormat() {
        McmEnvelope200 envelope = new McmEnvelope200(
                "mcm", "asn1/uper", "x", 1_600_000_000_000L, "2.0.0", validMessage());
        assertDoesNotThrow(() -> McmValidator200.validateEnvelope(envelope));
    }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static McmEnvelope200 validVehicleEnvelope() {
        return new McmEnvelope200(
                "mcm", "json/raw", "com_car_42", 1_600_000_000_000L, "2.0.0", validMessage());
    }

    private static McmMessage200 validMessage() {
        return new McmMessage200(1, 1L, 0, 1, 0,
                validReferencePosition(), validMcmData());
    }

    private static ReferencePosition validReferencePosition() {
        return new ReferencePosition(487_417_800, 22_415_320,
                new PositionConfidenceEllipse(10, 10, 900), null);
    }

    private static ReferencePosition validReferencePositionWithAltitude() {
        return new ReferencePosition(487_417_800, 22_415_320,
                new PositionConfidenceEllipse(10, 10, 900),
                new Altitude(0, 15));
    }

    private static McmData validMcmData() {
        return McmData.ofVehicle(validVehicleManoeuvreContainer());
    }

    private static VehicleManoeuvreContainer validVehicleManoeuvreContainer() {
        return VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(validGenericState())
                .vehicleCurrentStateContainer(validVehicleCurrentState())
                .submaneuvres(List.of(validSubmanoeuvre()))
                .build();
    }

    private static McmGenericCurrentStateContainer validGenericState() {
        return McmGenericCurrentStateContainer.builder()
                .mcmType(0)
                .manoeuvreId(0)
                .concept(0)
                .build();
    }

    private static VehicleCurrentStateContainer validVehicleCurrentState() {
        return VehicleCurrentStateContainer.builder()
                .manoeuvreOverallStrategy(ManoeuvreStrategy.DRIVE_STRAIGHT)
                .vehicleSpeed(new Speed(0, 1))
                .vehicleHeading(new Wgs84Angle(0, 1))
                .vehicleSize(validVehicleSize())
                .build();
    }

    private static VehicleSize validVehicleSize() {
        return new VehicleSize(1, null, new VehicleLength(10, 0), 10, 10);
    }

    private static Submanoeuvre validSubmanoeuvre() {
        return new Submanoeuvre(0, null, null, null,
                new TemporalCharacteristics(0, 1000),
                KinematicsCharacteristics.INSTANCE);
    }

    private static WayPoint validWayPoint() {
        return new WayPoint(0, 0, 0, null, null, 0);
    }

    private static ManoeuvreAdvice validManoeuvreAdvice() {
        return new ManoeuvreAdvice(1L, "stay_in_lane", List.of());
    }

    // ---- helpers for targeted envelope construction ----

    private static McmEnvelope200 envelopeWithGenericState(McmGenericCurrentStateContainer state) {
        VehicleManoeuvreContainer vmc = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(state)
                .vehicleCurrentStateContainer(validVehicleCurrentState())
                .submaneuvres(List.of(validSubmanoeuvre()))
                .build();
        return envelopeWithVmc(vmc);
    }

    private static McmEnvelope200 envelopeWithVehicleState(VehicleCurrentStateContainer state) {
        VehicleManoeuvreContainer vmc = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(validGenericState())
                .vehicleCurrentStateContainer(state)
                .submaneuvres(List.of(validSubmanoeuvre()))
                .build();
        return envelopeWithVmc(vmc);
    }

    private static McmEnvelope200 envelopeWithVehicleSize(VehicleSize vehicleSize) {
        return envelopeWithVehicleState(VehicleCurrentStateContainer.builder()
                .manoeuvreOverallStrategy(ManoeuvreStrategy.STOP)
                .vehicleSpeed(new Speed(0, 1))
                .vehicleHeading(new Wgs84Angle(0, 1))
                .vehicleSize(vehicleSize)
                .build());
    }

    private static McmEnvelope200 envelopeWithSubmaneuvres(List<Submanoeuvre> submaneuvres) {
        VehicleManoeuvreContainer vmc = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(validGenericState())
                .vehicleCurrentStateContainer(validVehicleCurrentState())
                .submaneuvres(submaneuvres)
                .build();
        return envelopeWithVmc(vmc);
    }

    private static McmEnvelope200 envelopeWithVmc(VehicleManoeuvreContainer vmc) {
        return new McmEnvelope200(
                "mcm", "json/raw", "x", 1_600_000_000_000L, "2.0.0",
                new McmMessage200(1, 1L, 0, 1, 0,
                        validReferencePosition(), McmData.ofVehicle(vmc)));
    }
}

