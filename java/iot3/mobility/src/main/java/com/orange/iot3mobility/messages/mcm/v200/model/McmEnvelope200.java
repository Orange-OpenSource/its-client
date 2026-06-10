/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model;

/**
 * MCM v2.0.0 top-level envelope.
 * <p>
 * Note: unlike CAM/DENM, MCM uses {@code message_type} instead of {@code type},
 * and {@code message_format} instead of {@code origin}. There is no {@code origin} field.
 *
 * @param messageType   Always {@code "mcm"}.
 * @param messageFormat {@code "json/raw"} or {@code "asn1/uper"}.
 * @param sourceUuid    Unique sender identifier.
 * @param timestamp     Unix epoch timestamp in milliseconds.
 * @param version       Always {@code "2.0.0"}.
 * @param message       MCM structured payload (json/raw only — ASN.1 UPER not modelled here).
 */
public record McmEnvelope200(
        String messageType,
        String messageFormat,
        String sourceUuid,
        long timestamp,
        String version,
        McmMessage200 message) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final String messageType;
        private String messageFormat;
        private String sourceUuid;
        private Long timestamp;
        private final String version;
        private McmMessage200 message;

        private Builder() {
            this.messageType = "mcm";
            this.version = "2.0.0";
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

        public Builder message(McmMessage200 message) {
            this.message = message;
            return this;
        }

        public McmEnvelope200 build() {
            return new McmEnvelope200(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(messageFormat, "message_format"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(version, "version"),
                    requireNonNull(message, "message"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

