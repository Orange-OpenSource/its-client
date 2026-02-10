package com.orange.iot3mobility.message.cam.v113.model;

public record HighFrequencyContainer(
        Integer heading,
        Integer speed,
        Integer driveDirection,
        Integer vehicleLength,
        Integer vehicleWidth,
        Integer curvature,
        Integer curvatureCalculationMode,
        Integer longitudinalAcceleration,
        Integer yawRate,
        String accelerationControl,
        Integer lanePosition,
        Integer lateralAcceleration,
        Integer verticalAcceleration,
        HighFrequencyConfidence confidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer heading;
        private Integer speed;
        private Integer driveDirection;
        private Integer vehicleLength;
        private Integer vehicleWidth;
        private Integer curvature;
        private Integer curvatureCalculationMode;
        private Integer longitudinalAcceleration;
        private Integer yawRate;
        private String accelerationControl;
        private Integer lanePosition;
        private Integer lateralAcceleration;
        private Integer verticalAcceleration;
        private HighFrequencyConfidence confidence;

        private Builder() {}

        public Builder heading(int heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public Builder driveDirection(int driveDirection) {
            this.driveDirection = driveDirection;
            return this;
        }

        public Builder vehicleSize(int vehicleLength, int vehicleWidth) {
            this.vehicleLength = vehicleLength;
            this.vehicleWidth = vehicleWidth;
            return this;
        }

        public Builder curvature(int curvature) {
            this.curvature = curvature;
            return this;
        }

        public Builder curvatureCalculationMode(int curvatureCalculationMode) {
            this.curvatureCalculationMode = curvatureCalculationMode;
            return this;
        }

        public Builder longitudinalAcceleration(int longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            return this;
        }

        public Builder yawRate(int yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder accelerationControl(String accelerationControl) {
            this.accelerationControl = accelerationControl;
            return this;
        }

        public Builder lanePosition(int lanePosition) {
            this.lanePosition = lanePosition;
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

        public Builder highFrequencyConfidence(HighFrequencyConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public HighFrequencyContainer build() {
            return new HighFrequencyContainer(
                    heading,
                    speed,
                    driveDirection,
                    vehicleLength,
                    vehicleWidth,
                    curvature,
                    curvatureCalculationMode,
                    longitudinalAcceleration,
                    yawRate,
                    accelerationControl,
                    lanePosition,
                    lateralAcceleration,
                    verticalAcceleration,
                    confidence);
        }
    }
}
