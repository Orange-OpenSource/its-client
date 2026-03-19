/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Originating vehicle container.
 *
 * @param heading Heading of the vehicle movement with regards to the true north. Unit: 0.1 degree.
 *                Value: wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param speed Driving speed. Unit: 0.01 m/s. Value: standstill(0), oneCentimeterPerSec(1), unavailable(16383).
 * @param confidence {@link VehicleConfidence}
 * @param driveDirection Optional. Drive direction. Value: forward(0), backward(1), unavailable(2).
 * @param vehicleLength Optional. Vehicle length. Unit: 0.1 meter. Value: tenCentimeters(1), outOfRange(1022),
 *                      unavailable(1023).
 * @param vehicleWidth Optional. Vehicle width. Unit: 0.1 meter. Value: tenCentimeters(1), outOfRange(61),
 *                     unavailable(62).
 * @param longitudinalAcceleration Optional. Longitudinal acceleration. Unit: 0.1 m/s2.
 *                                 Value: pointOneMeterPerSecSquaredForward(1), pointOneMeterPerSecSquaredBackward(-1),
 *                                 unavailable(161).
 * @param yawRate Optional. Yaw rate. Unit: 0.01 degree/s. Value: straight(0), degSec-000-01ToRight(-1),
 *                degSec-000-01ToLeft(1), unavailable(32767).
 * @param lateralAcceleration Optional. Lateral acceleration. Unit: 0.1 m/s2.
 *                            Value: pointOneMeterPerSecSquaredToRight(-1), pointOneMeterPerSecSquaredToLeft(1),
 *                            unavailable(161).
 * @param verticalAcceleration Optional. Vertical acceleration. Unit: 0.1 m/s2.
 *                             Value: pointOneMeterPerSecSquaredUp(1), pointOneMeterPerSecSquaredDown(-1),
 *                             unavailable(161).
 */
public record OriginatingVehicleContainer(
        int heading,
        int speed,
        VehicleConfidence confidence,
        Integer driveDirection,
        Integer vehicleLength,
        Integer vehicleWidth,
        Integer longitudinalAcceleration,
        Integer yawRate,
        Integer lateralAcceleration,
        Integer verticalAcceleration) {
    
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer heading;
        private Integer speed;
        private VehicleConfidence confidence;
        private Integer driveDirection;
        private Integer vehicleLength;
        private Integer vehicleWidth;
        private Integer longitudinalAcceleration;
        private Integer yawRate;
        private Integer lateralAcceleration;
        private Integer verticalAcceleration;

        public Builder heading(int heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public Builder confidence(VehicleConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder driveDirection(Integer driveDirection) {
            this.driveDirection = driveDirection;
            return this;
        }

        public Builder vehicleLength(Integer vehicleLength) {
            this.vehicleLength = vehicleLength;
            return this;
        }

        public Builder vehicleWidth(Integer vehicleWidth) {
            this.vehicleWidth = vehicleWidth;
            return this;
        }

        public Builder longitudinalAcceleration(Integer longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            return this;
        }

        public Builder yawRate(Integer yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder lateralAcceleration(Integer lateralAcceleration) {
            this.lateralAcceleration = lateralAcceleration;
            return this;
        }

        public Builder verticalAcceleration(Integer verticalAcceleration) {
            this.verticalAcceleration = verticalAcceleration;
            return this;
        }

        public OriginatingVehicleContainer build() {
            return new OriginatingVehicleContainer(
                    requireNonNull(heading, "heading"),
                    requireNonNull(speed, "speed"),
                    requireNonNull(confidence, "confidence"),
                    driveDirection,
                    vehicleLength,
                    vehicleWidth,
                    longitudinalAcceleration,
                    yawRate,
                    lateralAcceleration,
                    verticalAcceleration);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
