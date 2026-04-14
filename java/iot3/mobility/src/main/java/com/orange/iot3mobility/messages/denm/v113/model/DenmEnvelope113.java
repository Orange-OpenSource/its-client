/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model;

import com.orange.iot3mobility.messages.denm.v113.model.path.PathElement;

import java.util.List;

/**
 * DenmEnvelope113 - base class to build a DENM v1.1.3 with its header
 *
 * @param type message type (denm)
 * @param origin {@link Origin}
 * @param version json message format version (1.1.3)
 * @param sourceUuid identifier
 * @param timestamp Unit: millisecond. The timestamp when the message was generated since Unix Epoch (1970/01/01)
 * @param path Optional. List of {@link PathElement})
 * @param message {@link DenmMessage113}
 */
public record DenmEnvelope113(
        String type,
        String origin,
        String version,
        String sourceUuid,
        long timestamp,
        List<PathElement> path,
        DenmMessage113 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for DenmEnvelope113.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>type - hardcoded (denm)</li>
     * <li>origin - see {@link Origin}</li>
     * <li>version - hardcoded (1.1.3)</li>
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
        private List<PathElement> path;
        private DenmMessage113 message;

        private Builder() {
            this.type = "denm";
            this.version = "1.1.3";
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

        public Builder path(List<PathElement> path) {
            this.path = path;
            return this;
        }

        public Builder message(DenmMessage113 message) {
            this.message = message;
            return this;
        }

        public DenmEnvelope113 build() {
            return new DenmEnvelope113(
                    requireNonNull(type, "type"),
                    requireNonNull(origin, "origin"),
                    requireNonNull(version, "version"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    path,
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
