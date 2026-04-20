/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.validation;

import com.orange.iot3mobility.messages.cam.v113.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamValidator113Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        CamEnvelope113 envelope = validEnvelope();
        CamValidator113.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeAcceptsFullyPopulatedMessage() {
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 0, 0))
                .positionConfidence(new PositionConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();

        HighFrequencyContainer hf = HighFrequencyContainer.builder()
                .heading(0)
                .speed(0)
                .driveDirection(0)
                .vehicleSize(10, 10)
                .curvature(0)
                .curvatureCalculationMode(0)
                .longitudinalAcceleration(0)
                .yawRate(0)
                .accelerationControl("0000000")
                .lanePosition(0)
                .lateralAcceleration(0)
                .verticalAcceleration(0)
                .highFrequencyConfidence(HighFrequencyConfidence.builder()
                        .heading(1).speed(1).vehicleLength(0).yawRate(0)
                        .longitudinalAcceleration(0).curvature(0)
                        .lateralAcceleration(0).verticalAcceleration(0)
                        .build())
                .build();

        LowFrequencyContainer lf = LowFrequencyContainer.builder()
                .vehicleRole(0)
                .exteriorLights("00000000")
                .pathHistory(List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1)))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(hf)
                .lowFrequencyContainer(lf)
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        CamValidator113.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsInvalidOrigin() {
        CamEnvelope113 envelope = new CamEnvelope113(
                "cam",
                "invalid_origin",
                "1.1.3",
                "CCU6",
                1514764800000L,
                validMessage());

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateLowFrequencyRejectsTooManyPathPoints() {
        List<PathPoint> points = new ArrayList<>();
        for (int i = 0; i < 41; i++) {
            points.add(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1));
        }

        LowFrequencyContainer low = LowFrequencyContainer.builder()
                .vehicleRole(0)
                .exteriorLights("00000000")
                .pathHistory(points)
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateHighFrequencyRejectsInvalidAccelerationControl() {
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder()
                        .accelerationControl("1010102")
                        .build())
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateLowFrequencyRejectsInvalidExteriorLights() {
        LowFrequencyContainer low = LowFrequencyContainer.builder()
                .vehicleRole(0)
                .exteriorLights("0101012")
                .pathHistory(List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1)))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateHighFrequencyRejectsHeadingOutOfRange() {
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder()
                        .heading(4000)
                        .build())
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateLowFrequencyRejectsPathDeltaTimeOutOfRange() {
        LowFrequencyContainer low = LowFrequencyContainer.builder()
                .vehicleRole(0)
                .exteriorLights("00000000")
                .pathHistory(List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 0)))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateBasicContainerRejectsLatitudeOutOfRange() {
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(900000002, 0, 0))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateBasicContainerRejectsAltitudeOutOfRange() {
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 0, -100001))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    @Test
    void validateHighFrequencyRejectsSpeedOutOfRange() {
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder()
                        .speed(20000)
                        .build())
                .build();

        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator113.validateEnvelope(envelope));
    }

    private static CamEnvelope113 validEnvelope() {
        return CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static CamMessage113 validMessage() {
        return CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasic())
                .highFrequencyContainer(HighFrequencyContainer.builder().build())
                .build();
    }

    private static BasicContainer validBasic() {
        return BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 0, 0))
                .build();
    }
}
