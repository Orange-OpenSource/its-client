/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.spatem.v200.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.TimeChangeDetail;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class SpatemSchema200Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("spatem/spatem_schema_2-0-0.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        SpatemEnvelope200 envelope = minimalEnvelope();
        SpatemCodec codec = new SpatemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(SpatemVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        SpatemEnvelope200 envelope = fullyPopulatedEnvelope();

        SpatemCodec codec = new SpatemCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(SpatemVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static SpatemEnvelope200 minimalEnvelope() {
        MovementEvent movementEvent = MovementEvent.builder()
                .eventState(6)
                .build();
        MovementState movementState = MovementState.builder()
                .signalGroup(1)
                .stateTimeSpeed(List.of(movementEvent))
                .build();
        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(null, 1001);
        IntersectionState intersectionState = IntersectionState.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .status(List.of())
                .states(List.of(movementState))
                .build();
        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(List.of(intersectionState))
                .build();
        return SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static SpatemEnvelope200 fullyPopulatedEnvelope() {
        MovementEvent movementEvent = MovementEvent.builder()
                .eventState(6)
                .timing(TimeChangeDetail.builder()
                        .minEndTime(100)
                        .maxEndTime(200)
                        .likelyTime(150)
                        .build())
                .build();

        MovementState movementState1 = MovementState.builder()
                .signalGroup(1)
                .stateTimeSpeed(List.of(movementEvent))
                .build();
        MovementState movementState2 = MovementState.builder()
                .signalGroup(2)
                .stateTimeSpeed(List.of(MovementEvent.builder().eventState(3).build()))
                .build();

        IntersectionReferenceId id = new IntersectionReferenceId(42, 1001);
        IntersectionState intersectionState = IntersectionState.builder()
                .id(id)
                .revision(1)
                .status(List.of())
                .states(List.of(movementState1, movementState2))
                .build();

        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(List.of(intersectionState))
                .build();

        return SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
