/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cpm.v211.validation;

import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CpmValidator211Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        CpmValidator211.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeRejectsObjectIdRotationCountOutOfRange() {
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .objectIdRotationCount(300)
                .message(validMessage())
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    private static CpmEnvelope211 validEnvelope() {
        return CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static CpmMessage211 validMessage() {
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        Altitude altitude = new Altitude(0, 0);
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(ellipse)
                .altitude(altitude)
                .build();
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(reference)
                .build();

        return CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
    }
}

