/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemVersion;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.*;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.*;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.LaneGeometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts MAPEM version-specific lane data into the version-agnostic {@link LaneGeometry} model.
 * <p>
 * This is the <strong>only class</strong> in the SDK that knows about MAPEM model internals
 * (GenericLane, NodeList, Position3D, etc.). All road objects call into this converter and
 * receive plain {@link LaneGeometry} objects in return.
 * <p>
 * When a new MAPEM version is added, only this class and the road-object getters need to be updated.
 * <p>
 * <b>Node-offset coordinate math</b>
 * <ul>
 *   <li>{@code node_lat_lon}: absolute position encoded in ETSI ×10⁷ degree units — decoded via
 *       {@link EtsiConverter#latitudeDegrees(int)} / {@link EtsiConverter#longitudeDegrees(int)}.</li>
 *   <li>{@code node_xy}: centimetre offsets in a local ENU frame, accumulated from the previous node
 *       (or refPoint for the first node). Converted using the flat-Earth approximation:
 *       {@code Δlat = y_cm / (111_111 × 100)}, {@code Δlon = x_cm / (111_111 × cos(lat) × 100)}.
 *       This is MAPEM-specific geometry, not an ETSI unit encoding — hence handled locally.
 *       Error is sub-centimetre for offsets below 500 m, which covers all MAPEM use cases.</li>
 *   <li>{@code computed} lanes (translation of another lane) are not resolved in this implementation;
 *       they return an empty centre line. TODO: resolve by looking up the reference lane.</li>
 * </ul>
 */
public final class MapemLaneConverter {

    /**
     * MAPEM-specific geometric constant: centimetres per degree of latitude (flat-Earth).
     * Used only for {@code node_xy} ENU offset conversion — this is NOT an ETSI encoding constant.
     * {@code node_lat_lon} and {@code ref_point} positions use {@link EtsiConverter} instead.
     */
    private static final double METERS_PER_DEGREE_LAT = 111_111.0;
    private static final double CM_PER_DEGREE_LAT = METERS_PER_DEGREE_LAT * 100.0;

    private MapemLaneConverter() {}

    // -------------------------------------------------------------------------
    // Main dispatch entry point used by road objects
    // -------------------------------------------------------------------------

    /**
     * Extract lanes for an intersection from the given frame.
     *
     * @param frame      the stored MAPEM frame
     * @param regionId   intersection region ID (0 when absent in the message)
     * @param geometryId local intersection ID
     * @return list of {@link LaneGeometry}; empty if the version is unsupported or no match found
     */
    public static List<LaneGeometry> intersectionLanes(MapemCodec.MapemFrame<?> frame,
                                                        int regionId, int geometryId) {
        if (frame.version() == MapemVersion.V2_0_0) {
            MapemEnvelope200 env = (MapemEnvelope200) frame.envelope();
            if (env.message().intersections() != null) {
                for (IntersectionGeometry ig : env.message().intersections()) {
                    int region = ig.id().region() != null ? ig.id().region() : 0;
                    if (region == regionId && ig.id().id() == geometryId) {
                        return toLaneGeometries(ig.laneSet(), ig.refPoint());
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Extract lanes for a road segment from the given frame.
     *
     * @param frame      the stored MAPEM frame
     * @param regionId   segment region ID (0 when absent)
     * @param geometryId local segment ID
     * @return list of {@link LaneGeometry}; empty if the version is unsupported or no match found
     */
    public static List<LaneGeometry> roadSegmentLanes(MapemCodec.MapemFrame<?> frame,
                                                       int regionId, int geometryId) {
        if (frame.version() == MapemVersion.V2_0_0) {
            MapemEnvelope200 env = (MapemEnvelope200) frame.envelope();
            if (env.message().roadSegments() != null) {
                for (RoadSegmentData rs : env.message().roadSegments()) {
                    int region = rs.id().region() != null ? rs.id().region() : 0;
                    if (region == regionId && rs.id().id() == geometryId) {
                        return toLaneGeometries(rs.roadLaneSet(), rs.refPoint());
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // v200 conversion
    // -------------------------------------------------------------------------

    /**
     * Convert a list of v200 {@link GenericLane} objects into {@link LaneGeometry} instances.
     *
     * @param laneSet  lane list from intersection or road segment
     * @param refPoint the absolute anchor for offset-based node coordinates
     */
    static List<LaneGeometry> toLaneGeometries(List<GenericLane> laneSet, Position3D refPoint) {
        if (laneSet == null) return Collections.emptyList();
        // Position3D uses ETSI ×10⁷ degree encoding, same as CAM/DENM — delegate to EtsiConverter
        double anchorLat = EtsiConverter.latitudeDegrees(refPoint.latitude());
        double anchorLon = EtsiConverter.longitudeDegrees(refPoint.longitude());

        List<LaneGeometry> result = new ArrayList<>(laneSet.size());
        for (GenericLane lane : laneSet) {
            List<LatLng> centerLine = buildCenterLine(lane.nodeList(), anchorLat, anchorLon);
            List<LaneDirection> directionalUse = resolveDirectionalUse(lane.laneAttributes().directionalUse());
            List<LaneTypeFlag> typeFlags = extractTypeFlags(lane.laneAttributes().laneType());
            List<Integer> signalGroups = extractSignalGroups(lane.connectsTo());
            result.add(new LaneGeometry(lane.laneId(), centerLine, directionalUse, typeFlags, signalGroups));
        }
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------------------
    // Centre-line builder
    // -------------------------------------------------------------------------

    private static List<LatLng> buildCenterLine(NodeList nodeList,
                                                 double anchorLat, double anchorLon) {
        if (nodeList == null) return Collections.emptyList();

        if (nodeList.computed() != null) {
            // TODO: resolve computed lane by looking up the reference lane's node list.
            // For now return empty so callers can detect unresolved computed lanes.
            return Collections.emptyList();
        }

        if (nodeList.nodes() == null || nodeList.nodes().isEmpty()) {
            return Collections.emptyList();
        }

        List<LatLng> path = new ArrayList<>(nodeList.nodes().size());
        double prevLat = anchorLat;
        double prevLon = anchorLon;

        for (NodeXY node : nodeList.nodes()) {
            NodeDelta delta = node.delta();
            if (delta.nodeLatLon() != null) {
                // Absolute position: ETSI ×10⁷ degree encoding — delegate to EtsiConverter
                prevLat = EtsiConverter.latitudeDegrees(delta.nodeLatLon().lat());
                prevLon = EtsiConverter.longitudeDegrees(delta.nodeLatLon().lon());
            } else if (delta.nodeXy() != null) {
                // Relative offset in cm in local ENU frame — accumulate from previous point
                double dLat = delta.nodeXy().y() / CM_PER_DEGREE_LAT;
                double dLon = delta.nodeXy().x() / (CM_PER_DEGREE_LAT * Math.cos(Math.toRadians(prevLat)));
                prevLat += dLat;
                prevLon += dLon;
            } else {
                // Malformed node: skip
                continue;
            }
            path.add(new LatLng(prevLat, prevLon));
        }
        return Collections.unmodifiableList(path);
    }

    // -------------------------------------------------------------------------
    // Lane type flags extraction
    // -------------------------------------------------------------------------

    /**
     * Resolve {@code directional_use} strings to {@link LaneDirection} enums.
     * Unrecognised values are silently ignored.
     */
    private static List<LaneDirection> resolveDirectionalUse(List<String> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<LaneDirection> result = new ArrayList<>(raw.size());
        for (String value : raw) {
            LaneDirection direction = LaneDirection.fromValue(value);
            if (direction != null) result.add(direction);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Return the active typed flag list from the oneOf {@link LaneType} structure.
     * Exactly one sub-field should be non-null; if none is, returns an empty list.
     * Unrecognised string values are silently ignored.
     */
    private static List<LaneTypeFlag> extractTypeFlags(LaneType lt) {
        if (lt == null) return Collections.emptyList();
        if (lt.vehicle()        != null) return resolveFlags(lt.vehicle(),        VehicleLaneAttribute::fromValue);
        if (lt.crosswalk()      != null) return resolveFlags(lt.crosswalk(),      CrosswalkAttribute::fromValue);
        if (lt.bikeLane()       != null) return resolveFlags(lt.bikeLane(),       BikeLaneAttribute::fromValue);
        if (lt.sidewalk()       != null) return resolveFlags(lt.sidewalk(),       SidewalkAttribute::fromValue);
        if (lt.median()         != null) return resolveFlags(lt.median(),         MedianAttribute::fromValue);
        if (lt.striping()       != null) return resolveFlags(lt.striping(),       StripingAttribute::fromValue);
        if (lt.trackedVehicle() != null) return resolveFlags(lt.trackedVehicle(), TrackedVehicleAttribute::fromValue);
        if (lt.parking()        != null) return resolveFlags(lt.parking(),        ParkingAttribute::fromValue);
        return Collections.emptyList();
    }

    private static List<LaneTypeFlag> resolveFlags(List<String> raw,
                                                    java.util.function.Function<String, ? extends LaneTypeFlag> resolver) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<LaneTypeFlag> result = new ArrayList<>(raw.size());
        for (String value : raw) {
            LaneTypeFlag flag = resolver.apply(value);
            if (flag != null) result.add(flag);
        }
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------------------
    // Signal groups extraction
    // -------------------------------------------------------------------------

    private static List<Integer> extractSignalGroups(List<ConnectsTo> connectsTo) {
        if (connectsTo == null || connectsTo.isEmpty()) return Collections.emptyList();
        List<Integer> groups = new ArrayList<>();
        for (ConnectsTo ct : connectsTo) {
            if (ct.signalGroup() != null) groups.add(ct.signalGroup());
        }
        return Collections.unmodifiableList(groups);
    }
}

