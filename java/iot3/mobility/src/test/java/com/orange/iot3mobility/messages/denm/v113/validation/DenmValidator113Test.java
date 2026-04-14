/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.denm.v113.validation;

import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DenmValidator113Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        DenmValidator113.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeRejectsInvalidOrigin() {
        DenmEnvelope113 envelope = new DenmEnvelope113(
                "denm",
                "invalid_origin",
                "1.1.3",
                "CCU6",
                1514764800000L,
                null,
                validMessage());

        assertThrows(DenmValidationException.class, () -> DenmValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateAlacarteRejectsNegativePositioningSolution() {
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .alacarteContainer(new AlacarteContainer(null, -1))
                .build();

        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsStationTypeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(new ReferencePosition(0, 0, 0))
                .stationType(300)
                .build();
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsDetectionTimeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(4398046511104L)
                .referenceTime(0)
                .eventPosition(new ReferencePosition(0, 0, 0))
                .build();
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsEventPositionLatitudeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(new ReferencePosition(900000002, 0, 0))
                .build();
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(DenmValidationException.class, () -> DenmValidator113.validateEnvelope(envelope));
    }

    private static DenmEnvelope113 validEnvelope() {
        return DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static DenmMessage113 validMessage() {
        return DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .build();
    }

    private static ManagementContainer validManagement() {
        ReferencePosition position = new ReferencePosition(0, 0, 0);
        return ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(position)
                .build();
    }
}
