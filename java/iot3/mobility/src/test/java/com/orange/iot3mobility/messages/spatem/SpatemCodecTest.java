/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.spatem;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemException;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatemCodecTest {

    @Test
    void writeReadV200RoundTrip() throws Exception {
        SpatemEnvelope200 envelope = validEnvelope200();
        SpatemCodec codec = new SpatemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(SpatemVersion.V2_0_0, envelope, out);

        SpatemCodec.SpatemFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(SpatemVersion.V2_0_0, frame.version());
        assertTrue(frame.envelope() instanceof SpatemEnvelope200);
        SpatemEnvelope200 parsed = (SpatemEnvelope200) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void readStringV200RoundTrip() throws Exception {
        SpatemEnvelope200 envelope = validEnvelope200();
        SpatemCodec codec = new SpatemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(SpatemVersion.V2_0_0, envelope, out);

        SpatemCodec.SpatemFrame<?> frame = codec.read(out.toString());
        assertEquals(SpatemVersion.V2_0_0, frame.version());
        SpatemEnvelope200 parsed = (SpatemEnvelope200) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
    }

    @Test
    void readRejectsMissingVersion() {
        SpatemCodec codec = new SpatemCodec(new JsonFactory());
        String json = "{\"type\":\"spatem\"}";
        assertThrows(SpatemException.class, () -> codec.read(json));
    }

    private static SpatemEnvelope200 validEnvelope200() {
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
}

