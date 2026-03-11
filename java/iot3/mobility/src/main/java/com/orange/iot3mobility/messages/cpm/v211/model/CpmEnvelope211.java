/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model;

/**
 * CpmEnvelope211 - base class to build a JSON CPM v2.1.1 with its header
 * <p>
 * This 2.1.1 version corresponds to the following ETSI references:
 * <ul>
 *     <li>CPM TS 103 324 - version 2.1.1</li>
 *     <li>CDD TS 102 894-2 - version 2.1.1</li>
 * </ul>
 *
 * @param messageType Type of the message carried in message property (cpm)
 * @param sourceUuid Unique id for the message sender
 * @param timestamp Timestamp when the message was generated since Unix Epoch (millisecond)
 * @param version JSON message format version (2.1.1)
 * @param objectIdRotationCount Optional object ID rotation count.
 * @param message {@link CpmMessage211}
 */
public record CpmEnvelope211(
        String messageType,
        String sourceUuid,
        long timestamp,
        String version,
        Integer objectIdRotationCount,
        CpmMessage211 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CpmEnvelope211.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>messageType - hardcoded (cpm)</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>version - hardcoded (2.1.1)</li>
     * <li>message</li>
     * </ul>
     */
    public static final class Builder {
        private final String messageType;
        private String sourceUuid;
        private Long timestamp;
        private final String version;
        private Integer objectIdRotationCount;
        private CpmMessage211 message;

        private Builder() {
            this.messageType = "cpm";
            this.version = "2.1.1";
        }

        public Builder sourceUuid(String sourceUuid) {
            this.sourceUuid = sourceUuid;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder message(CpmMessage211 message) {
            this.message = message;
            return this;
        }

        public Builder objectIdRotationCount(Integer objectIdRotationCount) {
            this.objectIdRotationCount = objectIdRotationCount;
            return this;
        }

        public CpmEnvelope211 build() {
            return new CpmEnvelope211(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(version, "version"),
                    objectIdRotationCount,
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
