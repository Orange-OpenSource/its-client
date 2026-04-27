/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.spatem.v200.validation;

import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SpatemValidator200Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        SpatemEnvelope200 envelope = validEnvelope();
        SpatemValidator200.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsBlankSourceUuid() {
        SpatemEnvelope200 envelope = new SpatemEnvelope200(
                "spatem", "self", "2.0.0", "  ", 1514764800000L, validMessage());
        assertThrows(SpatemValidationException.class, () -> SpatemValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateMessageRejectsEmptyIntersections() {
        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(Collections.emptyList())
                .build();

        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(SpatemValidationException.class, () -> SpatemValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateMessageRejectsNullIntersections() {
        SpatemMessage200 message = new SpatemMessage200(1, 42L, null, null, null);

        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(SpatemValidationException.class, () -> SpatemValidator200.validateEnvelope(envelope));
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private static SpatemEnvelope200 validEnvelope() {
        return SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static SpatemMessage200 validMessage() {
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

        return SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(List.of(intersectionState))
                .build();
    }
}

