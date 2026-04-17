/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model;

/**
 * Top-level SPATEM v2.0.0 envelope.
 *
 * @param messageType Required. Always {@code "spatem"}.
 * @param origin      Required. Originating entity: self, global_application, mec_application, on_board_application.
 * @param version     Required. Always {@code "2.0.0"}.
 * @param sourceUuid  Required. Identifier of the emitting station.
 * @param timestamp   Required. Unix epoch ms when the message was generated [1514764800000..1830297600000].
 * @param message     Required. SPAT message payload.
 */
public record SpatemEnvelope200(
        String messageType,
        String origin,
        String version,
        String sourceUuid,
        long timestamp,
        SpatemMessage200 message) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String origin;
        private String sourceUuid;
        private Long timestamp;
        private SpatemMessage200 message;

        // messageType and version are constants — hardcoded by this builder
        private static final String MESSAGE_TYPE = "spatem";
        private static final String VERSION = "2.0.0";

        private Builder() {}

        public Builder origin(String origin) { this.origin = origin; return this; }
        public Builder sourceUuid(String sourceUuid) { this.sourceUuid = sourceUuid; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public Builder message(SpatemMessage200 message) { this.message = message; return this; }

        public SpatemEnvelope200 build() {
            return new SpatemEnvelope200(
                    MESSAGE_TYPE,
                    requireNonNull(origin, "origin"),
                    VERSION,
                    requireNonNull(sourceUuid, "sourceUuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(message, "message"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

