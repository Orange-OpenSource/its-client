/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Container representing all MAPEM-derived road geometry published by a single station.
 * <p>
 * A {@code RoadGeometry} aggregates all {@link RoadIntersection} and {@link RoadSegment} entries
 * extracted from MAPEM messages received from the same station. Each child entry is updated
 * independently with a per-entry revision guard: a new frame is applied to a child only when its
 * revision is strictly higher than the currently stored one.
 * <p>
 * The {@code uuid} is a composite string {@code {sourceUuid}_{stationId}} that uniquely identifies
 * the originating station across all MAPEM sources.
 * <p>
 * The object lives indefinitely; call {@link com.orange.iot3mobility.managers.RoadGeometryManager#clear()}
 * to remove all road geometry entries when leaving a geographic area.
 */
public class RoadGeometry {

    /** Composite identifier: {@code {sourceUuid}_{stationId}}. */
    private final String uuid;

    private final ArrayList<RoadIntersection> intersections = new ArrayList<>();
    private final HashMap<String, RoadIntersection> intersectionMap = new HashMap<>();

    private final ArrayList<RoadSegment> segments = new ArrayList<>();
    private final HashMap<String, RoadSegment> segmentMap = new HashMap<>();

    /** The most recently received MAPEM frame from this source. */
    private MapemCodec.MapemFrame<?> latestFrame;

    public RoadGeometry(String uuid, MapemCodec.MapemFrame<?> initialFrame) {
        this.uuid = uuid;
        this.latestFrame = initialFrame;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getUuid() { return uuid; }

    public MapemCodec.MapemFrame<?> getLatestFrame() { return latestFrame; }

    /** Unmodifiable view of all intersections from this source. */
    public List<RoadIntersection> getIntersections() {
        return Collections.unmodifiableList(intersections);
    }

    /** Unmodifiable view of all road segments from this source. */
    public List<RoadSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Look up a specific intersection by its region and intersection IDs.
     *
     * @param regionId       region ID (0 when absent)
     * @param intersectionId local intersection ID
     * @return the matching {@link RoadIntersection}, or {@code null} if not found
     */
    public synchronized RoadIntersection getIntersection(int regionId, int intersectionId) {
        return intersectionMap.get(regionId + "_" + intersectionId);
    }

    // -------------------------------------------------------------------------
    // Internal update (called by RoadGeometryManager)
    // -------------------------------------------------------------------------

    /**
     * Add or update an intersection within this container.
     *
     * @return {@code true} if a new intersection was created or an existing one accepted a higher revision;
     *         {@code false} if the revision guard rejected the update.
     */
    public synchronized boolean updateIntersection(LatLng refPoint, int revision,
                                             int regionId, int intersectionId,
                                             MapemCodec.MapemFrame<?> frame) {
        latestFrame = frame;
        String key = regionId + "_" + intersectionId;
        RoadIntersection existing = intersectionMap.get(key);
        if (existing != null) {
            return existing.update(refPoint, revision, frame);
        }
        RoadIntersection ri = new RoadIntersection(refPoint, revision, regionId, intersectionId, frame);
        intersections.add(ri);
        intersectionMap.put(key, ri);
        return true;
    }

    /**
     * Add or update a road segment within this container.
     *
     * @return {@code true} if a new segment was created or an existing one accepted a higher revision.
     */
    public synchronized boolean updateSegment(LatLng refPoint, int revision,
                                        int regionId, int segmentId,
                                        MapemCodec.MapemFrame<?> frame) {
        latestFrame = frame;
        String key = regionId + "_" + segmentId;
        RoadSegment existing = segmentMap.get(key);
        if (existing != null) {
            return existing.update(refPoint, revision, frame);
        }
        RoadSegment seg = new RoadSegment(refPoint, revision, regionId, segmentId, frame);
        segments.add(seg);
        segmentMap.put(key, seg);
        return true;
    }

    /** Remove all children. Called by {@link com.orange.iot3mobility.managers.RoadGeometryManager#clear()}. */
    public synchronized void clearChildren() {
        intersections.clear();
        intersectionMap.clear();
        segments.clear();
        segmentMap.clear();
    }
}
