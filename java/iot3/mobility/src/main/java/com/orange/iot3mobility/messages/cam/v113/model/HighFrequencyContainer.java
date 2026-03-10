/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.model;

/**
 * HighFrequencyContainer v1.1.3
 *
 * @param heading Unit: 0.1 degree. wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601)
 * @param speed Unit 0.01 m/s. standstill(0), oneCentimeterPerSec(1), unavailable(16383)
 * @param driveDirection forward (0), backward (1), unavailable (2)
 * @param vehicleLength tenCentimeters(1), outOfRange(1022), unavailable(1023)
 * @param vehicleWidth tenCentimeters(1), outOfRange(61), unavailable(62)
 * @param curvature straight(0), unavailable(1023)
 * @param curvatureCalculationMode Whether the yaw rate is used to calculate the curvature: yawRateUsed(0),
 *                                 yawRateNotUsed(1), unavailable(2)
 * @param longitudinalAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquaredForward(1),
 *                                 pointOneMeterPerSecSquaredBackward(-1), unavailable(161)
 * @param yawRate Unit: 0.01 degree/s: straight(0), degSec-000-01ToRight(-1), degSec-000-01ToLeft(1), unavailable(32767)
 * @param accelerationControl Current controlling mechanism for longitudinal movement of the vehicle. Represented as a
 *                            bit string: brakePedalEngaged (0), gasPedalEngaged (1), emergencyBrakeEngaged (2),
 *                            collisionWarningEngaged(3), accEngaged(4), cruiseControlEngaged(5), speedLimiterEngaged(6)
 * @param lanePosition offTheRoad(-1), innerHardShoulder(0), innermostDrivingLane(1), secondLaneFromInside(2),
 *                     outerHardShoulder(14)
 * @param lateralAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquaredToRight(-1),
 *                            pointOneMeterPerSecSquaredToLeft(1), unavailable(161)
 * @param verticalAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquaredUp(1), pointOneMeterPerSecSquaredDown(-1),
 *                             unavailable(161)
 * @param confidence {@link PositionConfidence}
 */
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

    /**
     * Builder for BasicContainer.
     * <p>
     * All fields are optional.
     */
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
