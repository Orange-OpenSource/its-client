/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v240.model;

/**
 * CamEnvelope240 - base class to build a JSON CAM v2.4.0 with its header
 * <p>
 * This 2.4.0 version corresponds to the following ETSI references:
 * <ul>
 *     <li>CAM TS 103 900 - version 2.2.1</li>
 *     <li>CDD TS 102 894-2 - version 2.3.1</li>
 * </ul>
 *
 * @param messageType Type of the message carried in message property (cam)
 * @param messageFormat {@link MessageFormat}
 * @param sourceUuid Unique id for the message sender
 * @param timestamp Timestamp when the message was generated since Unix Epoch (millisecond)
 * @param version JSON message format version (2.4.0)
 * @param linkedStationId Optional identifier for a linked ITS-S, as a trailer or a platooning pair
 * @param message {@link CamMessage240}
 */
public record CamEnvelope240(
        String messageType,
        String messageFormat,
        String sourceUuid,
        long timestamp,
        String version,
        Long linkedStationId,
        CamMessage240 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CamEnvelope240.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>messageType - hardcoded (cam)</li>
     * <li>messageFormat - see {@link MessageFormat}</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>version - hardcoded (2.4.0)</li>
     * <li>message</li>
     * </ul>
     * Optional fields:
     * <ul>
     * <li>linkedStationId</li>
     * </ul>
     */
    public static final class Builder {
        private final String messageType;
        private String messageFormat;
        private String sourceUuid;
        private Long timestamp;
        private final String version;
        private Long linkedStationId;
        private CamMessage240 message;

        private Builder() {
            this.messageType = "cam";
            this.version = "2.4.0";
        }

        public Builder messageFormat(String messageFormat) {
            this.messageFormat = messageFormat;
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

        public Builder linkedStationId(long linkedStationId) {
            this.linkedStationId = linkedStationId;
            return this;
        }

        public Builder message(CamMessage240 message) {
            this.message = message;
            return this;
        }

        public CamEnvelope240 build() {
            return new CamEnvelope240(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(messageFormat, "message_format"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(version, "version"),
                    linkedStationId,
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
