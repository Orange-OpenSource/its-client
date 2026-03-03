package com.orange.iot3mobility.message.cam.v113.model;

import java.util.List;

public record LowFrequencyContainer(
        Integer vehicleRole,
        String exteriorLights,
        List<PathPoint> pathHistory) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for LowFrequencyContainer.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>vehicleRole</li>
     * <li>exteriorLights</li>
     * <li>pathHistory</li>
     * </ul>
     */
    public static final class Builder {
        private Integer vehicleRole;
        private String exteriorLights;
        private List<PathPoint> pathHistory;

        private Builder() {}

        public Builder vehicleRole(int vehicleRole) {
            this.vehicleRole = vehicleRole;
            return this;
        }

        public Builder exteriorLights(String exteriorLights) {
            this.exteriorLights = exteriorLights;
            return this;
        }

        public Builder pathHistory(List<PathPoint> pathHistory) {
            this.pathHistory = pathHistory;
            return this;
        }

        public LowFrequencyContainer build() {
            return new LowFrequencyContainer(
                    requireNonNull(vehicleRole, "vehicle_role"),
                    requireNonNull(exteriorLights, "exterior_lights"),
                    requireNonNull(pathHistory, "path_history"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
