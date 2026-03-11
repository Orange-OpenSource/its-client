package com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

import java.util.List;

/**
 * Originating vehicle container.
 */
public record OriginatingVehicleContainer(
        Angle orientationAngle,
        Angle pitchAngle,
        Angle rollAngle,
        List<TrailerData> trailerDataSet) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Angle orientationAngle;
        private Angle pitchAngle;
        private Angle rollAngle;
        private List<TrailerData> trailerDataSet;

        public Builder orientationAngle(Angle orientationAngle) {
            this.orientationAngle = orientationAngle;
            return this;
        }

        public Builder pitchAngle(Angle pitchAngle) {
            this.pitchAngle = pitchAngle;
            return this;
        }

        public Builder rollAngle(Angle rollAngle) {
            this.rollAngle = rollAngle;
            return this;
        }

        public Builder trailerDataSet(List<TrailerData> trailerDataSet) {
            this.trailerDataSet = trailerDataSet;
            return this;
        }

        public OriginatingVehicleContainer build() {
            return new OriginatingVehicleContainer(
                    requireNonNull(orientationAngle, "orientation_angle"),
                    pitchAngle,
                    rollAngle,
                    trailerDataSet);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}

