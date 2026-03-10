/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model;

/**
 * CpmEnvelope121 - base class to build a CPM v1.2.1 with its header
 *
 * @param type message type (cpm)
 * @param origin {@link Origin}
 * @param version json message format version (1.2.1)
 * @param sourceUuid identifier
 * @param timestamp Unit: millisecond. The timestamp when the message was generated since Unix Epoch (1970/01/01)
 * @param message {@link CpmMessage121}
 */
public record CpmEnvelope121(
        String type,
        String origin,
        String version,
        String sourceUuid,
        long timestamp,
        CpmMessage121 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CpmEnvelope121.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>type - hardcoded (cpm)</li>
     * <li>origin - see {@link Origin}</li>
     * <li>version - hardcoded (1.2.1)</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>message</li>
     * </ul>
     */
    public static final class Builder {
        private final String type;
        private String origin;
        private final String version;
        private String sourceUuid;
        private Long timestamp;
        private CpmMessage121 message;

        private Builder() {
            this.type = "cpm";
            this.version = "1.2.1";
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder sourceUuid(String sourceUuid) {
            this.sourceUuid = sourceUuid;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder message(CpmMessage121 message) {
            this.message = message;
            return this;
        }

        public CpmEnvelope121 build() {
            return new CpmEnvelope121(
                    requireNonNull(type, "type"),
                    requireNonNull(origin, "origin"),
                    requireNonNull(version, "version"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(message, "message"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
