/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

/**
 * Physical size of a vehicle.
 *
 * @param vehicleType ISO 3833 vehicle type [0..255].
 * @param vehicleTransportedGoods Optional. Special transport goods flags.
 * @param vehicleLength Vehicle length with trailer confidence.
 * @param vehicleWidth Vehicle width in 0.1 m steps, excluding mirrors [1..62].
 * @param vehicleHeight Vehicle height in 0.05 m steps from ground to highest point [1..128].
 */
public record VehicleSize(
        int vehicleType,
        VehicleTransportedGoods vehicleTransportedGoods,
        VehicleLength vehicleLength,
        int vehicleWidth,
        int vehicleHeight) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer vehicleType;
        private VehicleTransportedGoods vehicleTransportedGoods;
        private VehicleLength vehicleLength;
        private Integer vehicleWidth;
        private Integer vehicleHeight;

        private Builder() {}

        public Builder vehicleType(int vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Builder vehicleTransportedGoods(VehicleTransportedGoods vehicleTransportedGoods) {
            this.vehicleTransportedGoods = vehicleTransportedGoods;
            return this;
        }

        public Builder vehicleLength(VehicleLength vehicleLength) {
            this.vehicleLength = vehicleLength;
            return this;
        }

        public Builder vehicleWidth(int vehicleWidth) {
            this.vehicleWidth = vehicleWidth;
            return this;
        }

        public Builder vehicleHeight(int vehicleHeight) {
            this.vehicleHeight = vehicleHeight;
            return this;
        }

        public VehicleSize build() {
            return new VehicleSize(
                    requireNonNull(vehicleType, "vehicle_type"),
                    vehicleTransportedGoods,
                    requireNonNull(vehicleLength, "vehicle_lenth"),
                    requireNonNull(vehicleWidth, "vehicle_width"),
                    requireNonNull(vehicleHeight, "vehicle_height"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

