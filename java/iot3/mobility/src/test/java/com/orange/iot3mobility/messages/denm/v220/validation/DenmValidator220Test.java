/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.denm.v220.validation;

import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.DetectionZone;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.EventPositionHeading;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.EventSpeed;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.EventZone;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DenmValidator220Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        DenmValidator220.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeAcceptsFullyPopulatedMessage() {
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

        AlacarteContainer alacarte = AlacarteContainer.builder()
                .lanePosition(0)
                .positioningSolution(1)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .locationContainer(location)
                .alacarteContainer(alacarte)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        DenmValidator220.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsMessageTypeMismatch() {
        DenmEnvelope220 envelope = new DenmEnvelope220(
                "invalid",
                "com_application_42",
                1514764800000L,
                "2.2.0",
                null,
                validMessage());

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateSituationRejectsTooManyLinkedDenms() {
        SituationContainer situation = SituationContainer.builder()
                .informationQuality(1)
                .eventType(new CauseCode(1, 0))
                .linkedDenms(java.util.List.of(
                        new ActionId(1, 1),
                        new ActionId(1, 2),
                        new ActionId(1, 3),
                        new ActionId(1, 4),
                        new ActionId(1, 5),
                        new ActionId(1, 6),
                        new ActionId(1, 7),
                        new ActionId(1, 8),
                        new ActionId(1, 9)))
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .situationContainer(situation)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateSituationRejectsEventEndOutOfRange() {
        SituationContainer situation = SituationContainer.builder()
                .informationQuality(1)
                .eventType(new CauseCode(1, 0))
                .eventEnd(9000)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .situationContainer(situation)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsStationTypeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(validEventPosition())
                .stationType(300)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsDetectionTimeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(4398046511104L)
                .referenceTime(0)
                .eventPosition(validEventPosition())
                .stationType(5)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsEventPositionLatitudeOutOfRange() {
        ReferencePosition invalidPosition = ReferencePosition.builder()
                .latitude(900000002)
                .longitude(0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(invalidPosition)
                .stationType(5)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    @Test
    void validateAlacarteRejectsPositioningSolutionOutOfRange() {
        AlacarteContainer alacarte = AlacarteContainer.builder()
                .positioningSolution(7)
                .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .alacarteContainer(alacarte)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator220.validateEnvelope(envelope));
    }

    private static DenmEnvelope220 validEnvelope() {
        return DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static DenmMessage220 validMessage() {
        return DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .build();
    }

    private static ManagementContainer validManagement() {
        return ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(validEventPosition())
                .stationType(5)
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
