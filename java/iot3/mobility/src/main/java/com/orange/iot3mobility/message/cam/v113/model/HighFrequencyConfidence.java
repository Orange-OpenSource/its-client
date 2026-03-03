package com.orange.iot3mobility.message.cam.v113.model;

/**
 * HighFrequencyConfidence v1.1.3
 *
 * @param heading equalOrWithinZeroPointOneDegree (1), equalOrWithinOneDegree (10), outOfRange(126), unavailable(127)
 * @param speed equalOrWithinOneCentimeterPerSec(1), equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127)
 * @param vehicleLength noTrailerPresent(0), trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2),
 *                      trailerPresenceIsUnknown(3), unavailable(4)
 * @param yawRate degSec-000-01 (0), degSec-000-05 (1), degSec-000-10 (2), degSec-001-00 (3), degSec-005-00 (4),
 *                degSec-010-00 (5), degSec-100-00 (6), outOfRange (7), unavailable (8)
 * @param longitudinalAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
 * @param curvature onePerMeter-0-00002 (0), onePerMeter-0-0001 (1), onePerMeter-0-0005 (2), onePerMeter-0-002 (3),
 *                  onePerMeter-0-01 (4), onePerMeter-0-1 (5), outOfRange (6), unavailable (7)
 * @param lateralAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
 * @param verticalAcceleration Unit: 0.1 m/s2. pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
 */
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