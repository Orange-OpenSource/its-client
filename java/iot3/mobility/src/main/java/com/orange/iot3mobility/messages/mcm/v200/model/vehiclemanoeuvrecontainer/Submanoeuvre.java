/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;

import java.util.List;

/**
 * A single sub-manoeuvre in a vehicle manoeuvre container.
 *
 * @param submanoeuvreId           Unique ID [0..255].
 * @param submanoeuvreStrategy     Optional. Intended sub-manoeuvre strategy.
 * @param referenceTrajectory      Optional. Reference trajectory (list of waypoints [1..10]).
 * @param targetRoadResourceIContainer Optional. TRR description (type 3 when trajectory is used).
 * @param temporalCharacteristics  Temporal occupancy bounds.
 * @param kinematicsCharacteristics Kinematics (intentionally empty).
 */
public record Submanoeuvre(
        int submanoeuvreId,
        ManoeuvreStrategy submanoeuvreStrategy,
        List<WayPoint> referenceTrajectory,
        TrrDescription targetRoadResourceIContainer,
        TemporalCharacteristics temporalCharacteristics,
        KinematicsCharacteristics kinematicsCharacteristics) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer submanoeuvreId;
        private ManoeuvreStrategy submanoeuvreStrategy;
        private List<WayPoint> referenceTrajectory;
        private TrrDescription targetRoadResourceIContainer;
        private TemporalCharacteristics temporalCharacteristics;
        private KinematicsCharacteristics kinematicsCharacteristics;

        private Builder() {}

        public Builder submanoeuvreId(int submanoeuvreId) {
            this.submanoeuvreId = submanoeuvreId;
            return this;
        }

        public Builder submanoeuvreStrategy(ManoeuvreStrategy submanoeuvreStrategy) {
            this.submanoeuvreStrategy = submanoeuvreStrategy;
            return this;
        }

        public Builder referenceTrajectory(List<WayPoint> referenceTrajectory) {
            this.referenceTrajectory = referenceTrajectory;
            return this;
        }

        public Builder targetRoadResourceIContainer(TrrDescription targetRoadResourceIContainer) {
            this.targetRoadResourceIContainer = targetRoadResourceIContainer;
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

        public Submanoeuvre build() {
            return new Submanoeuvre(
                    requireNonNull(submanoeuvreId, "submanoeuvre_id"),
                    submanoeuvreStrategy,
                    referenceTrajectory,
                    targetRoadResourceIContainer,
                    requireNonNull(temporalCharacteristics, "temporal_charateristics"),
                    requireNonNull(kinematicsCharacteristics, "kinematics_characteristics"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

