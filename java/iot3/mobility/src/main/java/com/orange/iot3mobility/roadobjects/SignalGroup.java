/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the current signal state for a single signal group within a {@link SignalController}.
 * <p>
 * Instances are created and owned by {@link SignalController}; application code should not
 * construct them directly.
 * <p>
 * Use {@link #getPhase()} for full ETSI phase semantics, or {@link #getColor()} for a
 * quick red / yellow / green indication.
 * <p>
 * {@link #getPosition()} returns a single representative WGS-84 stop-line position (first
 * ingress lane) for simple use cases such as rendering a colored dot on a map.
 * {@link #getLaneLevelPositions()} returns all stop-line positions keyed by lane ID, for
 * applications that need per-lane display or lane-level signal lookup.
 * Both are resolved from the corresponding MAPEM intersection data and are {@code null} /
 * empty until that MAPEM has been received.
 */
public class SignalGroup {

    /**
     * Grace period added to {@code minEndTime} when {@code maxEndTime} is absent, in milliseconds.
     * Used as the fallback phase expiry deadline.
     */
    public static final long SIGNAL_GROUP_EXPIRY_GRACE_MS = 5_000L;

    /** Signal group ID (matches {@code signal_group} in SPATEM and {@code connects_to} in MAPEM). */
    private final int id;

    /** Current signal phase. */
    private SignalPhase phase;

    /**
     * Optional: earliest time the phase could change, in tenths of a second in the current/next
     * hour (0–36001; 36001 = unknown).
     */
    private Integer minEndTime;

    /**
     * Optional: latest time the phase could change (same unit as {@code minEndTime}).
     */
    private Integer maxEndTime;

    /**
     * Stop-line positions of all ingress lanes associated with this signal group, keyed by
     * lane ID (insertion-ordered, lowest lane ID first).
     * Resolved from the corresponding MAPEM. Empty until MAPEM data is available.
     */
    private final Map<Integer, LatLng> laneLevelPositions = new LinkedHashMap<>();

    /**
     * Wall-clock Unix ms at which this signal group's phase is expected to have changed.
     * Derived from {@code maxEndTime} when available, otherwise from
     * {@code minEndTime + SIGNAL_GROUP_EXPIRY_GRACE_MS}.
     * {@link Long#MAX_VALUE} when no timing information is present.
     */
    private long phaseExpiryMs;

    /** {@code true} once {@link #markExpired()} has been called by the manager. */
    private boolean expired;

    /** Package-private: constructed only by {@link SignalController}. */
    SignalGroup(int id, int eventState, Integer minEndTime, Integer maxEndTime) {
        this.id = id;
        this.phase = SignalPhase.fromEtsiValue(eventState);
        this.minEndTime = minEndTime;
        this.maxEndTime = maxEndTime;
        this.expired = false;
        this.phaseExpiryMs = computePhaseExpiryMs(minEndTime, maxEndTime);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Signal group ID, as defined in SPATEM ({@code signal_group}) and referenced in
     * MAPEM ({@code connects_to[].signal_group}).
     */
    public int getId() { return id; }

    /**
     * Full ETSI phase state for this signal group.
     *
     * @return the {@link SignalPhase} corresponding to the received {@code event_state}
     */
    public SignalPhase getPhase() { return phase; }

    /**
     * Simplified traffic-light color derived from the current phase.
     * Convenience shortcut for {@code getPhase().color()}.
     *
     * @return {@link SignalColor#GREEN}, {@link SignalColor#YELLOW}, {@link SignalColor#RED},
     *         or {@link SignalColor#UNKNOWN}
     */
    public SignalColor getColor() { return phase.color(); }

    /**
     * Whether the current phase is a blinking/flashing state.
     * Convenience shortcut for {@code getPhase().isBlinking()}.
     * <p>
     * {@code true} for flashing red ({@link SignalPhase#STOP_THEN_PROCEED}) and
     * flashing yellow ({@link SignalPhase#CAUTION_CONFLICTING_TRAFFIC}).
     */
    public boolean isBlinking() { return phase.isBlinking(); }

    /**
     * Raw ETSI {@code event_state} integer value [0..9].
     * Equivalent to {@code getPhase().etsiValue()}.
     */
    public int getEventState() { return phase.etsiValue(); }

    public Integer getMinEndTime() { return minEndTime; }

    public Integer getMaxEndTime() { return maxEndTime; }

    /**
     * WGS-84 stop-line position of the first resolved ingress lane for this signal group.
     * <p>
     * Convenience accessor for simple use cases (e.g. rendering a single colored dot on a map).
     * Equivalent to the value of the lowest lane-ID entry in {@link #getLaneLevelPositions()}.
     * <p>
     * Returns {@code null} if the MAPEM for this intersection has not yet been received.
     */
    public LatLng getPosition() {
        if (laneLevelPositions.isEmpty()) return null;
        return laneLevelPositions.values().iterator().next();
    }

    /**
     * WGS-84 stop-line positions of <em>all</em> ingress lanes connected to this signal group,
     * keyed by MAPEM lane ID.
     * <p>
     * Use this when per-lane rendering or per-lane signal lookup is required. The map is
     * insertion-ordered (lanes are added in the order they appear in the MAPEM message).
     * <p>
     * Returns an empty map if the MAPEM for this intersection has not yet been received.
     *
     * @return unmodifiable map of laneId → stop-line position
     */
    public Map<Integer, LatLng> getLaneLevelPositions() {
        return Collections.unmodifiableMap(laneLevelPositions);
    }

    // -------------------------------------------------------------------------
    // Expiry (package-private — managed exclusively by SignalController)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the phase deadline has passed or {@link #markExpired()} was called.
     * <p>
     * Package-private: evaluated exclusively by
     * {@link SignalController#checkAndExpireSignalGroups()} and
     * {@link SignalController#forceExpireAllSignalGroups()}.
     */
    boolean isExpired() {
        return expired || System.currentTimeMillis() >= phaseExpiryMs;
    }

    /**
     * Returns {@code true} if this signal group has a concrete phase deadline derived from
     * {@code maxEndTime} or {@code minEndTime} in the SPATEM payload.
     * <p>
     * Returns {@code false} when neither timing field was present ({@code phaseExpiryMs}
     * is {@link Long#MAX_VALUE}).
     */
    boolean hasFiniteDeadline() {
        return phaseExpiryMs != Long.MAX_VALUE;
    }

    /**
     * Marks this signal group as expired: sets the phase to {@link SignalPhase#UNAVAILABLE}
     * and clears the timing fields.
     * <p>
     * Package-private: called exclusively by
     * {@link SignalController#checkAndExpireSignalGroups()} and
     * {@link SignalController#forceExpireAllSignalGroups()}.
     */
    void markExpired() {
        this.expired = true;
        this.phase = SignalPhase.UNAVAILABLE;
        this.minEndTime = null;
        this.maxEndTime = null;
        this.phaseExpiryMs = 0L;
    }

    // -------------------------------------------------------------------------
    // Update (called by SignalController)
    // -------------------------------------------------------------------------

    /** Package-private: only {@link SignalController} calls this. */
    void update(int newEventState, Integer newMinEndTime, Integer newMaxEndTime) {
        this.phase = SignalPhase.fromEtsiValue(newEventState);
        this.minEndTime = newMinEndTime;
        this.maxEndTime = newMaxEndTime;
        this.expired = false;
        this.phaseExpiryMs = computePhaseExpiryMs(newMinEndTime, newMaxEndTime);
    }

    /**
     * Adds or updates the stop-line position for a specific lane.
     * Package-private: only {@link SignalController} calls this during MAPEM resolution.
     *
     * @param laneId   the MAPEM lane ID
     * @param position the WGS-84 stop-line position (first node of the lane centre line)
     */
    void addLanePosition(int laneId, LatLng position) {
        laneLevelPositions.put(laneId, position);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static long computePhaseExpiryMs(Integer minEndTime, Integer maxEndTime) {
        long now = System.currentTimeMillis();
        if (maxEndTime != null && maxEndTime < 36000) {
            return EtsiConverter.spatemTimeMarkToWallClock(maxEndTime, now);
        }
        if (minEndTime != null && minEndTime < 36000) {
            return EtsiConverter.spatemTimeMarkToWallClock(minEndTime, now) + SIGNAL_GROUP_EXPIRY_GRACE_MS;
        }
        return Long.MAX_VALUE;
    }
}

