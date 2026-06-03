/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.KinematicsCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TemporalCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TrrDescription;

/**
 * Advisory TRR container: TRR description with temporal and kinematics characteristics.
 *
 * @param trrDescription         Geographic description of the target road resource.
 * @param temporalCharacteristics Temporal occupancy bounds.
 * @param kinematicsCharacteristics Kinematics (intentionally empty / ASN.1 NULL).
 */
public record AdvisedTrrContainer(
        TrrDescription trrDescription,
        TemporalCharacteristics temporalCharacteristics,
        KinematicsCharacteristics kinematicsCharacteristics) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TrrDescription trrDescription;
        private TemporalCharacteristics temporalCharacteristics;
        private KinematicsCharacteristics kinematicsCharacteristics;

        private Builder() {}

        public Builder trrDescription(TrrDescription trrDescription) {
            this.trrDescription = trrDescription;
            return this;
        }

        public Builder temporalCharacteristics(TemporalCharacteristics temporalCharacteristics) {
            this.temporalCharacteristics = temporalCharacteristics;
            return this;
        }

        public Builder kinematicsCharacteristics(KinematicsCharacteristics kinematicsCharacteristics) {
            this.kinematicsCharacteristics = kinematicsCharacteristics;
            return this;
        }

        public AdvisedTrrContainer build() {
            return new AdvisedTrrContainer(
                    requireNonNull(trrDescription, "trr_description"),
                    requireNonNull(temporalCharacteristics, "temporal_characteristics"),
                    requireNonNull(kinematicsCharacteristics, "kinematics_characteristics"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

