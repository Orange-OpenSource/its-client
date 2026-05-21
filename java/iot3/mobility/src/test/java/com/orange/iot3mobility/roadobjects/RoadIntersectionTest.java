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
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadIntersectionTest {

    private static final int REGION_ID = 0;
    private static final int INTERSECTION_ID = 1001;

    // ─── accessors ─────────────────────────────────────────────────────────────

    @Test
    void accessorsReturnConstructorValues() {
        LatLng refPoint = new LatLng(48.5, 2.2);
        MapemCodec.MapemFrame<?> frame = buildMapemFrame(REGION_ID, INTERSECTION_ID, 0);
        RoadIntersection roadIntersection = new RoadIntersection(refPoint, 0, REGION_ID, INTERSECTION_ID, frame);

        assertEquals(REGION_ID, roadIntersection.getRegionId());
        assertEquals(INTERSECTION_ID, roadIntersection.getIntersectionId());
        assertEquals(refPoint, roadIntersection.getRefPoint());
        assertEquals(0, roadIntersection.getRevision());
        assertEquals(frame, roadIntersection.getMapemFrame());
    }

    // ─── revision guard ────────────────────────────────────────────────────────

    @Test
    void updateAcceptsHigherRevision() {
        LatLng initialRefPoint = new LatLng(48.5, 2.2);
        RoadIntersection roadIntersection = new RoadIntersection(
                initialRefPoint, 0, REGION_ID, INTERSECTION_ID, buildMapemFrame(REGION_ID, INTERSECTION_ID, 0));

        LatLng newRefPoint = new LatLng(48.6, 2.3);
        MapemCodec.MapemFrame<?> newFrame = buildMapemFrame(REGION_ID, INTERSECTION_ID, 1);
        boolean accepted = roadIntersection.update(newRefPoint, 1, newFrame);

        assertTrue(accepted);
        assertEquals(1, roadIntersection.getRevision());
        assertEquals(newRefPoint, roadIntersection.getRefPoint());
    }

    @Test
    void updateRejectsSameRevision() {
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, buildMapemFrame(REGION_ID, INTERSECTION_ID, 0));

        boolean accepted = roadIntersection.update(new LatLng(48.6, 2.3), 0, buildMapemFrame(REGION_ID, INTERSECTION_ID, 0));

        assertFalse(accepted);
        assertEquals(0, roadIntersection.getRevision());
    }

    @Test
    void updateRejectsLowerRevision() {
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 5, REGION_ID, INTERSECTION_ID, buildMapemFrame(REGION_ID, INTERSECTION_ID, 5));

        boolean accepted = roadIntersection.update(new LatLng(48.6, 2.3), 4, buildMapemFrame(REGION_ID, INTERSECTION_ID, 4));

        assertFalse(accepted);
        assertEquals(5, roadIntersection.getRevision());
    }

    // ─── getLanes ──────────────────────────────────────────────────────────────

    @Test
    void getLanesReturnsCenterLineForXyOffsetNodes() {
        // refPoint = 48.5° lat, 2.2° lon (ETSI ×10⁷: 485000000, 22000000)
        // Two nodes with offset (0, 0) → both collapse onto the anchor point
        MapemCodec.MapemFrame<?> frame = buildMapemFrame(REGION_ID, INTERSECTION_ID, 0);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, frame);

        List<LaneGeometry> laneGeometries = roadIntersection.getLanes();

        assertFalse(laneGeometries.isEmpty());
        LaneGeometry laneGeometry = laneGeometries.get(0);
        assertEquals(1, laneGeometry.laneId());
        assertEquals(2, laneGeometry.centerLine().size());
        // With zero XY offsets the nodes stay at the anchor
        assertEquals(48.5, laneGeometry.centerLine().get(0).getLatitude(),  1e-3);
        assertEquals(2.2,  laneGeometry.centerLine().get(0).getLongitude(), 1e-3);
    }

    @Test
    void getLanesReturnsEmptyForUnknownIntersection() {
        // Frame describes intersection 1001 but we query via a RoadIntersection for 9999
        MapemCodec.MapemFrame<?> frame = buildMapemFrame(REGION_ID, INTERSECTION_ID, 0);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, 9999, frame);

        assertTrue(roadIntersection.getLanes().isEmpty());
    }

    // ─── frame builder ─────────────────────────────────────────────────────────

    private static MapemCodec.MapemFrame<?> buildMapemFrame(int regionId, int intersectionId, int revision) {
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

        GenericLane genericLane = GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .build();

        Position3D refPoint = Position3D.builder()
                .latitude(485000000)
                .longitude(22000000)
                .build();

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
}


