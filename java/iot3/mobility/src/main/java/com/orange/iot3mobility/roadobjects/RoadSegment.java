/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mapem.MapemLaneConverter;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

/**
 * Represents a single road segment geometry within a {@link RoadGeometry} container.
 * <p>
 * Instances are created and owned exclusively by {@link RoadGeometry}; they are not
 * constructed directly by application code.
 * <p>
 * Lane data is resolved on demand via {@link #getLanes()}, which delegates all
 * version-specific logic to {@link MapemLaneConverter}.
 */
public class RoadSegment {

    /** Region component of the globally unique segment ID (0 when absent). */
    private final int regionId;

    /** Local segment ID component. */
    private final int segmentId;

    private LatLng refPoint;
    private int revision;
    private MapemCodec.MapemFrame<?> mapemFrame;

    /** Package-private: constructed only by {@link RoadGeometry}. */
    RoadSegment(LatLng refPoint, int revision,
                int regionId, int segmentId,
                MapemCodec.MapemFrame<?> mapemFrame) {
        this.refPoint = refPoint;
        this.revision = revision;
        this.regionId = regionId;
        this.segmentId = segmentId;
        this.mapemFrame = mapemFrame;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getRegionId() { return regionId; }

    public int getSegmentId() { return segmentId; }

    public LatLng getRefPoint() { return refPoint; }

    public int getRevision() { return revision; }

    public MapemCodec.MapemFrame<?> getMapemFrame() { return mapemFrame; }

    // -------------------------------------------------------------------------
    // Update (revision-guarded — called by RoadGeometry)
    // -------------------------------------------------------------------------

    /** Package-private: only {@link RoadGeometry} calls this. */
    boolean update(LatLng newRefPoint, int newRevision, MapemCodec.MapemFrame<?> newFrame) {
        if (newRevision <= this.revision) return false;
        this.refPoint = newRefPoint;
        this.revision = newRevision;
        this.mapemFrame = newFrame;
        return true;
    }

    // -------------------------------------------------------------------------
    // Lane geometry
    // -------------------------------------------------------------------------

    /**
     * Return the lanes of this road segment as a version-agnostic list of {@link LaneGeometry}.
     * <p>
     * Each entry contains the lane's absolute WGS-84 centre-line path, directional-use flags,
     * lane-type flags, and signal-group IDs for SPATEM correlation.
     *
     * @return unmodifiable list of {@link LaneGeometry}; empty if no lanes can be resolved.
     */
    public List<LaneGeometry> getLanes() {
        return MapemLaneConverter.roadSegmentLanes(mapemFrame, regionId, segmentId);
    }
}
