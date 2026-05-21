/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.AllowedManeuver;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.SpeedLimit;

import java.util.Arrays;
import java.util.List;

/**
 * A generic lane object describing a single lane (motorized, crosswalk, bicycle, sidewalk, etc.)
 * within an intersection or road segment.
 *
 * @param laneId Intersection-unique lane identifier. Range: 0..255.
 * @param name Optional. Human-readable name (debug use only).
 * @param ingressApproach Optional. Approach ID of the ingress approach. Range: 0..15.
 * @param egressApproach Optional. Approach ID of the egress approach. Range: 0..15.
 * @param laneAttributes Constant attribute information describing the lane type and direction.
 * @param maneuvers Optional. Allowed manoeuvres from this lane. Use {@link AllowedManeuver} values.
 * @param nodeList The ordered node list or computed lane geometry.
 * @param connectsTo Optional. List of outbound connections beyond the stop line. Max 16.
 * @param speedLimits Optional. Speed limits applicable to this lane.
 * @param overlays Optional. Lane IDs of spatially overlapping lane objects. Max 5.
 */
public record GenericLane(
        int laneId,
        String name,
        Integer ingressApproach,
        Integer egressApproach,
        LaneAttributes laneAttributes,
        List<String> maneuvers,
        NodeList nodeList,
        List<ConnectsTo> connectsTo,
        List<SpeedLimit> speedLimits,
        List<Integer> overlays) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer laneId;
        private String name;
        private Integer ingressApproach;
        private Integer egressApproach;
        private LaneAttributes laneAttributes;
        private List<String> maneuvers;
        private NodeList nodeList;
        private List<ConnectsTo> connectsTo;
        private List<SpeedLimit> speedLimits;
        private List<Integer> overlays;

        private Builder() {}

        public Builder laneId(int laneId) { this.laneId = laneId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder ingressApproach(Integer ingressApproach) { this.ingressApproach = ingressApproach; return this; }
        public Builder egressApproach(Integer egressApproach) { this.egressApproach = egressApproach; return this; }
        public Builder laneAttributes(LaneAttributes laneAttributes) { this.laneAttributes = laneAttributes; return this; }
        public Builder maneuvers(List<String> maneuvers) { this.maneuvers = maneuvers; return this; }

        /**
         * Sets the allowed manoeuvres using typed enum constants.
         * <p>Example: {@code .maneuvers(AllowedManeuver.MANEUVER_STRAIGHT_ALLOWED, AllowedManeuver.MANEUVER_LEFT_ALLOWED)}
         *
         * @param maneuvers zero or more {@link AllowedManeuver} values
         * @return this builder
         */
        public Builder maneuvers(AllowedManeuver... maneuvers) {
            this.maneuvers = Arrays.stream(maneuvers).map(AllowedManeuver::value).toList();
            return this;
        }
        public Builder nodeList(NodeList nodeList) { this.nodeList = nodeList; return this; }
        public Builder connectsTo(List<ConnectsTo> connectsTo) { this.connectsTo = connectsTo; return this; }
        public Builder speedLimits(List<SpeedLimit> speedLimits) { this.speedLimits = speedLimits; return this; }
        public Builder overlays(List<Integer> overlays) { this.overlays = overlays; return this; }

        public GenericLane build() {
            return new GenericLane(
                    requireNonNull(laneId, "lane_id"),
                    name,
                    ingressApproach,
                    egressApproach,
                    requireNonNull(laneAttributes, "lane_attributes"),
                    maneuvers,
                    requireNonNull(nodeList, "node_list"),
                    connectsTo,
                    speedLimits,
                    overlays);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

