/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.validation;

import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;

/**
 * Validates mandatory structural constraints of a MAPEM v2.0.0 envelope.
 */
public final class MapemValidator200 {

    private MapemValidator200() {}

    /**
     * Validates the top-level envelope and its message payload.
     *
     * @param envelope the envelope to validate
     * @throws MapemValidationException if any constraint is violated
     */
    public static void validateEnvelope(MapemEnvelope200 envelope) {
        if (envelope == null) throw new MapemValidationException("MAPEM envelope must not be null");
        if (!"mapem".equals(envelope.messageType())) {
            throw new MapemValidationException("message_type must be 'mapem', got: " + envelope.messageType());
        }
        if (!"2.0.0".equals(envelope.version())) {
            throw new MapemValidationException("version must be '2.0.0', got: " + envelope.version());
        }
        if (envelope.sourceUuid() == null || envelope.sourceUuid().isBlank()) {
            throw new MapemValidationException("source_uuid must not be null or blank");
        }
        validateMessage(envelope.message());
    }

    private static void validateMessage(MapemMessage200 msg) {
        if (msg == null) throw new MapemValidationException("MAPEM message must not be null");
        if (msg.msgIssueRevision() < 0 || msg.msgIssueRevision() > 127) {
            throw new MapemValidationException("msg_issue_revision out of range [0..127]: " + msg.msgIssueRevision());
        }
        boolean hasIntersections = msg.intersections() != null && !msg.intersections().isEmpty();
        boolean hasRoadSegments = msg.roadSegments() != null && !msg.roadSegments().isEmpty();
        if (!hasIntersections && !hasRoadSegments) {
            throw new MapemValidationException("MAPEM message must contain at least one intersection or road segment");
        }
    }
}

