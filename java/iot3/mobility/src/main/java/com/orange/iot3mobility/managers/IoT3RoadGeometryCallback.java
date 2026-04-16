/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.roadobjects.RoadGeometry;

/**
 * Callback interface for MAPEM-derived road geometry events.
 * <p>
 * A {@link RoadGeometry} object groups all intersections and segments published by a single
 * MAPEM source ({@code sourceUuid}). The three callbacks correspond to the three
 * possible outcomes when a MAPEM message is received:
 * <ol>
 *   <li>{@link #newRoadGeometry} — first message from a new source; the geometry object is
 *       already populated with intersections/segments when the callback fires.</li>
 *   <li>{@link #roadGeometryUpdated} — subsequent message from a known source where at least
 *       one intersection or segment accepted a higher revision.</li>
 *   <li>{@link #mapemArrived} — fired for every MAPEM message before any object-level processing,
 *       regardless of the outcome above.</li>
 * </ol>
 */
public interface IoT3RoadGeometryCallback {

    /**
     * Called for every MAPEM message received, before object-level processing.
     *
     * @param mapemFrame the raw decoded MAPEM frame
     */
    void mapemArrived(MapemCodec.MapemFrame<?> mapemFrame);

    /**
     * Called when geometry from a brand-new source is discovered for the first time.
     * The {@code roadGeometry} object is already populated with its intersections and segments.
     *
     * @param roadGeometry the newly created geometry container
     */
    void newRoadGeometry(RoadGeometry roadGeometry);

    /**
     * Called when an existing geometry container receives an update where at least one
     * child intersection or segment accepted a strictly higher revision.
     *
     * @param roadGeometry the updated geometry container
     */
    void roadGeometryUpdated(RoadGeometry roadGeometry);
}
