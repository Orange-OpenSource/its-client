/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.cam.v113.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.core.CamVersion;
import com.orange.iot3mobility.messages.cam.v113.model.BasicContainer;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v113.model.CamMessage113;
import com.orange.iot3mobility.messages.cam.v113.model.HighFrequencyContainer;
import com.orange.iot3mobility.messages.cam.v113.model.DeltaReferencePosition;
import com.orange.iot3mobility.messages.cam.v113.model.HighFrequencyConfidence;
import com.orange.iot3mobility.messages.cam.v113.model.LowFrequencyContainer;
import com.orange.iot3mobility.messages.cam.v113.model.PathPoint;
import com.orange.iot3mobility.messages.cam.v113.model.PositionConfidence;
import com.orange.iot3mobility.messages.cam.v113.model.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v113.model.ReferencePosition;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class CamSchema113Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("cam/cam_schema_1-1-3.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        CamEnvelope113 envelope = minimalEnvelope();
        CamCodec codec = new CamCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V1_1_3, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        CamEnvelope113 envelope = fullyPopulatedEnvelope();

        CamCodec codec = new CamCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V1_1_3, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static CamEnvelope113 minimalEnvelope() {
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 0, 0))
                .build();
        HighFrequencyContainer high = HighFrequencyContainer.builder().build();
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();
        return CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static CamEnvelope113 fullyPopulatedEnvelope() {
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 0, 0))
                .positionConfidence(new PositionConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();

        HighFrequencyContainer high = HighFrequencyContainer.builder()
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

        LowFrequencyContainer low = LowFrequencyContainer.builder()
                .vehicleRole(0)
                .exteriorLights("00000000")
                .pathHistory(List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1)))
                .build();

        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .lowFrequencyContainer(low)
                .build();

        return CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
