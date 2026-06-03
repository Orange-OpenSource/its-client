/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Speed;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Wgs84Angle;

/**
 * Current vehicle state for manoeuvre coordination.
 *
 * @param manoeuvreOverallStrategy The overall manoeuvre strategy.
 * @param vehicleSpeed Current speed.
 * @param vehicleHeading Current heading.
 * @param vehicleSize Physical dimensions of the vehicle.
 */
public record VehicleCurrentStateContainer(
        ManoeuvreStrategy manoeuvreOverallStrategy,
        Speed vehicleSpeed,
        Wgs84Angle vehicleHeading,
        VehicleSize vehicleSize) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ManoeuvreStrategy manoeuvreOverallStrategy;
        private Speed vehicleSpeed;
        private Wgs84Angle vehicleHeading;
        private VehicleSize vehicleSize;

        private Builder() {}

        public Builder manoeuvreOverallStrategy(ManoeuvreStrategy manoeuvreOverallStrategy) {
            this.manoeuvreOverallStrategy = manoeuvreOverallStrategy;
            return this;
        }

        public Builder vehicleSpeed(Speed vehicleSpeed) {
            this.vehicleSpeed = vehicleSpeed;
            return this;
        }

        public Builder vehicleHeading(Wgs84Angle vehicleHeading) {
            this.vehicleHeading = vehicleHeading;
            return this;
        }

        public Builder vehicleSize(VehicleSize vehicleSize) {
            this.vehicleSize = vehicleSize;
            return this;
        }

        public VehicleCurrentStateContainer build() {
            return new VehicleCurrentStateContainer(
                    requireNonNull(manoeuvreOverallStrategy, "manoeuvre_overall_strategy"),
                    requireNonNull(vehicleSpeed, "vehicle_speed"),
                    requireNonNull(vehicleHeading, "vehicle_heading"),
                    requireNonNull(vehicleSize, "vehicle_size"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

