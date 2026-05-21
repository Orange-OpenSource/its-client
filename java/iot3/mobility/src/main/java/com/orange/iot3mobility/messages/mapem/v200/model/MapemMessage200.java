/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model;

import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.DataParameters;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.RestrictionClassAssignment;

import java.util.List;

/**
 * MAPEM message payload.
 *
 * @param protocolVersion Version of the message/communication protocol. Range: 0..255.
 * @param stationId Originating station identifier. Range: 0..4294967295.
 * @param timestamp Optional. Minute of the year at generation time.
 * @param msgIssueRevision Overall message revision counter. Shall be set to 0 in this profile. Range: 0..127.
 * @param layerType Optional. Type of information in this map layer.
 * @param layerId Optional. Fragment index for large MapData descriptions. Range: 0..100.
 * @param intersections Optional. List of intersection geometry entries. Min 1, max 32.
 * @param roadSegments Optional. List of road segment entries. Min 1, max 32.
 * @param dataParameters Optional. Static metadata about how the map fragment was produced.
 * @param restrictionList Optional. List of restriction class assignments. Min 1, max 254.
 */
public record MapemMessage200(
        int protocolVersion,
        long stationId,
        Integer timestamp,
        int msgIssueRevision,
        String layerType,
        Integer layerId,
        List<IntersectionGeometry> intersections,
        List<RoadSegmentData> roadSegments,
        DataParameters dataParameters,
        List<RestrictionClassAssignment> restrictionList) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private Integer timestamp;
        private Integer msgIssueRevision;
        private String layerType;
        private Integer layerId;
        private List<IntersectionGeometry> intersections;
        private List<RoadSegmentData> roadSegments;
        private DataParameters dataParameters;
        private List<RestrictionClassAssignment> restrictionList;

        private Builder() {}

        public Builder protocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; return this; }
        public Builder stationId(long stationId) { this.stationId = stationId; return this; }
        public Builder timestamp(Integer timestamp) { this.timestamp = timestamp; return this; }
        public Builder msgIssueRevision(int msgIssueRevision) { this.msgIssueRevision = msgIssueRevision; return this; }
        public Builder layerType(String layerType) { this.layerType = layerType; return this; }
        public Builder layerId(Integer layerId) { this.layerId = layerId; return this; }
        public Builder intersections(List<IntersectionGeometry> intersections) { this.intersections = intersections; return this; }
        public Builder roadSegments(List<RoadSegmentData> roadSegments) { this.roadSegments = roadSegments; return this; }
        public Builder dataParameters(DataParameters dataParameters) { this.dataParameters = dataParameters; return this; }
        public Builder restrictionList(List<RestrictionClassAssignment> restrictionList) { this.restrictionList = restrictionList; return this; }

        public MapemMessage200 build() {
            return new MapemMessage200(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    timestamp,
                    requireNonNull(msgIssueRevision, "msg_issue_revision"),
                    layerType,
                    layerId,
                    intersections,
                    roadSegments,
                    dataParameters,
                    restrictionList);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

