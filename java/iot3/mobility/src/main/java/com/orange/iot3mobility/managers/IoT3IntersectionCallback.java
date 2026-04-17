/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.roadobjects.RoadGeometry;
import com.orange.iot3mobility.roadobjects.SignalController;
import com.orange.iot3mobility.roadobjects.SignalGroup;
import com.orange.iot3mobility.roadobjects.SignalGroupUpdateType;

/**
 * Unified callback interface for intersection-related events derived from both MAPEM and SPATEM messages.
 * <p>
 * Use together with
 * {@link com.orange.iot3mobility.IoT3Mobility#setIntersectionRoI(com.orange.iot3mobility.quadkey.LatLng, int, boolean)}
 * and
 * {@link com.orange.iot3mobility.IoT3Mobility#setIntersectionCallback(IoT3IntersectionCallback)}
 * to subscribe to both MAPEM geometry and SPATEM signal phase data with a single setup call.
 * <p>
 * Position resolution between SPATEM signal groups and MAPEM intersection geometry is automatic:
 * when a MAPEM message arrives that matches the region and intersection ID of an already-known
 * signal controller, the signal group positions are resolved without any additional action from the caller.
 * <p>
 * This interface composes {@link IoT3RoadGeometryCallback} and {@link IoT3SignalControllerCallback}.
 */
public interface IoT3IntersectionCallback extends IoT3RoadGeometryCallback, IoT3SignalControllerCallback {

    // -------------------------------------------------------------------------
    // IoT3RoadGeometryCallback (MAPEM)
    // -------------------------------------------------------------------------

    /**
     * Called for every MAPEM message received, before object-level processing.
     *
     * @param mapemFrame the raw decoded MAPEM frame
     */
    @Override
    void mapemArrived(MapemCodec.MapemFrame<?> mapemFrame);

    /**
     * Called when geometry from a brand-new source is discovered for the first time.
     * The {@code roadGeometry} object is already populated with its intersections and segments.
     *
     * @param roadGeometry the newly created geometry container
     */
    @Override
    void newRoadGeometry(RoadGeometry roadGeometry);

    /**
     * Called when an existing geometry container receives an update where at least one
     * child intersection or segment accepted a strictly higher revision.
     *
     * @param roadGeometry the updated geometry container
     */
    @Override
    void roadGeometryUpdated(RoadGeometry roadGeometry);

    // -------------------------------------------------------------------------
    // IoT3TrafficLightCallback (SPATEM)
    // -------------------------------------------------------------------------

    /**
     * Called for every SPATEM message received, before object-level processing.
     *
     * @param spatemFrame the raw decoded SPATEM frame
     */
    @Override
    void spatemArrived(SpatemCodec.SpatemFrame<?> spatemFrame);

    /**
     * Called when signal phase data for a brand-new intersection is discovered for the first time.
     * The {@code trafficLight} object is already populated with its signal group states.
     *
     * @param signalController the newly created signal controller object
     */
    @Override
    void newSignalController(SignalController signalController);

    /**
     * Called when an existing signal controller receives updated signal phase data.
     *
     * @param signalController the updated signal controller object
     */
    @Override
    void signalControllerUpdated(SignalController signalController);

    /**
     * Called when a signal controller has not received any SPATEM update within its lifetime
     * ({@link SignalController#LIFETIME} ms) and is removed
     * from the manager.
     *
     * @param signalController the expired signal controller object
     */
    @Override
    void signalControllerExpired(SignalController signalController);

    /**
     * Called when a {@link SignalGroup} is seen for the first time within a {@link SignalController}.
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the newly discovered signal group
     */
    @Override
    default void newSignalGroup(SignalController signalController, SignalGroup signalGroup) {}

    /**
     * Called when a {@link SignalGroup}'s state changes: its phase, timing, or stop-line
     * position was updated.
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the updated signal group
     * @param updateType   what changed: phase, timing, both, or position
     */
    @Override
    default void signalGroupUpdated(SignalController signalController, SignalGroup signalGroup,
                                    SignalGroupUpdateType updateType) {}

    /**
     * Called when a {@link SignalGroup}'s phase deadline has passed or the parent
     * {@link SignalController} expired.
     *
     * @param signalController the parent signal controller
     * @param signalGroup  the expired signal group
     */
    @Override
    default void signalGroupExpired(SignalController signalController, SignalGroup signalGroup) {}
}


