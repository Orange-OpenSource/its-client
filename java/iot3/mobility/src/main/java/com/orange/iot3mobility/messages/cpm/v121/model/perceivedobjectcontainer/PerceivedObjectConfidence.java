package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Confidence values for a perceived object.
 *
 * @param xDistance Distance confidence in x-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param yDistance Distance confidence in y-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param xSpeed Speed confidence in x-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param ySpeed Speed confidence in y-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param object Object confidence. Value: noConfidence(0), fullConfidence(15).
 * @param zDistance Distance confidence in z-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param zSpeed Speed confidence in z-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param xAcceleration Acceleration confidence in x-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param yAcceleration Acceleration confidence in y-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param zAcceleration Acceleration confidence in z-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param rollAngle Roll angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param pitchAngle Pitch angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param yawAngle Yaw angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param rollRate Roll rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param pitchRate Pitch rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param yawRate Yaw rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param rollAcceleration Roll acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param pitchAcceleration Pitch acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param yawAcceleration Yaw acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param planarObjectDimension1 Accuracy of first dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param planarObjectDimension2 Accuracy of second dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param verticalObjectDimension Accuracy of vertical dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param longitudinalLanePosition Accuracy of longitudinal lane position. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 */
public record PerceivedObjectConfidence(
        int xDistance,
        int yDistance,
        int xSpeed,
        int ySpeed,
        int object,
        Integer zDistance,
        Integer zSpeed,
        Integer xAcceleration,
        Integer yAcceleration,
        Integer zAcceleration,
        Integer rollAngle,
        Integer pitchAngle,
        Integer yawAngle,
        Integer rollRate,
        Integer pitchRate,
        Integer yawRate,
        Integer rollAcceleration,
        Integer pitchAcceleration,
        Integer yawAcceleration,
        Integer planarObjectDimension1,
        Integer planarObjectDimension2,
        Integer verticalObjectDimension,
        Integer longitudinalLanePosition) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer xDistance;
        private Integer yDistance;
        private Integer xSpeed;
        private Integer ySpeed;
        private Integer object;
        private Integer zDistance;
        private Integer zSpeed;
        private Integer xAcceleration;
        private Integer yAcceleration;
        private Integer zAcceleration;
        private Integer rollAngle;
        private Integer pitchAngle;
        private Integer yawAngle;
        private Integer rollRate;
        private Integer pitchRate;
        private Integer yawRate;
        private Integer rollAcceleration;
        private Integer pitchAcceleration;
        private Integer yawAcceleration;
        private Integer planarObjectDimension1;
        private Integer planarObjectDimension2;
        private Integer verticalObjectDimension;
        private Integer longitudinalLanePosition;

        public Builder distance(int xDistance, int yDistance) {
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            return this;
        }

        public Builder speed(int xSpeed, int ySpeed) {
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            return this;
        }

        public Builder object(int object) {
            this.object = object;
            return this;
        }

        public Builder zDistance(Integer zDistance) {
            this.zDistance = zDistance;
            return this;
        }

        public Builder zSpeed(Integer zSpeed) {
            this.zSpeed = zSpeed;
            return this;
        }

        public Builder acceleration(Integer xAcceleration, Integer yAcceleration) {
            this.xAcceleration = xAcceleration;
            this.yAcceleration = yAcceleration;
            return this;
        }

        public Builder zAcceleration(Integer zAcceleration) {
            this.zAcceleration = zAcceleration;
            return this;
        }

        public Builder rollAngle(Integer rollAngle) {
            this.rollAngle = rollAngle;
            return this;
        }

        public Builder pitchAngle(Integer pitchAngle) {
            this.pitchAngle = pitchAngle;
            return this;
        }

        public Builder yawAngle(Integer yawAngle) {
            this.yawAngle = yawAngle;
            return this;
        }

        public Builder rollRate(Integer rollRate) {
            this.rollRate = rollRate;
            return this;
        }

        public Builder pitchRate(Integer pitchRate) {
            this.pitchRate = pitchRate;
            return this;
        }

        public Builder yawRate(Integer yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder rollAcceleration(Integer rollAcceleration) {
            this.rollAcceleration = rollAcceleration;
            return this;
        }

        public Builder pitchAcceleration(Integer pitchAcceleration) {
            this.pitchAcceleration = pitchAcceleration;
            return this;
        }

        public Builder yawAcceleration(Integer yawAcceleration) {
            this.yawAcceleration = yawAcceleration;
            return this;
        }

        public Builder planarObjectDimension(Integer planarObjectDimension1, Integer planarObjectDimension2) {
            this.planarObjectDimension1 = planarObjectDimension1;
            this.planarObjectDimension2 = planarObjectDimension2;
            return this;
        }

        public Builder verticalObjectDimension(Integer verticalObjectDimension) {
            this.verticalObjectDimension = verticalObjectDimension;
            return this;
        }

        public Builder longitudinalLanePosition(Integer longitudinalLanePosition) {
            this.longitudinalLanePosition = longitudinalLanePosition;
            return this;
        }

        public PerceivedObjectConfidence build() {
            return new PerceivedObjectConfidence(
                    requireNonNull(xDistance, "x_distance"),
                    requireNonNull(yDistance, "y_distance"),
                    requireNonNull(xSpeed, "x_speed"),
                    requireNonNull(ySpeed, "y_speed"),
                    requireNonNull(object, "object"),
                    zDistance,
                    zSpeed,
                    xAcceleration,
                    yAcceleration,
                    zAcceleration,
                    rollAngle,
                    pitchAngle,
                    yawAngle,
                    rollRate,
                    pitchRate,
                    yawRate,
                    rollAcceleration,
                    pitchAcceleration,
                    yawAcceleration,
                    planarObjectDimension1,
                    planarObjectDimension2,
                    verticalObjectDimension,
                    longitudinalLanePosition);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
