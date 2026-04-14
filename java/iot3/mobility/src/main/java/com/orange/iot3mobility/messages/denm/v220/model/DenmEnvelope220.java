/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model;

import com.orange.iot3mobility.messages.denm.v220.model.path.PathElement;

import java.util.List;

/**
 * DenmEnvelope220 - base class to build a DENM v2.2.0 with its header
 *
 * @param messageType message type (denm)
 * @param sourceUuid identifier. Example: com_car_4294967295, com_application_42
 * @param timestamp Unit: millisecond. The timestamp when the message was generated since Unix Epoch (1970/01/01)
 * @param version json message format version (2.2.0)
 * @param path Optional. Root source path (ordered list of {@link PathElement})
 * @param message {@link DenmMessage220}
 */
public record DenmEnvelope220(
        String messageType,
        String sourceUuid,
        long timestamp,
        String version,
        List<PathElement> path,
        DenmMessage220 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for DenmEnvelope220.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>messageType - hardcoded (denm)</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>version - hardcoded (2.2.0)</li>
     * <li>message</li>
     * </ul>
     */
    public static final class Builder {
        private final String messageType;
        private String sourceUuid;
        private Long timestamp;
        private final String version;
        private List<PathElement> path;
        private DenmMessage220 message;

        private Builder() {
            this.messageType = "denm";
            this.version = "2.2.0";
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

        public Builder message(DenmMessage220 message) {
            this.message = message;
            return this;
        }

        public DenmEnvelope220 build() {
            return new DenmEnvelope220(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(version, "version"),
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
