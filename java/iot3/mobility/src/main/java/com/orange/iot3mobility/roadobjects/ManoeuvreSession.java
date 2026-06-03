/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents an active Manoeuvre Coordination Session (MCS) observed via a received MCM
 * (Manoeuvre Coordination Message, ETSI TS 103 561).
 * <p>
 * A {@code ManoeuvreSession} is created on the first MCM received for a given session and
 * updated as subsequent MCMs arrive, following the MCS negotiation lifecycle
 * (INTENT → REQUEST → RESERVATION → EXECUTION_STATUS, or OFFER from an orchestrator).
 * The object expires either on a rolling timeout or immediately when a
 * {@link ManoeuvrePhase#causesImmediateExpiry() terminating phase} (TERMINATION or CANCELLATION)
 * is received.
 * <p>
 * <strong>Key format:</strong>
 * <ul>
 *   <li>Vehicle / VRU MCM: {@code {sourceUuid}_{stationId}_{manoeuvreId}}</li>
 *   <li>Orchestrator MCM (advised-only, no vehicle container): {@code {sourceUuid}_{stationId}_advised}</li>
 * </ul>
 * <p>
 * Instances are created and managed exclusively by
 * {@link com.orange.iot3mobility.managers.ManoeuvreSessionManager};
 * application code should not construct them directly.
 */
public class ManoeuvreSession {

    /**
     * Rolling timeout for mobile sources (VRU, vehicle).
     * Must emit at least once per 1 500 ms to keep the object alive.
     */
    public static final int MOBILE_LIFETIME_MS = 1500;

    /**
     * Rolling timeout for infrastructure sources (RSU, central station).
     */
    public static final int INFRASTRUCTURE_LIFETIME_MS = 5000;

    /** Composite key used by the manager to look up this object. */
    private final String uuid;

    /** Type of ITS-S that originated this sequence. */
    private final ManoeuvreSourceType sourceType;

    /** Current phase of the MCS negotiation. */
    private ManoeuvrePhase phase;

    /**
     * Coordination concept (agreement-seeking or prescriptive).
     * {@code null} for orchestrator-only sessions (no vehicle container).
     */
    private ManoeuvreConcept concept;

    /** Latest geographic position of the sender (decoded from ETSI units to degrees). */
    private LatLng position;

    /**
     * Decoded waypoints of the first sub-manoeuvre's reference trajectory,
     * as a list of (latitude, longitude) positions in degrees.
     * {@code null} if no trajectory is present in the MCM.
     * <p>
     * Suitable for rendering as a polyline on a map.
     */
    private List<LatLng> plannedTrajectory;

    /**
     * Station IDs of the vehicles targeted by the advice section.
     * {@code null} if the MCM carries no advice.
     */
    private List<Long> executantIds;

    /**
     * Map from executant station ID to the advised state change string
     * (e.g. {@code "stay_in_lane"}, {@code "slow_down"}).
     * {@code null} if the MCM carries no advice.
     */
    private Map<Long, String> advisedChanges;

    /** Latest raw MCM frame, available for full-detail access. */
    private McmCodec.McmFrame<?> mcmFrame;

    /** Wall-clock timestamp of the last received MCM update, used for expiry. */
    private long timestamp;

    /**
     * Package-private: constructed only by
     * {@link com.orange.iot3mobility.managers.ManoeuvreSessionManager}.
     */
    public ManoeuvreSession(String uuid,
                            ManoeuvreSourceType sourceType,
                            ManoeuvrePhase phase,
                            ManoeuvreConcept concept,
                            LatLng position,
                            List<LatLng> plannedTrajectory,
                            List<Long> executantIds,
                            Map<Long, String> advisedChanges,
                            McmCodec.McmFrame<?> mcmFrame) {
        this.uuid = uuid;
        this.sourceType = sourceType;
        this.phase = phase;
        this.concept = concept;
        this.position = position;
        this.plannedTrajectory = plannedTrajectory;
        this.executantIds = executantIds;
        this.advisedChanges = advisedChanges;
        this.mcmFrame = mcmFrame;
        this.timestamp = System.currentTimeMillis();
    }

    // -------------------------------------------------------------------------
    // Update (called by ManoeuvreSessionManager)
    // -------------------------------------------------------------------------

    /**
     * Updates the mutable fields of this sequence from a new MCM frame.
     * Called by the manager when a new MCM arrives for the same session key.
     */
    public void update(ManoeuvrePhase newPhase,
                       ManoeuvreConcept newConcept,
                       LatLng newPosition,
                       List<LatLng> newPlannedTrajectory,
                       List<Long> newExecutantIds,
                       Map<Long, String> newAdvisedChanges,
                       McmCodec.McmFrame<?> newMcmFrame) {
        this.phase = newPhase;
        this.concept = newConcept;
        this.position = newPosition;
        this.plannedTrajectory = newPlannedTrajectory;
        this.executantIds = newExecutantIds;
        this.advisedChanges = newAdvisedChanges;
        this.mcmFrame = newMcmFrame;
        this.timestamp = System.currentTimeMillis();
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this sequence is still within its rolling timeout.
     * Called by the manager's expiry scheduler.
     */
    public boolean stillLiving() {
        long lifetimeMs = sourceType.isMobile() ? MOBILE_LIFETIME_MS : INFRASTRUCTURE_LIFETIME_MS;
        return System.currentTimeMillis() - timestamp < lifetimeMs;
    }

    /**
     * Subtracts {@code ageMs} milliseconds from the stored timestamp.
     * For use in tests only — simulates silence without {@code Thread.sleep()}.
     */
    public void backdateTimestampForTesting(long ageMs) {
        this.timestamp -= ageMs;
    }

    /** Updates the timestamp to now. */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Composite manager key. */
    public String getUuid() { return uuid; }

    /** Type of ITS-S that originated this sequence. */
    public ManoeuvreSourceType getSourceType() { return sourceType; }

    /** Current phase of the MCS negotiation. */
    public ManoeuvrePhase getPhase() { return phase; }

    /**
     * Coordination concept, or {@code null} for advised-only sessions
     * that carry no vehicle container.
     */
    public ManoeuvreConcept getConcept() { return concept; }

    /** Latest geographic position of the sender in degrees. */
    public LatLng getPosition() { return position; }

    /**
     * Decoded waypoints of the planned trajectory (first sub-manoeuvre), as latitude/longitude
     * in degrees. Returns {@code null} if no trajectory is present.
     * Suitable for rendering as a polyline on a map.
     */
    public List<LatLng> getPlannedTrajectory() {
        return plannedTrajectory != null ? Collections.unmodifiableList(plannedTrajectory) : null;
    }

    /**
     * Station IDs of the vehicles targeted by the advice section,
     * or {@code null} if the MCM carries no advice.
     */
    public List<Long> getExecutantIds() {
        return executantIds != null ? Collections.unmodifiableList(executantIds) : null;
    }

    /**
     * Map from executant station ID to advised state change string,
     * or {@code null} if the MCM carries no advice.
     */
    public Map<Long, String> getAdvisedChanges() {
        return advisedChanges != null ? Collections.unmodifiableMap(advisedChanges) : null;
    }

    /**
     * The raw MCM frame for full-detail access.
     * Cast {@link McmCodec.McmFrame#envelope()} to
     * {@link com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200} after checking
     * {@link McmCodec.McmFrame#version()}.
     */
    public McmCodec.McmFrame<?> getMcmFrame() { return mcmFrame; }

    /** Wall-clock timestamp of the last received MCM update. */
    public long getTimestamp() { return timestamp; }
}



