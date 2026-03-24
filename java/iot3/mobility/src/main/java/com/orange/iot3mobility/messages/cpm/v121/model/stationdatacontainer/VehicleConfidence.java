/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Confidence values for originating vehicle container.
 *
 * @param heading Heading accuracy of the vehicle movement with regards to the true north. Unit: 0.1 degree. 
 *                Value: equalOrWithinZeroPointOneDegree(1), equalOrWithinOneDegree(10), outOfRange(126), 
 *                unavailable(127).
 * @param speed Speed accuracy. Unit: 0.01 m/s. Value: equalOrWithinOneCentimeterPerSec(1), 
 *              equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127).
 * @param vehicleLength Optional. Trailer presence/length confidence. Value: noTrailerPresent(0), 
 *                      trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2), 
 *                      trailerPresenceIsUnknown(3), unavailable(4).
 * @param yawRate Optional. Yaw rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), 
 *                degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param longitudinalAcceleration Optional. Longitudinal acceleration confidence. 
 *                                 Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param lateralAcceleration Optional. Lateral acceleration confidence. Unit: 0.1 m/s2. 
 *                            Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param verticalAcceleration Optional. Vertical acceleration confidence. Unit: 0.1 m/s2. 
 *                             Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 */
public record VehicleConfidence(
        int heading,
        int speed,
        Integer vehicleLength,
        Integer yawRate,
        Integer longitudinalAcceleration,
        Integer lateralAcceleration,
        Integer verticalAcceleration) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer heading;
        private Integer speed;
        private Integer vehicleLength;
        private Integer yawRate;
        private Integer longitudinalAcceleration;
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

        public Builder vehicleLength(Integer vehicleLength) {
            this.vehicleLength = vehicleLength;
            return this;
        }

        public Builder yawRate(Integer yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder longitudinalAcceleration(Integer longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
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

        public VehicleConfidence build() {
            return new VehicleConfidence(
                    requireNonNull(heading, "heading"),
                    requireNonNull(speed, "speed"),
                    vehicleLength,
                    yawRate,
                    longitudinalAcceleration,
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
