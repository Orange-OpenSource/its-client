/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.denm.v220.validation;

import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DenmValidator220Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        DenmValidator220.validateEnvelope(validEnvelope());
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

    private static DenmEnvelope220 validEnvelope() {
        return DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static DenmMessage220 validMessage() {
        ReferencePosition position = ReferencePosition.builder()
                .latitude(0)
                .longitude(0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(position)
                .stationType(5)
                .build();

        return DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
    }
}

