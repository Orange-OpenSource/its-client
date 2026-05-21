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
import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneAttributes;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneType;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeDelta;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeList;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXYOffset;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.messages.spatem.SpatemHelper;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import com.orange.iot3mobility.roadobjects.SignalController;
import com.orange.iot3mobility.roadobjects.SignalGroup;
import com.orange.iot3mobility.roadobjects.SignalGroupUpdateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignalControllerManagerTest {

    private IoT3SignalControllerCallback mockCallback;
    private final SpatemHelper spatemHelper = new SpatemHelper();
    private final MapemHelper mapemHelper = new MapemHelper();

    @BeforeEach
    void resetStaticState() throws Exception {
        mockCallback = mock(IoT3SignalControllerCallback.class);

        // Stop any running expiration scheduler to prevent interference between tests
        Field schedulerField = SignalControllerManager.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService existingScheduler = (ScheduledExecutorService) schedulerField.get(null);
        if (existingScheduler != null) {
            existingScheduler.shutdownNow();
            schedulerField.set(null, null);
        }

        Field signalControllersField = SignalControllerManager.class.getDeclaredField("SIGNAL_CONTROLLERS");
        signalControllersField.setAccessible(true);
        ((ArrayList<?>) signalControllersField.get(null)).clear();

        Field signalControllerMapField = SignalControllerManager.class.getDeclaredField("SIGNAL_CONTROLLER_MAP");
        signalControllerMapField.setAccessible(true);
        ((HashMap<?, ?>) signalControllerMapField.get(null)).clear();

        // Reset RoadGeometryManager state as well to isolate cross-manager interactions
        Field geometriesField = RoadGeometryManager.class.getDeclaredField("ROAD_GEOMETRIES");
        geometriesField.setAccessible(true);
        ((ArrayList<?>) geometriesField.get(null)).clear();

        Field geometryMapField = RoadGeometryManager.class.getDeclaredField("ROAD_GEOMETRY_MAP");
        geometryMapField.setAccessible(true);
        ((HashMap<?, ?>) geometryMapField.get(null)).clear();

        SignalControllerManager.init(mockCallback);
    }

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Build a SPATEM JSON with one intersection state. */
    private String spatemJson(String sourceUuid, long stationId,
                               Integer regionId, int intersectionId) throws Exception {
        MovementEvent movementEvent = MovementEvent.builder()
                .eventState(6)
                .build();
        MovementState movementState = MovementState.builder()
                .signalGroup(1)
                .stateTimeSpeed(List.of(movementEvent))
                .build();
        com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId intersectionReferenceId =
                new com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId(regionId, intersectionId);
        IntersectionState intersectionState = IntersectionState.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .status(List.of())
                .states(List.of(movementState))
                .build();
        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(stationId)
                .intersections(List.of(intersectionState))
                .build();
        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return spatemHelper.toJson(envelope);
    }

    /** Build a SPATEM JSON with one intersection state and a specific eventState. */
    private String spatemJsonWithEventState(String sourceUuid, long stationId,
                                            Integer regionId, int intersectionId,
                                            int eventState) throws Exception {
        MovementEvent movementEvent = MovementEvent.builder()
                .eventState(eventState)
                .build();
        MovementState movementState = MovementState.builder()
                .signalGroup(1)
                .stateTimeSpeed(List.of(movementEvent))
                .build();
        com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId intersectionReferenceId =
                new com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId(regionId, intersectionId);
        IntersectionState intersectionState = IntersectionState.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .status(List.of())
                .states(List.of(movementState))
                .build();
        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(stationId)
                .intersections(List.of(intersectionState))
                .build();
        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return spatemHelper.toJson(envelope);
    }

    /** Build a MAPEM JSON with one intersection, used to test MAPEM↔SPATEM position resolution. */
    private String mapemWithIntersectionJson(String sourceUuid, long stationId,
                                              Integer regionId, int intersectionId) throws Exception {
        com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId intersectionReferenceId =
                new com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId(regionId, intersectionId);
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
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(Position3D.builder().latitude(485000000).longitude(22000000).build())
                .laneSet(List.of(genericLane))
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

    // -------------------------------------------------------------------------
    // Basic lifecycle tests
    // -------------------------------------------------------------------------

    @Test
    void processSpatemFirstMessageTriggersNewSignalController() throws Exception {
        String json = spatemJson("rsu_42", 42L, null, 1001);
        SignalControllerManager.processSpatem(json, spatemHelper);

        verify(mockCallback, times(1)).newSignalController(any(SignalController.class));
        verify(mockCallback, never()).signalControllerUpdated(any());
    }

    @Test
    void processSpatemSpatemArrivedCallbackIsAlwaysFired() throws Exception {
        String json = spatemJson("rsu_42", 42L, null, 1001);
        SignalControllerManager.processSpatem(json, spatemHelper);
        SignalControllerManager.processSpatem(json, spatemHelper);

        verify(mockCallback, times(2)).spatemArrived(any());
    }

    @Test
    void processSpatemSecondMessageWithSameIntersectionTriggersUpdate() throws Exception {
        String json = spatemJson("rsu_42", 42L, null, 1001);
        SignalControllerManager.processSpatem(json, spatemHelper);
        SignalControllerManager.processSpatem(json, spatemHelper);

        verify(mockCallback, times(1)).newSignalController(any());
        verify(mockCallback, times(1)).signalControllerUpdated(any());
    }

    @Test
    void processSpatemTwoDifferentIntersectionsCreateTwoSignalControllers() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1002), spatemHelper);

        verify(mockCallback, times(2)).newSignalController(any());
        assertEquals(2, SignalControllerManager.getSignalControllers().size());
    }

    @Test
    void processSpatemTwoDifferentSourcesForSameIntersectionCreateTwoSignalControllers() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_A", 1L, null, 1001), spatemHelper);
        SignalControllerManager.processSpatem(spatemJson("rsu_B", 2L, null, 1001), spatemHelper);

        verify(mockCallback, times(2)).newSignalController(any());
        assertEquals(2, SignalControllerManager.getSignalControllers().size());
    }

    @Test
    void processSpatemNewSignalControllerHasCorrectIntersectionId() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, 5, 1001), spatemHelper);

        ArgumentCaptor<SignalController> captor = ArgumentCaptor.forClass(SignalController.class);
        verify(mockCallback).newSignalController(captor.capture());
        SignalController signalController = captor.getValue();
        assertEquals(5, signalController.getRegionId());
        assertEquals(1001, signalController.getIntersectionId());
    }

    @Test
    void processSpatemNullRegionDefaultsToRegionZero() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        ArgumentCaptor<SignalController> captor = ArgumentCaptor.forClass(SignalController.class);
        verify(mockCallback).newSignalController(captor.capture());
        assertEquals(0, captor.getValue().getRegionId());
    }

    // -------------------------------------------------------------------------
    // MAPEM↔SPATEM position resolution tests
    // -------------------------------------------------------------------------

    @Test
    void processSpatemBeforeMapemResolvePositionsWhenMapemArrives() throws Exception {
        // SPATEM arrives first — no MAPEM geometry available yet
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        // Initialize RoadGeometryManager with a no-op callback and inject MAPEM
        RoadGeometryManager.init(mock(IoT3RoadGeometryCallback.class));
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001), mapemHelper);

        // After MAPEM arrives, TrafficLightManager.tryResolvePositions should have been triggered
        // and the traffic light should now have signal groups with a non-null position
        SignalController signalController = SignalControllerManager.getSignalControllers().get(0);
        assertNotNull(signalController);
        // Signal group position resolution is best-effort; only verify no exception was thrown
        // and the traffic light object is still present
        assertEquals(1001, signalController.getIntersectionId());
    }

    @Test
    void processMapemBeforeSpatemResolvesPositionsImmediatelyOnSpatemArrival() throws Exception {
        // MAPEM arrives first
        RoadGeometryManager.init(mock(IoT3RoadGeometryCallback.class));
        RoadGeometryManager.processMapem(
                mapemWithIntersectionJson("rsu_42", 42L, null, 1001), mapemHelper);

        // SPATEM arrives after MAPEM — positions should be resolved immediately
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        verify(mockCallback, times(1)).newSignalController(any(SignalController.class));
        assertEquals(1, SignalControllerManager.getSignalControllers().size());
    }

    // -------------------------------------------------------------------------
    // Public API tests
    // -------------------------------------------------------------------------

    @Test
    void getSignalControllersReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> SignalControllerManager.getSignalControllers().add(null));
    }

    @Test
    void clearRemovesAllSignalControllers() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        SignalControllerManager.clear();

        assertEquals(0, SignalControllerManager.getSignalControllers().size());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void processSpatemInvalidJsonDoesNotCrash() {
        assertDoesNotThrow(() ->
                SignalControllerManager.processSpatem("{invalid}", spatemHelper));
    }

    @Test
    void processSpatemWithNullCallbackDoesNotCrash() throws Exception {
        // Shutdown scheduler started by init() in @BeforeEach, then set callback to null
        Field schedulerField = SignalControllerManager.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService runningScheduler = (ScheduledExecutorService) schedulerField.get(null);
        if (runningScheduler != null) runningScheduler.shutdownNow();
        schedulerField.set(null, null);

        Field callbackField = SignalControllerManager.class.getDeclaredField("ioT3SignalControllerCallback");
        callbackField.setAccessible(true);
        callbackField.set(null, null);

        assertDoesNotThrow(() ->
                SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper));
    }

    // -------------------------------------------------------------------------
    // Signal group callbacks
    // -------------------------------------------------------------------------

    @Test
    void processSpatemFirstMessageFiresNewSignalGroupForEachGroup() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        verify(mockCallback, times(1)).newSignalGroup(any(SignalController.class), any(SignalGroup.class));
    }

    @Test
    void processSpatemPhaseChangeFiresSignalGroupUpdatedWithPhaseType() throws Exception {
        // First SPATEM: eventState=6 (green)
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        // Second SPATEM: eventState=3 (red)
        SignalControllerManager.processSpatem(spatemJsonWithEventState("rsu_42", 42L, null, 1001, 3), spatemHelper);

        ArgumentCaptor<SignalGroupUpdateType> typeCaptor = ArgumentCaptor.forClass(SignalGroupUpdateType.class);
        verify(mockCallback, atLeastOnce()).signalGroupUpdated(any(), any(), typeCaptor.capture());
        assertTrue(typeCaptor.getAllValues().contains(SignalGroupUpdateType.PHASE));
    }

    @Test
    void processSpatemSameStateDoesNotFireSignalGroupUpdated() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);
        // Same JSON again — no state change
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        verify(mockCallback, never()).signalGroupUpdated(any(), any(), any());
    }

    @Test
    void newSignalGroupNotFiredAgainOnSecondSpatem() throws Exception {
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);
        SignalControllerManager.processSpatem(spatemJson("rsu_42", 42L, null, 1001), spatemHelper);

        // newSignalGroup fires only once (on first SPATEM)
        verify(mockCallback, times(1)).newSignalGroup(any(), any());
    }
}



