/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;

import java.util.List;

/**
 * Vehicle manoeuvre container: carries the originating vehicle's current state and sub-manoeuvres.
 *
 * @param mcmGenericCurrentStateContainer Generic state (type, ID, concept, rational).
 * @param vehicleCurrentStateContainer    Vehicle-specific state (strategy, speed, heading, size).
 * @param submaneuvres                    List of sub-manoeuvres.
 * @param manoeuvreAdvice                 Optional. Advice entries directed to other vehicles.
 */
public record VehicleManoeuvreContainer(
        McmGenericCurrentStateContainer mcmGenericCurrentStateContainer,
        VehicleCurrentStateContainer vehicleCurrentStateContainer,
        List<Submanoeuvre> submaneuvres,
        List<ManoeuvreAdvice> manoeuvreAdvice) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private McmGenericCurrentStateContainer mcmGenericCurrentStateContainer;
        private VehicleCurrentStateContainer vehicleCurrentStateContainer;
        private List<Submanoeuvre> submaneuvres;
        private List<ManoeuvreAdvice> manoeuvreAdvice;

        private Builder() {}

        public Builder mcmGenericCurrentStateContainer(McmGenericCurrentStateContainer mcmGenericCurrentStateContainer) {
            this.mcmGenericCurrentStateContainer = mcmGenericCurrentStateContainer;
            return this;
        }

        public Builder vehicleCurrentStateContainer(VehicleCurrentStateContainer vehicleCurrentStateContainer) {
            this.vehicleCurrentStateContainer = vehicleCurrentStateContainer;
            return this;
        }

        public Builder submaneuvres(List<Submanoeuvre> submaneuvres) {
            this.submaneuvres = submaneuvres;
            return this;
        }

        public Builder manoeuvreAdvice(List<ManoeuvreAdvice> manoeuvreAdvice) {
            this.manoeuvreAdvice = manoeuvreAdvice;
            return this;
        }

        public VehicleManoeuvreContainer build() {
            return new VehicleManoeuvreContainer(
                    requireNonNull(mcmGenericCurrentStateContainer, "mcm_generic_current_state_container"),
                    requireNonNull(vehicleCurrentStateContainer, "vehicle_current_state_container"),
                    requireNonNull(submaneuvres, "submaneuvres"),
                    manoeuvreAdvice);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

