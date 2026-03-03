package com.orange.iot3mobility.message.cam.v113.model;

public record HighFrequencyConfidence(
        Integer heading,
        Integer speed,
        Integer vehicleLength,
        Integer yawRate,
        Integer longitudinalAcceleration,
        Integer curvature,
        Integer lateralAcceleration,
        Integer verticalAcceleration) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for HighFrequencyConfidence.
     * <p>
     * All fields are optional.
     */
    public static final class Builder {
        private Integer heading;
        private Integer speed;
        private Integer vehicleLength;
        private Integer yawRate;
        private Integer longitudinalAcceleration;
        private Integer curvature;
        private Integer lateralAcceleration;
        private Integer verticalAcceleration;

        private Builder() {}

        public Builder heading(int heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public Builder vehicleLength(int vehicleLength) {
            this.vehicleLength = vehicleLength;
            return this;
        }

        public Builder yawRate(int yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder longitudinalAcceleration(int longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            return this;
        }

        public Builder curvature(int curvature) {
            this.curvature = curvature;
            return this;
        }

        public Builder lateralAcceleration(int lateralAcceleration) {
            this.lateralAcceleration = lateralAcceleration;
            return this;
        }

        public Builder verticalAcceleration(int verticalAcceleration) {
            this.verticalAcceleration = verticalAcceleration;
            return this;
        }

        public HighFrequencyConfidence build() {
            return new HighFrequencyConfidence(
                    heading,
                    speed,
                    vehicleLength,
                    yawRate,
                    longitudinalAcceleration,
                    curvature,
                    lateralAcceleration,
                    verticalAcceleration);
        }
    }
}