package com.orange.iot3mobility.message.cam.v230.model;

/**
 * CamEnvelope230 - base class to build a JSON CAM v2.3.0 with its header
 * <p>
 * This 2.3.0 version corresponds to the following ETSI references:
 * <ul>
 *     <li>CAM TS 103 900 - version 2.2.1</li>
 *     <li>CDD TS 102 894-2 - version 2.3.1</li>
 * </ul>
 *
 * @param messageType Type of the message carried in message property (cam)
 * @param messageFormat {@link MessageFormat}
 * @param sourceUuid Unique id for the message sender
 * @param timestamp Timestamp when the message was generated since Unix Epoch (millisecond)
 * @param version JSON message format version (2.3.0)
 * @param message {@link CamMessage230}
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
     * <li>messageType - hardcoded (cam)</li>
     * <li>messageFormat - see {@link MessageFormat}</li>
     * <li>sourceUuid</li>
     * <li>timestamp</li>
     * <li>version - hardcoded (2.3.0)</li>
     * <li>message</li>
     * </ul>
     */
    public static final class Builder {
        private final String messageType;
        private String messageFormat;
        private String sourceUuid;
        private Long timestamp;
        private final String version;
        private CamMessage230 message;

        private Builder() {
            this.messageType = "cam";
            this.version = "2.3.0";
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
