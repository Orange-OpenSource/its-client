/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.managers.SignalControllerManager;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementEvent;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.MovementState;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the signal phase and timing state of one intersection, derived from a SPATEM message.
 * <p>
 * A {@code SignalController} aggregates the {@link SignalGroup} entries for all movement phases
 * of a single intersection. Identified by the composite key
 * {@code {sourceUuid}_{regionId}_{intersectionId}}.
 * <p>
 * Signal group positions (stop-line coordinates) are resolved from the corresponding MAPEM
 * intersection via {@link #resolvePositions(RoadIntersection)}. This is called automatically
 * by the managers whenever MAPEM or SPATEM data arrives; SDK consumers read positions via
 * {@link SignalGroup#getPosition()}.
 * <p>
 * Instances are created and managed exclusively by
 * {@link SignalControllerManager}; application code should not
 * construct them directly.
 */
public class SignalController {

    /**
     * Maximum time in milliseconds since the last SPATEM update before a {@code SignalController}
     * is considered stale and eligible for removal.
     * <p>
     * In event-based publishing deployments (SPATEM sent only on phase change), the RSU may be
     * silent for a full traffic cycle without sending any message. This cap ensures a truly
     * offline RSU is eventually detected.
     */
    public static final int MAX_STALENESS_MS = 60_000;

    /** Composite key: {@code {sourceUuid}_{regionId}_{intersectionId}}. */
    private final String uuid;

    /** Region component of the globally unique intersection ID (0 when absent). */
    private final int regionId;

    /** Local intersection ID. */
    private final int intersectionId;

    /** Latest raw SPATEM frame used to populate this object. */
    private SpatemCodec.SpatemFrame<?> spatemFrame;

    /** Timestamp of the last received SPATEM update, used for expiry. */
    private long timestamp;

    /** Signal groups, keyed by signal group ID. */
    private final Map<Integer, SignalGroup> signalGroupMap = new HashMap<>();

    /** Lane-to-signal-group index populated during MAPEM resolution, keyed by lane ID. */
    private final Map<Integer, Integer> laneIdToSignalGroupId = new HashMap<>();

    /** Package-private: constructed only by {@link SignalControllerManager}. */
    public SignalController(String uuid, int regionId, int intersectionId,
                            SpatemCodec.SpatemFrame<?> spatemFrame) {
        this.uuid = uuid;
        this.regionId = regionId;
        this.intersectionId = intersectionId;
        this.spatemFrame = spatemFrame;
        this.timestamp = System.currentTimeMillis();
        populateFromFrame(spatemFrame);
    }

    // -------------------------------------------------------------------------
    // Update (called by SignalControllerManager)
    // -------------------------------------------------------------------------

    /**
     * Replace the stored frame and refresh all signal group states.
     * Always accepts updates (no revision guard — SPATEM is a real-time stream).
     *
     * @param newFrame the new SPATEM frame
     */
    public void update(SpatemCodec.SpatemFrame<?> newFrame) {
        this.spatemFrame = newFrame;
        this.timestamp = System.currentTimeMillis();
        // Do NOT clear the map — existing SignalGroup entries keep their resolved positions.
        // populateFromFrame only updates phase/timing in-place for known groups and adds new ones.
        populateFromFrame(newFrame);
        updateTimestamp();
    }

    /**
     * Like {@link #update(SpatemCodec.SpatemFrame)}, but also returns a map describing
     * exactly which <em>existing</em> signal groups changed and how.
     * <p>
     * Only signal groups that were already known before this call are included in the returned map;
     * signal groups that appear for the first time in {@code newFrame} are added to the internal
     * state but are not part of the map — the caller is responsible for detecting new groups by
     * comparing {@link #getSignalGroups()} before and after this call.
     * <p>
     * Called by {@link SignalControllerManager} to fire
     * granular signal-group-level callbacks.
     *
     * @param newFrame the new SPATEM frame
     * @return map from changed signal group to the type of change; empty when nothing changed
     */
    public Map<SignalGroup, SignalGroupUpdateType> updateWithChanges(SpatemCodec.SpatemFrame<?> newFrame) {
        // Capture previous state before applying the update
        Map<Integer, SignalPhase> previousPhases = new HashMap<>();
        Map<Integer, Integer> previousMinEndTimes = new HashMap<>();
        Map<Integer, Integer> previousMaxEndTimes = new HashMap<>();
        Set<Integer> knownGroupIds = new HashSet<>(signalGroupMap.keySet());
        for (Map.Entry<Integer, SignalGroup> entry : signalGroupMap.entrySet()) {
            previousPhases.put(entry.getKey(), entry.getValue().getPhase());
            previousMinEndTimes.put(entry.getKey(), entry.getValue().getMinEndTime());
            previousMaxEndTimes.put(entry.getKey(), entry.getValue().getMaxEndTime());
        }

        this.spatemFrame = newFrame;
        this.timestamp = System.currentTimeMillis();
        populateFromFrame(newFrame);

        // Compute deltas for previously known groups only
        Map<SignalGroup, SignalGroupUpdateType> signalGroupChanges = new LinkedHashMap<>();
        for (Map.Entry<Integer, SignalGroup> entry : signalGroupMap.entrySet()) {
            int groupId = entry.getKey();
            SignalGroup signalGroup = entry.getValue();
            if (!knownGroupIds.contains(groupId)) continue; // new groups excluded — handled by caller
            boolean phaseChanged = signalGroup.getPhase() != previousPhases.get(groupId);
            boolean timingChanged = !Objects.equals(signalGroup.getMinEndTime(), previousMinEndTimes.get(groupId))
                    || !Objects.equals(signalGroup.getMaxEndTime(), previousMaxEndTimes.get(groupId));
            if (phaseChanged || timingChanged) {
                SignalGroupUpdateType updateType;
                if (phaseChanged && timingChanged) updateType = SignalGroupUpdateType.PHASE_AND_TIMING;
                else if (phaseChanged) updateType = SignalGroupUpdateType.PHASE;
                else updateType = SignalGroupUpdateType.TIMING;
                signalGroupChanges.put(signalGroup, updateType);
            }
        }
        return signalGroupChanges;
    }

    private void populateFromFrame(SpatemCodec.SpatemFrame<?> frame) {
        if (frame.version() != SpatemVersion.V2_0_0) return;
        SpatemEnvelope200 envelope = (SpatemEnvelope200) frame.envelope();
        for (IntersectionState intersectionState : envelope.message().intersections()) {
            int rId = intersectionState.id().region() != null ? intersectionState.id().region() : 0;
            if (rId != regionId || intersectionState.id().id() != intersectionId) continue;
            for (MovementState movementState : intersectionState.states()) {
                int signalGroupId = movementState.signalGroup();
                if (movementState.stateTimeSpeed() == null || movementState.stateTimeSpeed().isEmpty()) continue;
                MovementEvent firstEvent = movementState.stateTimeSpeed().get(0);
                Integer minEnd = firstEvent.timing() != null ? firstEvent.timing().minEndTime() : null;
                Integer maxEnd = firstEvent.timing() != null ? firstEvent.timing().maxEndTime() : null;
                SignalGroup existingSignalGroup = signalGroupMap.get(signalGroupId);
                if (existingSignalGroup != null) {
                    existingSignalGroup.update(firstEvent.eventState(), minEnd, maxEnd);
                } else {
                    signalGroupMap.put(signalGroupId, new SignalGroup(signalGroupId, firstEvent.eventState(), minEnd, maxEnd));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // MAPEM position resolution
    // -------------------------------------------------------------------------

    /**
     * Resolve the stop-line position of each {@link SignalGroup} from the corresponding
     * MAPEM intersection data.
     * <p>
     * For each signal group, the first ingress lane in {@code intersection} whose
     * {@link LaneGeometry#signalGroups()} list contains the group's ID is used.
     * The position is set to the first node of that lane's {@link LaneGeometry#centerLine()}
     * (the stop-line end, since MAPEM lanes are described from stop-line outward).
     * <p>
     * Called automatically by the managers; SDK consumers do not need to call this directly.
     *
     * @param roadIntersection the MAPEM-derived intersection whose lanes carry signal-group IDs
     * @return list of signal groups whose position was resolved for the first time by this call
     */
    public List<SignalGroup> resolvePositions(RoadIntersection roadIntersection) {
        List<SignalGroup> newlyResolved = new ArrayList<>();
        List<LaneGeometry> laneGeometries = roadIntersection.getLanes();
        for (SignalGroup signalGroup : signalGroupMap.values()) {
            boolean hadPosition = signalGroup.getPosition() != null;
            Map<Integer, LatLng> laneLevelPositions = findAllStopLinePositions(laneGeometries, signalGroup.getId());
            for (Map.Entry<Integer, LatLng> laneEntry : laneLevelPositions.entrySet()) {
                signalGroup.addLanePosition(laneEntry.getKey(), laneEntry.getValue());
                laneIdToSignalGroupId.put(laneEntry.getKey(), signalGroup.getId());
            }
            if (!hadPosition && signalGroup.getPosition() != null) {
                newlyResolved.add(signalGroup);
            }
        }
        return Collections.unmodifiableList(newlyResolved);
    }

    /**
     * Collect stop-line positions (first node of each matching ingress lane) for all ingress
     * lanes that reference the given signal group ID via {@code connects_to}.
     *
     * @return map of laneId → stop-line position (insertion-ordered, in MAPEM lane order)
     */
    private static Map<Integer, LatLng> findAllStopLinePositions(List<LaneGeometry> laneGeometries,
                                                                  int signalGroupId) {
        Map<Integer, LatLng> result = new java.util.LinkedHashMap<>();
        for (LaneGeometry laneGeometry : laneGeometries) {
            if (!laneGeometry.directionalUse().contains(LaneDirection.INGRESS_PATH)) continue;
            if (!laneGeometry.signalGroups().contains(signalGroupId)) continue;
            if (laneGeometry.centerLine().isEmpty()) continue;
            result.put(laneGeometry.laneId(), laneGeometry.centerLine().get(0));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Composite key of this traffic light object. */
    public String getUuid() { return uuid; }

    public int getRegionId() { return regionId; }

    public int getIntersectionId() { return intersectionId; }

    /** Latest raw SPATEM frame. */
    public SpatemCodec.SpatemFrame<?> getSpatemFrame() { return spatemFrame; }

    /**
     * Return all {@link SignalGroup} entries for this intersection.
     *
     * @return unmodifiable list of signal groups
     */
    public List<SignalGroup> getSignalGroups() {
        return Collections.unmodifiableList(new ArrayList<>(signalGroupMap.values()));
    }

    /**
     * Return the {@link SignalGroup} for a specific signal group ID, or {@code null} if not present.
     *
     * @param signalGroupId the signal group ID (as defined in the MAP / SPATEM)
     * @return the signal group, or {@code null}
     */
    public SignalGroup getSignalGroup(int signalGroupId) {
        return signalGroupMap.get(signalGroupId);
    }

    /**
     * Return the {@link SignalGroup} controlling a specific lane, or {@code null} if the lane
     * is unknown or no MAPEM has been received yet.
     * <p>
     * The mapping is built automatically during {@link #resolvePositions(RoadIntersection)}.
     * Returns {@code null} until MAPEM data for this intersection has been processed.
     *
     * @param laneId the MAPEM lane ID the vehicle is currently on
     * @return the controlling signal group, or {@code null}
     */
    public SignalGroup getSignalGroupForLane(int laneId) {
        Integer signalGroupId = laneIdToSignalGroupId.get(laneId);
        if (signalGroupId == null) return null;
        return signalGroupMap.get(signalGroupId);
    }

    /** Timestamp (ms, wall clock) of the last received SPATEM update. */
    public long getTimestamp() {
        return timestamp;
    }

    /** Update the timestamp to now. Called by the manager on every SPATEM update. */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Subtracts {@code ageMs} milliseconds from the current timestamp.
     * <p>
     * Package-private: used exclusively in tests to simulate RSU silence without sleeping.
     *
     * @param ageMs the number of milliseconds to subtract from the timestamp
     */
    void backdateTimestampForTesting(long ageMs) {
        this.timestamp -= ageMs;
    }

    /**
     * Returns {@code true} if this signal controller should remain alive.
     * <p>
     * The controller is kept alive while at least one {@link SignalGroup} has a <em>timed</em>
     * active phase — i.e. a non-{@link SignalPhase#UNAVAILABLE} phase whose deadline is finite
     * (derived from {@code maxEndTime} or {@code minEndTime} in the SPATEM payload) and has not
     * yet passed. This supports event-based publishing where the RSU sends a SPATEM only on
     * phase change and is legitimately silent during a stable phase.
     * <p>
     * When no timed active phase is present (all groups are {@link SignalPhase#UNAVAILABLE},
     * all timed deadlines have passed, or all remaining active groups are untimed), the
     * controller falls back to a staleness cap: it expires once no SPATEM has been received
     * within {@link #MAX_STALENESS_MS} milliseconds. This detects a truly offline RSU even
     * when untimed groups would otherwise keep the controller alive indefinitely.
     * <p>
     * If no signal groups have been populated yet (immediately after construction), only the
     * staleness cap is evaluated.
     */
    public boolean stillLiving() {
        if (!signalGroupMap.isEmpty()) {
            for (SignalGroup signalGroup : signalGroupMap.values()) {
                if (signalGroup.getPhase() == SignalPhase.UNAVAILABLE) continue;
                if (signalGroup.isExpired()) continue;
                if (signalGroup.hasFiniteDeadline()) return true;
            }
        }
        return System.currentTimeMillis() - timestamp < MAX_STALENESS_MS;
    }

    // -------------------------------------------------------------------------
    // Signal group expiry (called by SignalControllerManager scheduler)
    // -------------------------------------------------------------------------

    /**
     * Checks all signal groups for deadline expiry and marks those whose
     * {@code phaseExpiryMs} has passed.
     * <p>
     * Called by {@link SignalControllerManager} on each
     * scheduler tick. For each newly expired group the phase is set to
     * {@link SignalPhase#UNAVAILABLE} before this method returns, so the returned list
     * already reflects the updated state.
     *
     * @return unmodifiable list of signal groups newly marked expired in this call;
     *         empty if none expired
     */
    public List<SignalGroup> checkAndExpireSignalGroups() {
        List<SignalGroup> newlyExpired = new ArrayList<>();
        for (SignalGroup signalGroup : signalGroupMap.values()) {
            if (signalGroup.isExpired() && signalGroup.getPhase() != SignalPhase.UNAVAILABLE) {
                signalGroup.markExpired();
                newlyExpired.add(signalGroup);
            }
        }
        return Collections.unmodifiableList(newlyExpired);
    }

    /**
     * Forces all signal groups that are not yet expired to the
     * {@link SignalPhase#UNAVAILABLE} state immediately, regardless of their deadline.
     * <p>
     * Called by {@link SignalControllerManager} when the
     * parent signal controller itself expires due to RSU silence, so that every signal group
     * receives a {@code signalGroupExpired} callback before {@code signalControllerExpired}.
     *
     * @return unmodifiable list of signal groups that were not already expired;
     *         empty if all groups had already been individually expired
     */
    public List<SignalGroup> forceExpireAllSignalGroups() {
        List<SignalGroup> forcedExpired = new ArrayList<>();
        for (SignalGroup signalGroup : signalGroupMap.values()) {
            if (signalGroup.getPhase() != SignalPhase.UNAVAILABLE) {
                signalGroup.markExpired();
                forcedExpired.add(signalGroup);
            }
        }
        return Collections.unmodifiableList(forcedExpired);
    }
}
