/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.WayPoint;

import java.util.List;

/**
 * An advised sub-manoeuvre item within a manoeuvre advice entry.
 *
 * @param submanoeuvreId        Unique ID [0..255].
 * @param advisedTrajectory     Optional. Advised trajectory (list of waypoints [1..10]).
 * @param advisedTargetRoadResource Optional. Advisory TRR container.
 */
public record AdvisedSubmanoeuvre(
        int submanoeuvreId,
        List<WayPoint> advisedTrajectory,
        AdvisedTrrContainer advisedTargetRoadResource) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer submanoeuvreId;
        private List<WayPoint> advisedTrajectory;
        private AdvisedTrrContainer advisedTargetRoadResource;

        private Builder() {}

        public Builder submanoeuvreId(int submanoeuvreId) {
            this.submanoeuvreId = submanoeuvreId;
            return this;
        }

        public Builder advisedTrajectory(List<WayPoint> advisedTrajectory) {
            this.advisedTrajectory = advisedTrajectory;
            return this;
        }

        public Builder advisedTargetRoadResource(AdvisedTrrContainer advisedTargetRoadResource) {
            this.advisedTargetRoadResource = advisedTargetRoadResource;
            return this;
        }

        public AdvisedSubmanoeuvre build() {
            return new AdvisedSubmanoeuvre(
                    requireNonNull(submanoeuvreId, "submanoeuvre_id"),
                    advisedTrajectory,
                    advisedTargetRoadResource);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

