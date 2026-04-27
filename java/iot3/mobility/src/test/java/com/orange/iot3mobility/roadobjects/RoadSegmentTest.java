/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemVersion;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;
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
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadSegmentTest {

    private static final int REGION_ID = 0;
    private static final int SEGMENT_ID = 2001;

    // ─── accessors ─────────────────────────────────────────────────────────────

    @Test
    void accessorsReturnConstructorValues() {
        LatLng refPoint = new LatLng(48.5, 2.2);
        MapemCodec.MapemFrame<?> frame = buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0);
        RoadSegment roadSegment = new RoadSegment(refPoint, 0, REGION_ID, SEGMENT_ID, frame);

        assertEquals(REGION_ID, roadSegment.getRegionId());
        assertEquals(SEGMENT_ID, roadSegment.getSegmentId());
        assertEquals(refPoint, roadSegment.getRefPoint());
        assertEquals(0, roadSegment.getRevision());
        assertEquals(frame, roadSegment.getMapemFrame());
    }

    // ─── revision guard ────────────────────────────────────────────────────────

    @Test
    void updateAcceptsHigherRevision() {
        RoadSegment roadSegment = new RoadSegment(
                new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID,
                buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0));

        LatLng newRefPoint = new LatLng(48.6, 2.3);
        MapemCodec.MapemFrame<?> newFrame = buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 1);
        boolean accepted = roadSegment.update(newRefPoint, 1, newFrame);

        assertTrue(accepted);
        assertEquals(1, roadSegment.getRevision());
        assertEquals(newRefPoint, roadSegment.getRefPoint());
    }

    @Test
    void updateRejectsSameRevision() {
        RoadSegment roadSegment = new RoadSegment(
                new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID,
                buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0));

        boolean accepted = roadSegment.update(new LatLng(48.6, 2.3), 0,
                buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0));

        assertFalse(accepted);
        assertEquals(0, roadSegment.getRevision());
    }

    @Test
    void updateRejectsLowerRevision() {
        RoadSegment roadSegment = new RoadSegment(
                new LatLng(48.5, 2.2), 5, REGION_ID, SEGMENT_ID,
                buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 5));

        boolean accepted = roadSegment.update(new LatLng(48.6, 2.3), 4,
                buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 4));

        assertFalse(accepted);
        assertEquals(5, roadSegment.getRevision());
    }

    // ─── getLanes ──────────────────────────────────────────────────────────────

    @Test
    void getLanesReturnsCenterLineForSegment() {
        MapemCodec.MapemFrame<?> frame = buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0);
        RoadSegment roadSegment = new RoadSegment(
                new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID, frame);

        List<LaneGeometry> laneGeometries = roadSegment.getLanes();

        assertFalse(laneGeometries.isEmpty());
        assertEquals(1, laneGeometries.get(0).laneId());
        assertEquals(2, laneGeometries.get(0).centerLine().size());
    }

    @Test
    void getLanesReturnsEmptyForUnknownSegment() {
        MapemCodec.MapemFrame<?> frame = buildMapemFrameWithSegment(REGION_ID, SEGMENT_ID, 0);
        // Segment ID 9999 is not in the frame
        RoadSegment roadSegment = new RoadSegment(
                new LatLng(48.5, 2.2), 0, REGION_ID, 9999, frame);

        assertTrue(roadSegment.getLanes().isEmpty());
    }

    // ─── frame builder ─────────────────────────────────────────────────────────

    private static MapemCodec.MapemFrame<?> buildMapemFrameWithSegment(int regionId, int segmentId, int revision) {
        NodeXYOffset nodeXYOffset = new NodeXYOffset(0, 0);
        NodeDelta nodeDelta = new NodeDelta(nodeXYOffset, null);
        NodeXY nodeXY = NodeXY.builder().delta(nodeDelta).build();
        NodeList nodeList = new NodeList(List.of(nodeXY, nodeXY), null);

        LaneType laneType = new LaneType(List.of(), null, null, null, null, null, null, null);
        LaneAttributes laneAttributes = LaneAttributes.builder()
                .directionalUse(LaneDirection.EGRESS_PATH)
                .sharedWith()
                .laneType(laneType)
                .build();

        GenericLane genericLane = GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .build();

        Position3D refPoint = Position3D.builder()
                .latitude(485000000)
                .longitude(22000000)
                .build();

        RoadSegmentReferenceId roadSegmentReferenceId =
                new RoadSegmentReferenceId(regionId == 0 ? null : regionId, segmentId);
        RoadSegmentData roadSegmentData = RoadSegmentData.builder()
                .id(roadSegmentReferenceId)
                .revision(revision)
                .refPoint(refPoint)
                .roadLaneSet(List.of(genericLane))
                .build();

        MapemMessage200 mapemMessage = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .roadSegments(List.of(roadSegmentData))
                .build();

        MapemEnvelope200 mapemEnvelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(System.currentTimeMillis())
                .message(mapemMessage)
                .build();

        return new MapemCodec.MapemFrame<>(MapemVersion.V2_0_0, mapemEnvelope);
    }
}

