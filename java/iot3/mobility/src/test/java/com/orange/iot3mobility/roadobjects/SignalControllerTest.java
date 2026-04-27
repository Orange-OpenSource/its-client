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
import com.orange.iot3mobility.messages.mapem.v200.model.lane.ConnectingLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.ConnectsTo;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneAttributes;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneType;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeDelta;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeList;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXYOffset;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignalControllerTest {

    private static final int REGION_ID = 0;
    private static final int INTERSECTION_ID = 1001;

    // ─── construction ──────────────────────────────────────────────────────────

    @Test
    void accessorsReturnConstructorValues() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        assertEquals("rsu_0_1001", signalController.getUuid());
        assertEquals(REGION_ID, signalController.getRegionId());
        assertEquals(INTERSECTION_ID, signalController.getIntersectionId());
        assertEquals(frame, signalController.getSpatemFrame());
    }

    @Test
    void constructorPopulatesSignalGroups() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrameTwoGroups(REGION_ID, INTERSECTION_ID);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        assertEquals(2, signalController.getSignalGroups().size());
    }

    @Test
    void getSignalGroupByIdReturnsCorrectPhase() {
        // eventState=6 → PROTECTED_MOVEMENT_ALLOWED (green)
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        SignalGroup signalGroup = signalController.getSignalGroup(1);
        assertNotNull(signalGroup);
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalGroup.getPhase());
        assertEquals(SignalColor.GREEN, signalGroup.getColor());
    }

    // ─── update ────────────────────────────────────────────────────────────────

    @Test
    void updateRefreshesSignalGroupPhase() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalController.getSignalGroup(1).getPhase());

        // Update to RED (eventState=3)
        SpatemCodec.SpatemFrame<?> updatedFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 3);
        signalController.update(updatedFrame);

        assertEquals(SignalPhase.STOP_AND_REMAIN, signalController.getSignalGroup(1).getPhase());
        assertEquals(SignalColor.RED, signalController.getSignalGroup(1).getColor());
    }

    @Test
    void updatePreservesSignalGroupNotInNewFrame() {
        SpatemCodec.SpatemFrame<?> initialFrame = buildSpatemFrameTwoGroups(REGION_ID, INTERSECTION_ID);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, initialFrame);
        assertEquals(2, signalController.getSignalGroups().size());

        // New frame only carries group 1
        SpatemCodec.SpatemFrame<?> partialFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 3);
        signalController.update(partialFrame);

        // Group 2 must still be present (retained from previous state)
        assertNotNull(signalController.getSignalGroup(2));
        assertEquals(2, signalController.getSignalGroups().size());
    }

    // ─── lifetime ──────────────────────────────────────────────────────────────

    @Test
    void stillLivingReturnsTrueRightAfterCreation() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        // Groups have an active phase → alive regardless of timestamp
        assertTrue(signalController.stillLiving());
    }

    @Test
    void stillLivingReturnsTrueWhenGroupHasActivePhaseAndNotStale() {
        // eventState=6 → PROTECTED_MOVEMENT_ALLOWED, no timing → phaseExpiryMs = Long.MAX_VALUE
        // isExpired() = false, so the group keeps the controller alive while timestamp is fresh
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        assertTrue(signalController.stillLiving());
    }

    @Test
    void stillLivingReturnsFalseWhenAllGroupsExpiredAndStale() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        // Force all groups to UNAVAILABLE
        signalController.forceExpireAllSignalGroups();
        // Backdate the timestamp beyond MAX_STALENESS_MS
        signalController.backdateTimestampForTesting(SignalController.MAX_STALENESS_MS + 1);

        assertFalse(signalController.stillLiving());
    }

    @Test
    void stillLivingReturnsFalseWhenUntimedGroupIsStale() {
        // Untimed group: no minEndTime/maxEndTime → phaseExpiryMs = Long.MAX_VALUE, isExpired() = false
        // With a fresh timestamp it would stay alive, but once stale it must expire
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        // Backdate beyond MAX_STALENESS_MS — group is untimed so it was never marked expired,
        // but isExpired() returns false AND staleness cap is exceeded → stillLiving() = false
        signalController.backdateTimestampForTesting(SignalController.MAX_STALENESS_MS + 1);

        assertFalse(signalController.stillLiving());
    }

    @Test
    void stillLivingReturnsTrueWhenGroupsExpiredButRecentSpatem() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        // Force all groups to UNAVAILABLE but keep timestamp fresh (just updated)
        signalController.forceExpireAllSignalGroups();
        signalController.updateTimestamp();

        // All groups expired, but SPATEM was just received — still alive
        assertTrue(signalController.stillLiving());
    }

    // ─── MAPEM position resolution ─────────────────────────────────────────────

    @Test
    void resolvePositionsSetsStopLineOnMatchingIngressLane() {
        // Build a SPATEM with signal group 1
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        // Signal group position is null before MAPEM resolution
        assertNull(signalController.getSignalGroup(1).getPosition());

        // Build a MAPEM with an ingress lane connected to signal group 1
        // refPoint = 48.5° lat, 2.2° lon in ETSI ×10⁷
        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID,
                485000000, 22000000, // refPoint ETSI ×10⁷
                1,                   // laneId
                1                    // signalGroupId
        );
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);

        signalController.resolvePositions(roadIntersection);

        LatLng stopLine = signalController.getSignalGroup(1).getPosition();
        assertNotNull(stopLine);
        // The lane uses XY-offset nodes (0,0) from the refPoint, so the first centerLine point
        // should be at the anchor coordinates (48.5, 2.2) within floating-point tolerance
        assertEquals(48.5, stopLine.getLatitude(),  1e-3);
        assertEquals(2.2,  stopLine.getLongitude(), 1e-3);
    }

    @Test
    void resolvePositionsLeavesPositionNullWhenNoMatchingLane() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        // MAPEM lane is connected to signal group 99 — no match for group 1
        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 99);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);

        signalController.resolvePositions(roadIntersection);

        assertNull(signalController.getSignalGroup(1).getPosition());
    }

    @Test
    void resolvePositionsReturnsNewlyResolvedGroups() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);

        List<SignalGroup> resolved = signalController.resolvePositions(roadIntersection);

        assertEquals(1, resolved.size());
        assertEquals(1, resolved.get(0).getId());
    }

    @Test
    void resolvePositionsDoesNotReturnAlreadyResolvedGroups() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);

        // First call resolves position
        signalController.resolvePositions(roadIntersection);
        // Second call — position already set, should return empty
        List<SignalGroup> resolvedAgain = signalController.resolvePositions(roadIntersection);

        assertTrue(resolvedAgain.isEmpty());
    }

    // ─── getLaneLevelPositions ─────────────────────────────────────────────────

    @Test
    void resolvePositionsPopulatesLaneLevelPositionsForAllMatchingLanes() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        // Two ingress lanes (laneId 1 and 2) both connected to signal group 1
        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithTwoLanesSameGroup(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);

        signalController.resolvePositions(roadIntersection);

        java.util.Map<Integer, LatLng> laneLevelPositions =
                signalController.getSignalGroup(1).getLaneLevelPositions();
        assertEquals(2, laneLevelPositions.size());
        assertTrue(laneLevelPositions.containsKey(1));
        assertTrue(laneLevelPositions.containsKey(2));
    }

    @Test
    void getLaneLevelPositionsIsUnmodifiable() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);
        signalController.resolvePositions(roadIntersection);

        assertThrows(UnsupportedOperationException.class,
                () -> signalController.getSignalGroup(1).getLaneLevelPositions().put(99, new LatLng(0, 0)));
    }

    // ─── getSignalGroupForLane ─────────────────────────────────────────────────

    @Test
    void getSignalGroupForLaneReturnsCorrectGroupAfterMapemResolution() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);
        signalController.resolvePositions(roadIntersection);

        SignalGroup signalGroup = signalController.getSignalGroupForLane(1);
        assertNotNull(signalGroup);
        assertEquals(1, signalGroup.getId());
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalGroup.getPhase());
    }

    @Test
    void getSignalGroupForLaneReturnsNullForUnknownLane() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithSignaledIngressLane(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);
        signalController.resolvePositions(roadIntersection);

        assertNull(signalController.getSignalGroupForLane(99));
    }

    @Test
    void getSignalGroupForLaneReturnsNullBeforeMapemResolution() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        // No resolvePositions() call — laneIdToSignalGroupId is empty
        assertNull(signalController.getSignalGroupForLane(1));
    }

    @Test
    void getSignalGroupForLaneBothLanesResolveToSameGroup() {
        SpatemCodec.SpatemFrame<?> spatemFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, spatemFrame);

        MapemCodec.MapemFrame<?> mapemFrame = buildMapemFrameWithTwoLanesSameGroup(
                REGION_ID, INTERSECTION_ID, 485000000, 22000000, 1);
        RoadIntersection roadIntersection = new RoadIntersection(
                new LatLng(48.5, 2.2), 0, REGION_ID, INTERSECTION_ID, mapemFrame);
        signalController.resolvePositions(roadIntersection);

        SignalGroup fromLane1 = signalController.getSignalGroupForLane(1);
        SignalGroup fromLane2 = signalController.getSignalGroupForLane(2);
        assertNotNull(fromLane1);
        assertNotNull(fromLane2);
        assertEquals(fromLane1.getId(), fromLane2.getId());
    }

    // ─── updateWithChanges ─────────────────────────────────────────────────────

    @Test
    void updateWithChangesDetectsPhaseChange() {
        SpatemCodec.SpatemFrame<?> initialFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, initialFrame);

        SpatemCodec.SpatemFrame<?> updatedFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 3);
        Map<SignalGroup, SignalGroupUpdateType> signalGroupChanges = signalController.updateWithChanges(updatedFrame);

        assertEquals(1, signalGroupChanges.size());
        Map.Entry<SignalGroup, SignalGroupUpdateType> change = signalGroupChanges.entrySet().iterator().next();
        assertEquals(1, change.getKey().getId());
        assertEquals(SignalGroupUpdateType.PHASE, change.getValue());
    }

    @Test
    void updateWithChangesDoesNotIncludeNewSignalGroupsInMap() {
        SpatemCodec.SpatemFrame<?> initialFrame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, initialFrame);

        // Frame now carries group 2 as well — it must NOT appear in the returned changes map
        SpatemCodec.SpatemFrame<?> updatedFrame = buildSpatemFrameTwoGroups(REGION_ID, INTERSECTION_ID);
        Map<SignalGroup, SignalGroupUpdateType> signalGroupChanges = signalController.updateWithChanges(updatedFrame);

        assertFalse(signalGroupChanges.keySet().stream().anyMatch(signalGroup -> signalGroup.getId() == 2));
        // But the new group must now be present in the traffic light's signal groups
        assertEquals(2, signalController.getSignalGroups().size());
        assertNotNull(signalController.getSignalGroup(2));
    }

    @Test
    void updateWithChangesReturnsEmptyWhenNothingChanged() {
        SpatemCodec.SpatemFrame<?> frame = buildSpatemFrame(REGION_ID, INTERSECTION_ID, 1, 6);
        SignalController signalController = new SignalController("rsu_0_1001", REGION_ID, INTERSECTION_ID, frame);

        // Same frame again — no changes
        Map<SignalGroup, SignalGroupUpdateType> signalGroupChanges = signalController.updateWithChanges(frame);

        assertTrue(signalGroupChanges.isEmpty());
    }

    // ─── frame builders ────────────────────────────────────────────────────────

    private static SpatemCodec.SpatemFrame<?> buildSpatemFrame(int regionId, int intersectionId,
                                                                int signalGroupId, int eventState) {
        MovementEvent movementEvent = MovementEvent.builder().eventState(eventState).build();
        MovementState movementState = MovementState.builder()
                .signalGroup(signalGroupId)
                .stateTimeSpeed(List.of(movementEvent))
                .build();

        com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId refId =
                new com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId(
                        regionId == 0 ? null : regionId, intersectionId);

        IntersectionState intersectionState = IntersectionState.builder()
                .id(refId)
                .revision(0)
                .status()
                .states(List.of(movementState))
                .build();

        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(List.of(intersectionState))
                .build();

        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(System.currentTimeMillis())
                .message(message)
                .build();

        return new SpatemCodec.SpatemFrame<>(SpatemVersion.V2_0_0, envelope);
    }

    private static SpatemCodec.SpatemFrame<?> buildSpatemFrameTwoGroups(int regionId, int intersectionId) {
        MovementEvent event1 = MovementEvent.builder().eventState(6).build();
        MovementEvent event2 = MovementEvent.builder().eventState(3).build();

        MovementState state1 = MovementState.builder()
                .signalGroup(1).stateTimeSpeed(List.of(event1)).build();
        MovementState state2 = MovementState.builder()
                .signalGroup(2).stateTimeSpeed(List.of(event2)).build();

        com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId refId =
                new com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionReferenceId(
                        regionId == 0 ? null : regionId, intersectionId);

        IntersectionState intersectionState = IntersectionState.builder()
                .id(refId)
                .revision(0)
                .status()
                .states(List.of(state1, state2))
                .build();

        SpatemMessage200 message = SpatemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .intersections(List.of(intersectionState))
                .build();

        SpatemEnvelope200 envelope = SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_42")
                .timestamp(System.currentTimeMillis())
                .message(message)
                .build();

        return new SpatemCodec.SpatemFrame<>(SpatemVersion.V2_0_0, envelope);
    }

    private static MapemCodec.MapemFrame<?> buildMapemFrameWithSignaledIngressLane(
            int regionId, int intersectionId,
            int refLatEtsi, int refLonEtsi,
            int laneId, int signalGroupId) {

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

        ConnectingLane connectingLane = new ConnectingLane(2, null);
        ConnectsTo connectsTo = ConnectsTo.builder()
                .connectingLane(connectingLane)
                .signalGroup(signalGroupId)
                .build();

        GenericLane genericLane = GenericLane.builder()
                .laneId(laneId)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .connectsTo(List.of(connectsTo))
                .build();

        Position3D refPoint = Position3D.builder()
                .latitude(refLatEtsi)
                .longitude(refLonEtsi)
                .build();

        IntersectionReferenceId intersectionReferenceId =
                new IntersectionReferenceId(regionId == 0 ? null : regionId, intersectionId);
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
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

    /**
     * Builds a MAPEM frame with two ingress lanes (laneId 1 and 2) both connected to the same
     * signal group ID. Used to test {@code getLaneLevelPositions()} and {@code getSignalGroupForLane()}.
     */
    private static MapemCodec.MapemFrame<?> buildMapemFrameWithTwoLanesSameGroup(
            int regionId, int intersectionId,
            int refLatEtsi, int refLonEtsi,
            int signalGroupId) {

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

        ConnectsTo connectsTo = ConnectsTo.builder()
                .connectingLane(new ConnectingLane(99, null))
                .signalGroup(signalGroupId)
                .build();

        GenericLane lane1 = GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .connectsTo(List.of(connectsTo))
                .build();
        GenericLane lane2 = GenericLane.builder()
                .laneId(2)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .connectsTo(List.of(connectsTo))
                .build();

        Position3D refPoint = Position3D.builder()
                .latitude(refLatEtsi)
                .longitude(refLonEtsi)
                .build();

        IntersectionReferenceId intersectionReferenceId =
                new IntersectionReferenceId(regionId == 0 ? null : regionId, intersectionId);
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(refPoint)
                .laneSet(List.of(lane1, lane2))
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

