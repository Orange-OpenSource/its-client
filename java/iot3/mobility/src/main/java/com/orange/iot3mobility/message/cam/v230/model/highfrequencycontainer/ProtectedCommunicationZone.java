package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

public record ProtectedCommunicationZone(
        int protectedZoneType,
        Long expiryTime,
        int protectedZoneLatitude,
        int protectedZoneLongitude,
        Integer protectedZoneRadius,
        Integer protectedZoneId) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ProtectedCommunicationZone.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>protectedZoneType</li>
     * <li>protectedZoneLatitude</li>
     * <li>protectedZoneLongitude</li>
     * </ul>
     */
    public static final class Builder {
        private Integer protectedZoneType;
        private Long expiryTime;
        private Integer protectedZoneLatitude;
        private Integer protectedZoneLongitude;
        private Integer protectedZoneRadius;
        private Integer protectedZoneId;

        private Builder() {}

        public Builder protectedZoneType(int protectedZoneType) {
            this.protectedZoneType = protectedZoneType;
            return this;
        }

        public Builder expiryTime(long expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }

        public Builder protectedZoneLatitude(int protectedZoneLatitude) {
            this.protectedZoneLatitude = protectedZoneLatitude;
            return this;
        }

        public Builder protectedZoneLongitude(int protectedZoneLongitude) {
            this.protectedZoneLongitude = protectedZoneLongitude;
            return this;
        }

        public Builder protectedZoneRadius(int protectedZoneRadius) {
            this.protectedZoneRadius = protectedZoneRadius;
            return this;
        }

        public Builder protectedZoneId(int protectedZoneId) {
            this.protectedZoneId = protectedZoneId;
            return this;
        }

        public ProtectedCommunicationZone build() {
            return new ProtectedCommunicationZone(
                    requireNonNull(protectedZoneType, "protected_zone_type"),
                    expiryTime,
                    requireNonNull(protectedZoneLatitude, "protected_zone_latitude"),
                    requireNonNull(protectedZoneLongitude, "protected_zone_longitude"),
                    protectedZoneRadius,
                    protectedZoneId);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
