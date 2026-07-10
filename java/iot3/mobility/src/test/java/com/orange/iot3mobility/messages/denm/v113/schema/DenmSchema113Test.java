/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v113.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathHistory;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.EventType;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.LinkedCause;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class DenmSchema113Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("denm/denm_schema_1-1-3.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        DenmEnvelope113 envelope = minimalEnvelope();
        DenmCodec codec = new DenmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V1_1_3, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        DenmEnvelope113 envelope = fullyPopulatedEnvelope();

        DenmCodec codec = new DenmCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V1_1_3, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static DenmEnvelope113 minimalEnvelope() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(new ReferencePosition(0, 0, 0))
                .build();
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        return DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static DenmEnvelope113 fullyPopulatedEnvelope() {
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(1000L)
                .referenceTime(1000L)
                .termination(0)
                .eventPosition(new ReferencePosition(0, 0, 0))
                .relevanceDistance(0)
                .relevanceTrafficDirection(0)
                .validityDuration(60)
                .transmissionInterval(1000)
                .stationType(5)
                .confidence(new PositionConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();

        SituationContainer situation = SituationContainer.builder()
                .informationQuality(3)
                .eventType(new EventType(97, 0))
                .linkedCause(new LinkedCause(1, 0))
                .build();

        LocationContainer location = LocationContainer.builder()
                .eventSpeed(500)
                .eventPositionHeading(900)
                .traces(List.of(new PathHistory(
                        List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 10)))))
                .roadType(0)
                .confidence(new LocationConfidence(1, 1))
                .build();

        AlacarteContainer alacarte = new AlacarteContainer(0, 1);

        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .locationContainer(location)
                .alacarteContainer(alacarte)
                .build();

        return DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
