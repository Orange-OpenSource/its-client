/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.mapem.MapemHelper;
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
import com.orange.iot3mobility.roadobjects.RoadGeometry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoadGeometryManagerTest {

    private IoT3RoadGeometryCallback mockCallback;
    private final MapemHelper mapemHelper = new MapemHelper();

    @BeforeEach
    void resetStaticState() throws Exception {
        mockCallback = mock(IoT3RoadGeometryCallback.class);

        Field geometriesField = RoadGeometryManager.class.getDeclaredField("ROAD_GEOMETRIES");
        geometriesField.setAccessible(true);
        ((ArrayList<?>) geometriesField.get(null)).clear();

        Field mapField = RoadGeometryManager.class.getDeclaredField("ROAD_GEOMETRY_MAP");
        mapField.setAccessible(true);
        ((HashMap<?, ?>) mapField.get(null)).clear();

        // Also reset TrafficLightManager static state to avoid cross-test interference
        Field signalControllersField = SignalControllerManager.class.getDeclaredField("SIGNAL_CONTROLLERS");
        signalControllersField.setAccessible(true);
        ((ArrayList<?>) signalControllersField.get(null)).clear();

        Field signalControllerMapField = SignalControllerManager.class.getDeclaredField("SIGNAL_CONTROLLER_MAP");
        signalControllerMapField.setAccessible(true);
        ((HashMap<?, ?>) signalControllerMapField.get(null)).clear();

        RoadGeometryManager.init(mockCallback);
    }

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Build a MAPEM JSON with one intersection. */
    private String mapemWithIntersectionJson(String sourceUuid, long stationId,
                                              Integer regionId, int intersectionId,
                                              int revision) throws Exception {
        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(regionId, intersectionId);
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(revision)
                .refPoint(validRefPoint())
                .laneSet(List.of(validGenericLane()))
                .build();
        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(stationId)
                .msgIssueRevision(0)
                .intersections(List.of(intersectionGeometry))
                .build();
        MapemEnvelope200 envelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return mapemHelper.toJson(envelope);
    }

    /** Build a MAPEM JSON with one road segment. */
    private String mapemWithRoadSegmentJson(String sourceUuid, long stationId,
                                             Integer regionId, int segmentId,
                                             int revision) throws Exception {
        RoadSegmentReferenceId roadSegmentReferenceId = new RoadSegmentReferenceId(regionId, segmentId);
        RoadSegmentData roadSegmentData = RoadSegmentData.builder()
                .id(roadSegmentReferenceId)
                .revision(revision)
                .refPoint(validRefPoint())
                .roadLaneSet(List.of(validGenericLane()))
                .build();
        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(stationId)
                .msgIssueRevision(0)
                .roadSegments(List.of(roadSegmentData))
                .build();
        MapemEnvelope200 envelope = MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return mapemHelper.toJson(envelope);
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

    // -------------------------------------------------------------------------
    // Intersection tests
    // -------------------------------------------------------------------------

    @Test
    void processMapemFirstMessageTriggersNewRoadGeometry() throws Exception {
        String json = mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0);
        RoadGeometryManager.processMapem(json, mapemHelper);

        verify(mockCallback, times(1)).newRoadGeometry(any(RoadGeometry.class));
        verify(mockCallback, never()).roadGeometryUpdated(any());
    }

    @Test
    void processMapemMapemArrivedCallbackIsAlwaysFired() throws Exception {
        String json = mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0);
        RoadGeometryManager.processMapem(json, mapemHelper);
        RoadGeometryManager.processMapem(json, mapemHelper);

        verify(mockCallback, times(2)).mapemArrived(any());
    }

    @Test
    void processMapemSameStationSameRevisionDoesNotTriggerUpdate() throws Exception {
        String json = mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0);
        RoadGeometryManager.processMapem(json, mapemHelper);
        RoadGeometryManager.processMapem(json, mapemHelper);

        verify(mockCallback, times(1)).newRoadGeometry(any());
        verify(mockCallback, never()).roadGeometryUpdated(any());
    }

    @Test
    void processMapemHigherRevisionTriggersRoadGeometryUpdated() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0), mapemHelper);
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 1), mapemHelper);

        verify(mockCallback, times(1)).newRoadGeometry(any());
        verify(mockCallback, times(1)).roadGeometryUpdated(any());
    }

    @Test
    void processMapemTwoDifferentStationsCreateTwoRoadGeometries() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_A", 1L, null, 1001, 0), mapemHelper);
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_B", 2L, null, 2001, 0), mapemHelper);

        verify(mockCallback, times(2)).newRoadGeometry(any());
        assertEquals(2, RoadGeometryManager.getRoadGeometries().size());
    }

    @Test
    void processMapemSameUuidDifferentStationIdCreatesTwoRoadGeometries() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 1L, null, 1001, 0), mapemHelper);
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 2L, null, 1002, 0), mapemHelper);

        verify(mockCallback, times(2)).newRoadGeometry(any());
        assertEquals(2, RoadGeometryManager.getRoadGeometries().size());
    }

    @Test
    void processMapemNewRoadGeometryHasCorrectRefPointPosition() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0), mapemHelper);

        ArgumentCaptor<RoadGeometry> captor = ArgumentCaptor.forClass(RoadGeometry.class);
        verify(mockCallback).newRoadGeometry(captor.capture());
        RoadGeometry roadGeometry = captor.getValue();
        assertFalse(roadGeometry.getIntersections().isEmpty());
        assertEquals(48.5, roadGeometry.getIntersections().get(0).getRefPoint().getLatitude(), 1e-2);
        assertEquals(2.2, roadGeometry.getIntersections().get(0).getRefPoint().getLongitude(), 1e-2);
    }

    @Test
    void processMapemIntersectionWithExplicitRegionIdIsStored() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, 5, 1001, 0), mapemHelper);

        verify(mockCallback, times(1)).newRoadGeometry(any(RoadGeometry.class));
        assertEquals(1, RoadGeometryManager.getRoadIntersections().size());
        assertEquals(5, RoadGeometryManager.getRoadIntersections().get(0).getRegionId());
    }

    // -------------------------------------------------------------------------
    // Road segment tests
    // -------------------------------------------------------------------------

    @Test
    void processMapemWithRoadSegmentTriggersNewRoadGeometry() throws Exception {
        String json = mapemWithRoadSegmentJson("rsu_seg_1", 10L, null, 2001, 0);
        RoadGeometryManager.processMapem(json, mapemHelper);

        verify(mockCallback, times(1)).newRoadGeometry(any(RoadGeometry.class));
        assertEquals(1, RoadGeometryManager.getRoadSegments().size());
    }

    @Test
    void processMapemSegmentHigherRevisionTriggersUpdate() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithRoadSegmentJson("rsu_seg_1", 10L, null, 2001, 0), mapemHelper);
        RoadGeometryManager.processMapem(
                mapemWithRoadSegmentJson("rsu_seg_1", 10L, null, 2001, 1), mapemHelper);

        verify(mockCallback, times(1)).roadGeometryUpdated(any());
    }

    // -------------------------------------------------------------------------
    // Public API tests
    // -------------------------------------------------------------------------

    @Test
    void getRoadGeometriesReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadGeometryManager.getRoadGeometries().add(null));
    }

    @Test
    void getRoadIntersectionsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadGeometryManager.getRoadIntersections().add(null));
    }

    @Test
    void getRoadSegmentsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadGeometryManager.getRoadSegments().add(null));
    }

    @Test
    void clearRemovesAllRoadGeometries() throws Exception {
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0), mapemHelper);

        RoadGeometryManager.clear();

        assertEquals(0, RoadGeometryManager.getRoadGeometries().size());
        assertEquals(0, RoadGeometryManager.getRoadIntersections().size());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void processMapemInvalidJsonDoesNotCrash() {
        assertDoesNotThrow(() ->
                RoadGeometryManager.processMapem("{invalid}", mapemHelper));
    }

    @Test
    void processMapemWithNullCallbackDoesNotCrash() throws Exception {
        RoadGeometryManager.init(null);
        assertDoesNotThrow(() ->
                RoadGeometryManager.processMapem(
                        mapemWithIntersectionJson("rsu_42", 42L, null, 1001, 0), mapemHelper));
    }
}


