package com.orange.iot3mobility.message.cam.v113.model;

public record CamMessage113(
        int protocolVersion,
        long stationId,
        int generationDeltaTime,
        BasicContainer basicContainer,
        HighFrequencyContainer highFrequencyContainer,
        LowFrequencyContainer lowFrequencyContainer,
        SpecialVehicleContainer specialVehicleContainer) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CamMessage113.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>protocolVersion</li>
     * <li>stationId</li>
     * <li>generationDeltaTime</li>
     * <li>basicContainer</li>
     * <li>highFrequencyContainer</li>
     * </ul>
     */
    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private Integer generationDeltaTime;
        private BasicContainer basicContainer;
        private HighFrequencyContainer highFrequencyContainer;
        private LowFrequencyContainer lowFrequencyContainer;
        private SpecialVehicleContainer specialVehicleContainer;

        private Builder() {}

        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder stationId(long stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder generationDeltaTime(int generationDeltaTime) {
            this.generationDeltaTime = generationDeltaTime;
            return this;
        }

        public Builder basicContainer(BasicContainer basicContainer) {
            this.basicContainer = basicContainer;
            return this;
        }

        public Builder highFrequencyContainer(HighFrequencyContainer highFrequencyContainer) {
            this.highFrequencyContainer = highFrequencyContainer;
            return this;
        }

        public Builder lowFrequencyContainer(LowFrequencyContainer lowFrequencyContainer) {
            this.lowFrequencyContainer = lowFrequencyContainer;
            return this;
        }

        public Builder specialVehicleContainer(SpecialVehicleContainer specialVehicleContainer) {
            this.specialVehicleContainer = specialVehicleContainer;
            return this;
        }

        public CamMessage113 build() {
            return new CamMessage113(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(generationDeltaTime, "generation_delta_time"),
                    requireNonNull(basicContainer, "basic_container"),
                    requireNonNull(highFrequencyContainer, "high_frequency_container"),
                    lowFrequencyContainer,
                    specialVehicleContainer);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
