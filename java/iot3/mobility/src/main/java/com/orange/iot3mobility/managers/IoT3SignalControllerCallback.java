/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.roadobjects.SignalGroup;
import com.orange.iot3mobility.roadobjects.SignalGroupUpdateType;
import com.orange.iot3mobility.roadobjects.SignalController;

/**
 * Callback interface for SPATEM-derived signal controller events.
 * <p>
 * A {@link SignalController} object represents one intersection's signal phase state, derived from
 * incoming SPATEM messages. The three intersection-level callbacks correspond to the three possible
 * outcomes when a SPATEM message is received:
 * <ol>
 *   <li>{@link #newSignalController} — first message from a new intersection; the object is already
 *       populated with {@link com.orange.iot3mobility.roadobjects.SignalGroup} entries.</li>
 *   <li>{@link #signalControllerUpdated} — subsequent message updating an existing intersection.</li>
 *   <li>{@link #spatemArrived} — fired for every SPATEM message before any object-level processing,
 *       regardless of the outcome above.</li>
 * </ol>
 * <p>
 * Three additional signal-group-level callbacks offer finer granularity.
 * <ul>
 *   <li>{@link #newSignalGroup} — a signal group is seen for the first time.</li>
 *   <li>{@link #signalGroupUpdated} — a signal group's phase, timing, or position changed.</li>
 *   <li>{@link #signalGroupExpired} — a signal group's phase deadline passed or the parent
 *       signal controller expired; the group's phase is set to
 *       {@link com.orange.iot3mobility.roadobjects.SignalPhase#UNAVAILABLE}.</li>
 * </ul>
 */
public interface IoT3SignalControllerCallback {

    /**
     * Called for every SPATEM message received, before object-level processing.
     *
     * @param spatemFrame the raw decoded SPATEM frame
     */
    void spatemArrived(SpatemCodec.SpatemFrame<?> spatemFrame);

    /**
     * Called when signal phase data for a brand-new intersection is discovered for the first time.
     * The {@code trafficLight} object is already populated with its signal group states.
     *
     * @param signalController the newly created signal controller object
     */
    void newSignalController(SignalController signalController);

    /**
     * Called when an existing signal controller receives updated signal phase data.
     *
     * @param signalController the updated signal controller object
     */
    void signalControllerUpdated(SignalController signalController);

    /**
     * Called when a signal controller has not received any SPATEM update within its lifetime
     * ({@link SignalController#MAX_STALENESS_MS}) and is removed from the manager.
     *
     * @param signalController the expired signal controller object
     */
    void signalControllerExpired(SignalController signalController);

    // -------------------------------------------------------------------------
    // Signal group level (default no-op — override to enable fine-grained tracking)
    // -------------------------------------------------------------------------

    /**
     * Called when a {@link SignalGroup} is seen for the first time within a
     * {@link SignalController}.
     * <p>
     * Fired immediately after {@link #newSignalController} for each group present in the first
     * SPATEM frame, and again whenever a later SPATEM introduces a previously unseen group
     * (preceded by {@link #signalControllerUpdated}).
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the newly discovered signal group
     */
    default void newSignalGroup(SignalController signalController, SignalGroup signalGroup) {}

    /**
     * Called when a {@link SignalGroup}'s state changes: its phase, timing, or stop-line
     * position was updated.
     * <p>
     * The {@code updateType} parameter describes what changed, so callers that only care
     * about phase changes can ignore {@link SignalGroupUpdateType#TIMING} events cheaply.
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the updated signal group (already reflects the new state)
     * @param updateType   what changed: phase, timing, both, or position
     */
    default void signalGroupUpdated(SignalController signalController, SignalGroup signalGroup,
                                    SignalGroupUpdateType updateType) {}

    /**
     * Called when a {@link SignalGroup}'s phase deadline has passed without a refreshing
     * SPATEM update, or when the parent {@link SignalController} itself expires.
     * <p>
     * At the time this callback fires, {@link SignalGroup#getPhase()} already returns
     * {@link com.orange.iot3mobility.roadobjects.SignalPhase#UNAVAILABLE}.
     * When the signal controller expires, this callback is fired for every remaining live signal
     * group before {@link #signalControllerExpired} is fired.
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the expired signal group
     */
    default void signalGroupExpired(SignalController signalController, SignalGroup signalGroup) {}
}

