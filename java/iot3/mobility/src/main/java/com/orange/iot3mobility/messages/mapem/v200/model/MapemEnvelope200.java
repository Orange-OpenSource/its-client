/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model;

/**
 * MAPEM v2.0.0 JSON envelope.
 * Corresponds to ETSI IS TS 103 301 v2.2.1.
 *
 * @param messageType Message type identifier, always "mapem".
 * @param origin Entity responsible for this message (e.g. "self", "mec_application").
 * @param version JSON message format version, always "2.0.0".
 * @param sourceUuid Identifier of the originating entity.
 * @param timestamp Unix epoch timestamp in milliseconds when the message was generated. Range: 1514764800000..1830297600000.
 * @param message The MAPEM payload.
 */
public record MapemEnvelope200(
        String messageType,
        String origin,
        String version,
        String sourceUuid,
        long timestamp,
        MapemMessage200 message) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final String messageType;
        private String origin;
        private final String version;
        private String sourceUuid;
        private Long timestamp;
        private MapemMessage200 message;

        private Builder() {
            this.messageType = "mapem";
            this.version = "2.0.0";
        }

        public Builder origin(String origin) { this.origin = origin; return this; }
        public Builder sourceUuid(String sourceUuid) { this.sourceUuid = sourceUuid; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public Builder message(MapemMessage200 message) { this.message = message; return this; }

        public MapemEnvelope200 build() {
            return new MapemEnvelope200(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(origin, "origin"),
                    requireNonNull(version, "version"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(message, "message"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

