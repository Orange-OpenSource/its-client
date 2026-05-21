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
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadGeometryTest {

    private static final int REGION_ID = 0;
    private static final int INTERSECTION_ID = 1001;
    private static final int SEGMENT_ID = 2001;

    // ─── uuid ──────────────────────────────────────────────────────────────────

    @Test
    void getUuidReturnsConstructedValue() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        assertEquals("rsu_42_100", roadGeometry.getUuid());
    }

    // ─── intersections ─────────────────────────────────────────────────────────

    @Test
    void updateIntersectionCreatesNewEntry() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        MapemCodec.MapemFrame<?> frame = buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0);

        boolean created = roadGeometry.updateIntersection(new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, frame);

        assertTrue(created);
        assertEquals(1, roadGeometry.getIntersections().size());
        assertNotNull(roadGeometry.getIntersection(REGION_ID, INTERSECTION_ID));
    }

    @Test
    void updateIntersectionSameRevisionRejected() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        MapemCodec.MapemFrame<?> frame = buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0);
        roadGeometry.updateIntersection(new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, frame);

        // Same revision → rejected
        boolean accepted = roadGeometry.updateIntersection(new LatLng(48.6, 2.3), 0, REGION_ID, INTERSECTION_ID, frame);

        assertFalse(accepted);
        assertEquals(1, roadGeometry.getIntersections().size());
    }

    @Test
    void updateIntersectionHigherRevisionAccepted() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        MapemCodec.MapemFrame<?> initialFrame = buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0);
        roadGeometry.updateIntersection(new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, initialFrame);

        MapemCodec.MapemFrame<?> updatedFrame = buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 1);
        boolean accepted = roadGeometry.updateIntersection(new LatLng(48.6, 2.3), 1, REGION_ID, INTERSECTION_ID, updatedFrame);

        assertTrue(accepted);
        assertEquals(1, roadIntersection(roadGeometry).getRevision(),
                "Revision should be updated to 1 after a higher-revision update");
    }

    @Test
    void getIntersectionByRegionAndIdReturnsNullForUnknown() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));

        assertNull(roadGeometry.getIntersection(99, 9999));
    }

    // ─── road segments ─────────────────────────────────────────────────────────

    @Test
    void updateSegmentCreatesNewEntry() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        MapemCodec.MapemFrame<?> frame = buildSegmentFrame(REGION_ID, SEGMENT_ID, 0);

        boolean created = roadGeometry.updateSegment(new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID, frame);

        assertTrue(created);
        assertEquals(1, roadGeometry.getSegments().size());
    }

    @Test
    void updateSegmentSameRevisionRejected() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        MapemCodec.MapemFrame<?> frame = buildSegmentFrame(REGION_ID, SEGMENT_ID, 0);
        roadGeometry.updateSegment(new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID, frame);

        boolean accepted = roadGeometry.updateSegment(new LatLng(48.6, 2.3), 0, REGION_ID, SEGMENT_ID, frame);

        assertFalse(accepted);
        assertEquals(1, roadGeometry.getSegments().size());
    }

    // ─── clearChildren ─────────────────────────────────────────────────────────

    @Test
    void clearChildrenRemovesAll() {
        RoadGeometry roadGeometry = new RoadGeometry("rsu_42_100", buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        roadGeometry.updateIntersection(new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID,
                buildIntersectionFrame(REGION_ID, INTERSECTION_ID, 0));
        roadGeometry.updateSegment(new LatLng(48.5, 2.2), 0, REGION_ID, SEGMENT_ID,
                buildSegmentFrame(REGION_ID, SEGMENT_ID, 0));
        assertEquals(1, roadGeometry.getIntersections().size());
        assertEquals(1, roadGeometry.getSegments().size());

        roadGeometry.clearChildren();

        assertTrue(roadGeometry.getIntersections().isEmpty());
        assertTrue(roadGeometry.getSegments().isEmpty());
        assertNull(roadGeometry.getIntersection(REGION_ID, INTERSECTION_ID));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    /** Retrieve the single RoadIntersection stored inside a RoadGeometry (test helper). */
    private static RoadIntersection roadIntersection(RoadGeometry roadGeometry) {
        return roadGeometry.getIntersections().get(0);
    }

    private static MapemCodec.MapemFrame<?> buildIntersectionFrame(int regionId, int intersectionId, int revision) {
        GenericLane genericLane = buildGenericLane();
        Position3D refPoint = Position3D.builder().latitude(485000000).longitude(22000000).build();

        IntersectionReferenceId intersectionReferenceId =
                new IntersectionReferenceId(regionId == 0 ? null : regionId, intersectionId);
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(revision)
                .refPoint(refPoint)
                .laneSet(List.of(genericLane))
                .build();

        MapemMessage200 mapemMessage = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .intersections(List.of(intersectionGeometry))
                .build();

        MapemEnvelope200 mapemEnvelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(System.currentTimeMillis())
                .message(mapemMessage)
                .build();

        return new MapemCodec.MapemFrame<>(MapemVersion.V2_0_0, mapemEnvelope);
    }

    private static MapemCodec.MapemFrame<?> buildSegmentFrame(int regionId, int segmentId, int revision) {
        GenericLane genericLane = buildGenericLane();
        Position3D refPoint = Position3D.builder().latitude(485000000).longitude(22000000).build();

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

    private static GenericLane buildGenericLane() {
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
