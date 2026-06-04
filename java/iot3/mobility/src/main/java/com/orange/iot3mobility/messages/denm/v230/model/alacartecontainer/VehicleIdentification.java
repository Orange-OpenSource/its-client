/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer;

/**
 * VehicleIdentification - identification of a stationary vehicle.
 *
 * @param wMINumber Optional. World Manufacturer Identifier (WMI) of the vehicle (1..3 characters).
 * @param vDS       Optional. Vehicle Descriptor Section (VDS) of the VIN (exactly 6 characters).
 */
public record VehicleIdentification(
        String wMINumber,
        String vDS) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String wMINumber;
        private String vDS;

        public Builder wMINumber(String wMINumber) {
            this.wMINumber = wMINumber;
            return this;
        }

        public Builder vDS(String vDS) {
            this.vDS = vDS;
            return this;
        }

        public VehicleIdentification build() {
            return new VehicleIdentification(wMINumber, vDS);
        }
    }
}

