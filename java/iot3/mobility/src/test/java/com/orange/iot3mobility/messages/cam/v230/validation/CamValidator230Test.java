/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cam.v230.validation;

import com.orange.iot3mobility.messages.cam.v230.model.CamEnvelope230;
import com.orange.iot3mobility.messages.cam.v230.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.Altitude;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamValidator230Test {

    @Test
    void validateEnvelopeAcceptsStructuredPayload() {
        CamEnvelope230 envelope = validEnvelope();
        CamValidator230.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsMessageFormatMismatch() {
        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("asn1/uper")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(validStructuredMessage())
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    private static CamEnvelope230 validEnvelope() {
        return CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(validStructuredMessage())
                .build();
    }

    private static CamStructuredData validStructuredMessage() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        BasicVehicleContainerHighFrequency high = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(0, 1))
                .speed(new Speed(0, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();

        return CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();
    }
}

