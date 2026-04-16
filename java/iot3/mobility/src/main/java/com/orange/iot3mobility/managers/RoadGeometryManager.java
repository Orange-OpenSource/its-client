/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.mapem.MapemHelper;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemVersion;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadGeometry;
import com.orange.iot3mobility.roadobjects.RoadIntersection;
import com.orange.iot3mobility.roadobjects.RoadSegment;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages {@link RoadGeometry} containers derived from received MAPEM messages.
 * <p>
 * One {@link RoadGeometry} object is maintained per unique station, identified by the composite key
 * {@code {sourceUuid}_{stationId}}. It aggregates all intersections and road segments emitted by
 * that station, including any MAPEM layer fragments ({@code layerId > 0}) from the same station.
 * Objects live indefinitely; call {@link #clear()} to remove all entries when leaving a geographic area.
 * <p>
 * State is static — there is one shared store per JVM process.
 */
public class RoadGeometryManager {

    private static final Logger LOGGER = Logger.getLogger(RoadGeometryManager.class.getName());
    private static final String TAG = "IoT3Mobility.RoadGeometryManager";

    private static final ArrayList<RoadGeometry> ROAD_GEOMETRIES = new ArrayList<>();
    private static final HashMap<String, RoadGeometry> ROAD_GEOMETRY_MAP = new HashMap<>();

    private static IoT3RoadGeometryCallback ioT3RoadGeometryCallback;

    public static void init(IoT3RoadGeometryCallback callback) {
        RoadGeometryManager.ioT3RoadGeometryCallback = callback;
    }

    // -------------------------------------------------------------------------
    // MAPEM processing
    // -------------------------------------------------------------------------

    public static void processMapem(String message, MapemHelper mapemHelper) {
        if (ioT3RoadGeometryCallback == null) return;
        try {
            MapemCodec.MapemFrame<?> frame = mapemHelper.parse(message);
            ioT3RoadGeometryCallback.mapemArrived(frame);

            if (frame.version() == MapemVersion.V2_0_0) {
                MapemEnvelope200 envelope = (MapemEnvelope200) frame.envelope();
                String uuid = envelope.sourceUuid() + "_" + envelope.message().stationId();

                synchronized (ROAD_GEOMETRY_MAP) {
                    RoadGeometry roadGeometry = ROAD_GEOMETRY_MAP.get(uuid);
                    boolean isNew = roadGeometry == null;

                    if (isNew) {
                        roadGeometry = new RoadGeometry(uuid, frame);
                        ROAD_GEOMETRY_MAP.put(uuid, roadGeometry);
                        synchronized (ROAD_GEOMETRIES) {
                            ROAD_GEOMETRIES.add(roadGeometry);
                        }
                    }

                    boolean anyUpdate = populate(roadGeometry, envelope, frame);

                    if (isNew) {
                        ioT3RoadGeometryCallback.newRoadGeometry(roadGeometry);
                    } else if (anyUpdate) {
                        ioT3RoadGeometryCallback.roadGeometryUpdated(roadGeometry);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, TAG + " MAPEM parsing error: " + e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, TAG + " MAPEM processing error: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // Frame → RoadGeometry population (v200)
    // -------------------------------------------------------------------------

    private static boolean populate(RoadGeometry roadGeometry,
                                     MapemEnvelope200 envelope,
                                     MapemCodec.MapemFrame<?> frame) {
        boolean anyUpdate = false;

        if (envelope.message().intersections() != null) {
            for (IntersectionGeometry ig : envelope.message().intersections()) {
                int regionId = ig.id().region() != null ? ig.id().region() : 0;
                LatLng refPoint = toLatLng(ig.refPoint());
                anyUpdate |= roadGeometry.updateIntersection(
                        refPoint, ig.revision(), regionId, ig.id().id(), frame);
            }
        }

        if (envelope.message().roadSegments() != null) {
            for (RoadSegmentData rs : envelope.message().roadSegments()) {
                int regionId = rs.id().region() != null ? rs.id().region() : 0;
                LatLng refPoint = toLatLng(rs.refPoint());
                anyUpdate |= roadGeometry.updateSegment(
                        refPoint, rs.revision(), regionId, rs.id().id(), frame);
            }
        }

        return anyUpdate;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static LatLng toLatLng(Position3D pos) {
        return new LatLng(EtsiConverter.latitudeDegrees(pos.latitude()), EtsiConverter.longitudeDegrees(pos.longitude()));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Retrieve a read-only list of all known road geometry containers (one per MAPEM source).
     */
    public static List<RoadGeometry> getRoadGeometries() {
        return Collections.unmodifiableList(ROAD_GEOMETRIES);
    }

    /**
     * Retrieve a flat read-only list of all known road intersections across all sources.
     */
    public static List<RoadIntersection> getRoadIntersections() {
        List<RoadIntersection> all = new ArrayList<>();
        synchronized (ROAD_GEOMETRIES) {
            for (RoadGeometry rg : ROAD_GEOMETRIES) all.addAll(rg.getIntersections());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Retrieve a flat read-only list of all known road segments across all sources.
     */
    public static List<RoadSegment> getRoadSegments() {
        List<RoadSegment> all = new ArrayList<>();
        synchronized (ROAD_GEOMETRIES) {
            for (RoadGeometry rg : ROAD_GEOMETRIES) all.addAll(rg.getSegments());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Remove all stored road geometry. Use when leaving a geographic area or resetting state.
     */
    public static void clear() {
        synchronized (ROAD_GEOMETRY_MAP) {
            for (RoadGeometry rg : ROAD_GEOMETRIES) rg.clearChildren();
            ROAD_GEOMETRIES.clear();
            ROAD_GEOMETRY_MAP.clear();
        }
    }
}
