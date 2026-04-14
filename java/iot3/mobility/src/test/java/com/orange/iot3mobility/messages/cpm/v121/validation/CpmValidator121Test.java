/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cpm.v121.validation;

import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CpmValidator121Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        CpmValidator121.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeRejectsTimestampOutOfRange() {
        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1L)
                .message(validMessage())
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    private static CpmEnvelope121 validEnvelope() {
        return CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static CpmMessage121 validMessage() {
        ReferencePosition reference = new ReferencePosition(0, 0, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .confidence(confidence)
                .build();

        return CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .build();
    }
}

