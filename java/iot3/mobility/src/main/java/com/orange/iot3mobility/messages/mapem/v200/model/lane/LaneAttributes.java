/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneSharing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constant attribute information for a lane object.
 *
 * @param directionalUse Allowed travel directions. Use {@link LaneDirection} values. Min 1, max 2.
 * @param sharedWith     List of other user/mode types sharing this lane. Use {@link LaneSharing} values. Max 10.
 * @param laneType       Type-specific attribute flags; exactly one of the fields in {@link LaneType} should be non-null.
 */
public record LaneAttributes(List<String> directionalUse, List<String> sharedWith, LaneType laneType) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<String> directionalUse;
        private List<String> sharedWith;
        private LaneType laneType;

        private Builder() {}

        /** Sets the allowed travel directions using raw JSON strings. */
        public Builder directionalUse(List<String> directionalUse) {
            this.directionalUse = directionalUse;
            return this;
        }

        /**
         * Sets the allowed travel directions using typed enum constants.
         * <p>Example: {@code .directionalUse(LaneDirection.INGRESS_PATH)}
         *
         * @param directions one or two {@link LaneDirection} values
         * @return this builder
         */
        public Builder directionalUse(LaneDirection... directions) {
            this.directionalUse = Arrays.stream(directions).map(LaneDirection::value).toList();
            return this;
        }

        /** Sets the lane sharing modes using raw JSON strings. */
        public Builder sharedWith(List<String> sharedWith) {
            this.sharedWith = sharedWith;
            return this;
        }

        /**
         * Sets the lane sharing modes using typed enum constants.
         * Pass no arguments for an empty list.
         * <p>Example: {@code .sharedWith(LaneSharing.BUS_VEHICLE_TRAFFIC)}
         *
         * @param sharing zero or more {@link LaneSharing} values
         * @return this builder
         */
        public Builder sharedWith(LaneSharing... sharing) {
            this.sharedWith = sharing.length == 0
                    ? Collections.emptyList()
                    : Arrays.stream(sharing).map(LaneSharing::value).toList();
            return this;
        }

        public Builder laneType(LaneType laneType) {
            this.laneType = laneType;
            return this;
        }

        public LaneAttributes build() {
            return new LaneAttributes(
                    requireNonNull(directionalUse, "directional_use"),
                    requireNonNull(sharedWith, "shared_with"),
                    requireNonNull(laneType, "lane_type"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
