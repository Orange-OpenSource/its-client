/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.validation;

import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;

/**
 * Validates mandatory structural constraints of a SPATEM v2.0.0 envelope.
 */
public final class SpatemValidator200 {

    private SpatemValidator200() {}

    /**
     * Validates the top-level envelope and its message payload.
     *
     * @param envelope the envelope to validate
     * @throws SpatemValidationException if any constraint is violated
     */
    public static void validateEnvelope(SpatemEnvelope200 envelope) {
        if (envelope == null) throw new SpatemValidationException("SPATEM envelope must not be null");
        if (!"spatem".equals(envelope.messageType())) {
            throw new SpatemValidationException("message_type must be 'spatem', got: " + envelope.messageType());
        }
        if (!"2.0.0".equals(envelope.version())) {
            throw new SpatemValidationException("version must be '2.0.0', got: " + envelope.version());
        }
        if (envelope.sourceUuid() == null || envelope.sourceUuid().isBlank()) {
            throw new SpatemValidationException("source_uuid must not be null or blank");
        }
        validateMessage(envelope.message());
    }

    private static void validateMessage(SpatemMessage200 msg) {
        if (msg == null) throw new SpatemValidationException("SPATEM message must not be null");
        if (msg.intersections() == null || msg.intersections().isEmpty()) {
            throw new SpatemValidationException("SPATEM message must contain at least one intersection");
        }
    }
}

