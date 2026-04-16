/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.intersection;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.SpeedLimit;

import java.util.List;

/**
 * Full geometry description of one intersection including its reference point and lane set.
 *
 * @param name Optional. Human-readable intersection name (debug use).
 * @param id Globally unique intersection identifier.
 * @param revision Revision counter. Incremented on each geometry change. Range: 0..127.
 * @param refPoint Reference anchor point (lat/lon in 1/10 micro-degree) for all lane offset calculations.
 * @param laneWidth Optional. Default lane width in cm used by all lanes unless overridden. Range: 0..32767.
 * @param speedLimits Optional. Default speed limits applicable to all lanes unless overridden.
 * @param laneSet Ordered list of all lanes in this intersection. Min 1, max 255.
 */
public record IntersectionGeometry(
        String name,
        IntersectionReferenceId id,
        int revision,
        Position3D refPoint,
        Integer laneWidth,
        List<SpeedLimit> speedLimits,
        List<GenericLane> laneSet) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private IntersectionReferenceId id;
        private Integer revision;
        private Position3D refPoint;
        private Integer laneWidth;
        private List<SpeedLimit> speedLimits;
        private List<GenericLane> laneSet;

        private Builder() {}

        public Builder name(String name) { this.name = name; return this; }
        public Builder id(IntersectionReferenceId id) { this.id = id; return this; }
        public Builder revision(int revision) { this.revision = revision; return this; }
        public Builder refPoint(Position3D refPoint) { this.refPoint = refPoint; return this; }
        public Builder laneWidth(Integer laneWidth) { this.laneWidth = laneWidth; return this; }
        public Builder speedLimits(List<SpeedLimit> speedLimits) { this.speedLimits = speedLimits; return this; }
        public Builder laneSet(List<GenericLane> laneSet) { this.laneSet = laneSet; return this; }

        public IntersectionGeometry build() {
            return new IntersectionGeometry(
                    name,
                    requireNonNull(id, "id"),
                    requireNonNull(revision, "revision"),
                    requireNonNull(refPoint, "ref_point"),
                    laneWidth,
                    speedLimits,
                    requireNonNull(laneSet, "lane_set"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

