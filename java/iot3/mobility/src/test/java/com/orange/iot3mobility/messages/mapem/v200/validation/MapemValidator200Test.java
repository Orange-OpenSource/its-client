/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mapem.v200.validation;

import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneAttributes;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneType;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeDelta;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeList;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXYOffset;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MapemValidator200Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        MapemEnvelope200 envelope = validEnvelope();
        MapemValidator200.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeAcceptsRoadSegmentOnly() {
        RoadSegmentReferenceId roadSegmentReferenceId = new RoadSegmentReferenceId(null, 2001);
        RoadSegmentData roadSegmentData = RoadSegmentData.builder()
                .id(roadSegmentReferenceId)
                .revision(0)
                .refPoint(validRefPoint())
                .roadLaneSet(List.of(validGenericLane()))
                .build();

        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .roadSegments(List.of(roadSegmentData))
                .build();

        MapemEnvelope200 envelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_road_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        MapemValidator200.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsBlankSourceUuid() {
        MapemEnvelope200 envelope = new MapemEnvelope200(
                "mapem", "self", "2.0.0", "  ", 1514764800000L, validMessage());
        assertThrows(MapemValidationException.class, () -> MapemValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateMessageRejectsNoIntersectionAndNoRoadSegment() {
        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .build();

        MapemEnvelope200 envelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(MapemValidationException.class, () -> MapemValidator200.validateEnvelope(envelope));
    }

    @Test
    void validateMessageRejectsMsgIssueRevisionOutOfRange() {
        MapemMessage200 message = new MapemMessage200(
                1, 42L, null, 128, null, null,
                List.of(validIntersectionGeometry()), null, null, null);

        MapemEnvelope200 envelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(MapemValidationException.class, () -> MapemValidator200.validateEnvelope(envelope));
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private static MapemEnvelope200 validEnvelope() {
        return MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static MapemMessage200 validMessage() {
        return MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .intersections(List.of(validIntersectionGeometry()))
                .build();
    }

    private static IntersectionGeometry validIntersectionGeometry() {
        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(null, 1001);
        return IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(validRefPoint())
                .laneSet(List.of(validGenericLane()))
                .build();
    }

    private static Position3D validRefPoint() {
        return Position3D.builder().latitude(485000000).longitude(22000000).build();
    }

    private static GenericLane validGenericLane() {
        NodeXYOffset nodeXYOffset = new NodeXYOffset(0, 0);
        NodeDelta nodeDelta = new NodeDelta(nodeXYOffset, null);
        NodeXY nodeXY = NodeXY.builder().delta(nodeDelta).build();
        NodeList nodeList = new NodeList(List.of(nodeXY, nodeXY), null);
        LaneType laneType = new LaneType(List.of(), null, null, null, null, null, null, null);
        LaneAttributes laneAttributes = LaneAttributes.builder()
                .directionalUse(LaneDirection.INGRESS_PATH)
                .sharedWith()
                .laneType(laneType)
                .build();
        return GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .build();
    }
}

