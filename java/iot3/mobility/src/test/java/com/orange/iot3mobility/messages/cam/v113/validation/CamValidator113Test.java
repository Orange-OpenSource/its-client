/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
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

