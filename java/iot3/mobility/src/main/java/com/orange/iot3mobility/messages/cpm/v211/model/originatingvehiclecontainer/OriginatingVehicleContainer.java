/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

import java.util.List;

/**
 * Originating vehicle container
 *
 * @param orientationAngle {@link Angle} absolute orientation of the disseminating vehicle in the WGS84 coordinate
 *                         system with respect to true North. Unit: 0,1 degrees.
 * @param pitchAngle Optional. {@link Angle} between the ground plane and the current orientation of the vehicle's
 *                   x-axis with respect to the ground plane about the y-axis according to ISO 8855. Unit: 0,1 degrees.
 * @param rollAngle Optional. {@link Angle} between the ground plane and the current orientation of a vehicle's y-axis
 *                  with respect to the ground plane about the x-axis according to ISO 8855. Unit: 0,1 degrees.
 * @param trailerDataSet Optional. List of {@link TrailerData} for attached trailers.
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
