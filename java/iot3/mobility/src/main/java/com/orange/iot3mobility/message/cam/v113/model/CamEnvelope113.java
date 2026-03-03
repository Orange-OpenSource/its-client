package com.orange.iot3mobility.message.cam.v113.model;

public record CamEnvelope113(
        String type,
        String origin,
        String version,
        String sourceUuid,
        long timestamp,
        CamMessage113 message) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CamEnvelope113.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>type - hardcoded (cam)</li>
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
        private CamMessage113 message;

        private Builder() {
            this.type = "cam";
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

        public Builder message(CamMessage113 message) {
            this.message = message;
            return this;
        }

        public CamEnvelope113 build() {
            return new CamEnvelope113(
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
