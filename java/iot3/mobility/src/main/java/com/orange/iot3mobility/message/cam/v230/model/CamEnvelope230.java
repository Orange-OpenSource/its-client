package com.orange.iot3mobility.message.cam.v230.model;

/**
 * Top-level CAM envelope for version 2.3.0.
 */
public record CamEnvelope230(
        String messageType,
        String messageFormat,
        String sourceUuid,
        long timestamp,
        String version,
        CamMessage230 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CamEnvelope230.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>messageType</li>
     * <li>messageFormat</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>version</li>
     * <li>message</li>
     * </ul>
     */
    public static final class Builder {
        private String messageType;
        private String messageFormat;
        private String sourceUuid;
        private Long timestamp;
        private String version;
        private CamMessage230 message;

        private Builder() {}

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
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

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder message(CamMessage230 message) {
            this.message = message;
            return this;
        }

        public CamEnvelope230 build() {
            return new CamEnvelope230(
                    requireNonNull(messageType, "message_type"),
                    requireNonNull(messageFormat, "message_format"),
                    requireNonNull(sourceUuid, "source_uuid"),
                    requireNonNull(timestamp, "timestamp"),
                    requireNonNull(version, "version"),
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
