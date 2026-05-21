/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId;

/**
 * A connection entry linking this lane beyond the stop line to an outbound lane (possibly in a remote intersection).
 *
 * @param connectingLane The outbound lane and its associated allowed manoeuvre.
 * @param remoteIntersections Optional. Reference to a remote intersection when the connecting lane belongs to another intersection.
 * @param signalGroup Optional. Signal group ID that governs this connection (links MAP to SPAT).
 * @param restrictionClassId Optional. Restriction class governing who may use this connection.
 * @param connectionId Optional. Unique connection identifier within the intersection.
 */
public record ConnectsTo(
        ConnectingLane connectingLane,
        IntersectionReferenceId remoteIntersections,
        Integer signalGroup,
        Integer restrictionClassId,
        Integer connectionId) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ConnectingLane connectingLane;
        private IntersectionReferenceId remoteIntersections;
        private Integer signalGroup;
        private Integer restrictionClassId;
        private Integer connectionId;

        private Builder() {}

        public Builder connectingLane(ConnectingLane connectingLane) {
            this.connectingLane = connectingLane;
            return this;
        }

        public Builder remoteIntersections(IntersectionReferenceId remoteIntersections) {
            this.remoteIntersections = remoteIntersections;
            return this;
        }

        public Builder signalGroup(Integer signalGroup) {
            this.signalGroup = signalGroup;
            return this;
        }

        public Builder restrictionClassId(Integer restrictionClassId) {
            this.restrictionClassId = restrictionClassId;
            return this;
        }

        public Builder connectionId(Integer connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public ConnectsTo build() {
            return new ConnectsTo(
                    requireNonNull(connectingLane, "connecting_lane"),
                    remoteIntersections,
                    signalGroup,
                    restrictionClassId,
                    connectionId);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

