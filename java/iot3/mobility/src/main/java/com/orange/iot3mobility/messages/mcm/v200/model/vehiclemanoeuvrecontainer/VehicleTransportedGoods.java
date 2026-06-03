/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

/**
 * Special transport flags for vehicle transported goods.
 *
 * @param heavyLoad Whether the vehicle carries heavy load.
 * @param excessWidth Whether the vehicle has excess width.
 * @param excessLength Whether the vehicle has excess length.
 * @param excessHeight Whether the vehicle has excess height.
 */
public record VehicleTransportedGoods(
        boolean heavyLoad,
        boolean excessWidth,
        boolean excessLength,
        boolean excessHeight) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Boolean heavyLoad;
        private Boolean excessWidth;
        private Boolean excessLength;
        private Boolean excessHeight;

        private Builder() {}

        public Builder heavyLoad(boolean heavyLoad) {
            this.heavyLoad = heavyLoad;
            return this;
        }

        public Builder excessWidth(boolean excessWidth) {
            this.excessWidth = excessWidth;
            return this;
        }

        public Builder excessLength(boolean excessLength) {
            this.excessLength = excessLength;
            return this;
        }

        public Builder excessHeight(boolean excessHeight) {
            this.excessHeight = excessHeight;
            return this;
        }

        public VehicleTransportedGoods build() {
            return new VehicleTransportedGoods(
                    requireNonNull(heavyLoad, "heavy_load"),
                    requireNonNull(excessWidth, "excess_width"),
                    requireNonNull(excessLength, "excess_length"),
                    requireNonNull(excessHeight, "excess_height"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

