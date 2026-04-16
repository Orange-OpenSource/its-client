/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.roadsegment;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.SpeedLimit;

import java.util.List;

/**
 * Complete description of a road segment including its geometry and navigational lane information.
 * Named {@code RoadSegmentData} to avoid a name clash with the {@code RoadSegment} road-object class.
 *
 * @param name Optional. Human-readable segment name (debug use).
 * @param id Globally unique road-segment identifier.
 * @param revision Revision counter. Range: 0..127.
 * @param refPoint Reference anchor point for all lane offset calculations.
 * @param laneWidth Optional. Default lane width in cm. Range: 0..32767.
 * @param speedLimits Optional. Default speed limits for this segment.
 * @param roadLaneSet Lane set describing this road segment. Min 1, max 255.
 */
public record RoadSegmentData(
        String name,
        RoadSegmentReferenceId id,
        int revision,
        Position3D refPoint,
        Integer laneWidth,
        List<SpeedLimit> speedLimits,
        List<GenericLane> roadLaneSet) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private RoadSegmentReferenceId id;
        private Integer revision;
        private Position3D refPoint;
        private Integer laneWidth;
        private List<SpeedLimit> speedLimits;
        private List<GenericLane> roadLaneSet;

        private Builder() {}

        public Builder name(String name) { this.name = name; return this; }
        public Builder id(RoadSegmentReferenceId id) { this.id = id; return this; }
        public Builder revision(int revision) { this.revision = revision; return this; }
        public Builder refPoint(Position3D refPoint) { this.refPoint = refPoint; return this; }
        public Builder laneWidth(Integer laneWidth) { this.laneWidth = laneWidth; return this; }
        public Builder speedLimits(List<SpeedLimit> speedLimits) { this.speedLimits = speedLimits; return this; }
        public Builder roadLaneSet(List<GenericLane> roadLaneSet) { this.roadLaneSet = roadLaneSet; return this; }

        public RoadSegmentData build() {
            return new RoadSegmentData(
                    name,
                    requireNonNull(id, "id"),
                    requireNonNull(revision, "revision"),
                    requireNonNull(refPoint, "ref_point"),
                    laneWidth,
                    speedLimits,
                    requireNonNull(roadLaneSet, "road_lane_set"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

