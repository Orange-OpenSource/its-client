/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.VehicleLengthConfidenceIndication;

/**
 * Vehicle length with a confidence indication about trailer presence.
 *
 * @param vehicleLengthValue Vehicle length in 0.1 m steps [1..1023].
 * @param vehicleLengthConfidenceIndication Trailer presence indication [0..4].
 *   noTrailerPresent(0), trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2),
 *   trailerPresenceIsUnknown(3), unavailable(4).
 */
public record VehicleLength(int vehicleLengthValue, int vehicleLengthConfidenceIndication) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer vehicleLengthValue;
        private Integer vehicleLengthConfidenceIndication;

        private Builder() {}

        public Builder vehicleLengthValue(int vehicleLengthValue) {
            this.vehicleLengthValue = vehicleLengthValue;
            return this;
        }

        public Builder vehicleLengthConfidenceIndication(int vehicleLengthConfidenceIndication) {
            this.vehicleLengthConfidenceIndication = vehicleLengthConfidenceIndication;
            return this;
        }

        /**
         * Sets the vehicle length confidence indication using the typed enum constant.
         *
         * @param indication {@link VehicleLengthConfidenceIndication} value
         * @return this builder
         */
        public Builder vehicleLengthConfidenceIndication(VehicleLengthConfidenceIndication indication) {
            this.vehicleLengthConfidenceIndication = indication.value;
            return this;
        }

        public VehicleLength build() {
            return new VehicleLength(
                    requireNonNull(vehicleLengthValue, "vehicle_length_value"),
                    requireNonNull(vehicleLengthConfidenceIndication, "vehicle_length_confidence_indication"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

