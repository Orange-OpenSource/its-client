package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.ConnectingLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.ConnectsTo;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneAttributes;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneType;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeDelta;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeList;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXYOffset;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

/**
 * Factory producing a minimal MAPEM v2.0.0 test message.
 * <p>
 * The generated message describes a fictitious T-shaped intersection near the given reference
 * position, with two independent approach directions:
 * <ul>
 *   <li>Approach 1 (north–south axis): lane 1 ingress (from south) → signal group 1 → lane 2 egress (to north).</li>
 *   <li>Approach 2 (east–west axis): lane 3 ingress (from east) → signal group 2 → lane 4 egress (to west).</li>
 * </ul>
 * The {@code connects_to} entries on the two ingress lanes let the SDK resolve the stop-line
 * position of each signal group declared in the companion {@link SpatemV200Factory}.
 */
final class MapemV200Factory {

    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 654321L;
    private static final int MSG_ISSUE_REVISION = 0;
    private static final int REGION_ID = 10;
    private static final int INTERSECTION_ID = 1001;
    private static final int INTERSECTION_REVISION = 1;
    private static final int LANE_WIDTH_CM = 350;

    // Lane IDs
    private static final int LANE_ID_INGRESS_NORTH_SOUTH = 1;
    private static final int LANE_ID_EGRESS_NORTH_SOUTH  = 2;
    private static final int LANE_ID_INGRESS_EAST_WEST   = 3;
    private static final int LANE_ID_EGRESS_EAST_WEST    = 4;

    // Signal group IDs — must match SpatemV200Factory
    private static final int SIGNAL_GROUP_NORTH_SOUTH = 1;
    private static final int SIGNAL_GROUP_EAST_WEST   = 2;

    private MapemV200Factory() {
        // Factory class
    }

    /**
     * Build a minimal MAPEM v2.0.0 envelope with one intersection at the given position.
     *
     * @param sourceUuid the source UUID of the emitting station
     * @param position   the geographic reference point for the intersection
     * @return the constructed {@link MapemEnvelope200}
     */
    static MapemEnvelope200 createTestMapemEnvelope(String sourceUuid, LatLng position) {
        Position3D refPoint = Position3D.builder()
                .latitude(EtsiConverter.latitudeEtsi(position.getLatitude()))
                .longitude(EtsiConverter.longitudeEtsi(position.getLongitude()))
                .build();

        // ── Approach 1: north–south axis ─────────────────────────────────────────
        // Lane 1 – ingress from south: 3 nodes offset southward (y = −1500 cm each).
        // The stop-line is at the first node, i.e. 45 m south of the ref point.
        // connects_to → lane 2, governed by signal group 1 (green in SPATEM).
        GenericLane ingressLaneNorthSouth = GenericLane.builder()
                .laneId(LANE_ID_INGRESS_NORTH_SOUTH)
                .ingressApproach(1)
                .laneAttributes(LaneAttributes.builder()
                        .directionalUse(LaneDirection.INGRESS_PATH)
                        .sharedWith()
                        .laneType(new LaneType(List.of(), null, null, null, null, null, null, null))
                        .build())
                .nodeList(new NodeList(List.of(
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(0, -1500), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(0, -1500), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(0, -1500), null)).build()
                ), null))
                .connectsTo(List.of(
                        ConnectsTo.builder()
                                .connectingLane(ConnectingLane.builder()
                                        .lane(LANE_ID_EGRESS_NORTH_SOUTH)
                                        .maneuver()
                                        .build())
                                .signalGroup(SIGNAL_GROUP_NORTH_SOUTH)
                                .build()))
                .build();

        // Lane 2 – egress to north: 3 nodes offset northward (y = +1500 cm each),
        // shifted 350 cm east to model the opposite side of the road.
        GenericLane egressLaneNorthSouth = GenericLane.builder()
                .laneId(LANE_ID_EGRESS_NORTH_SOUTH)
                .egressApproach(1)
                .laneAttributes(LaneAttributes.builder()
                        .directionalUse(LaneDirection.EGRESS_PATH)
                        .sharedWith()
                        .laneType(new LaneType(List.of(), null, null, null, null, null, null, null))
                        .build())
                .nodeList(new NodeList(List.of(
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(350, 1500), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(0,   1500), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(0,   1500), null)).build()
                ), null))
                .build();

        // ── Approach 2: east–west axis ────────────────────────────────────────────
        // Lane 3 – ingress from east: 3 nodes offset eastward (x = +1500 cm each).
        // The stop-line is at the first node, i.e. 45 m east of the ref point.
        // connects_to → lane 4, governed by signal group 2 (red in SPATEM).
        GenericLane ingressLaneEastWest = GenericLane.builder()
                .laneId(LANE_ID_INGRESS_EAST_WEST)
                .ingressApproach(2)
                .laneAttributes(LaneAttributes.builder()
                        .directionalUse(LaneDirection.INGRESS_PATH)
                        .sharedWith()
                        .laneType(new LaneType(List.of(), null, null, null, null, null, null, null))
                        .build())
                .nodeList(new NodeList(List.of(
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(1500, 0), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(1500, 0), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(1500, 0), null)).build()
                ), null))
                .connectsTo(List.of(
                        ConnectsTo.builder()
                                .connectingLane(ConnectingLane.builder()
                                        .lane(LANE_ID_EGRESS_EAST_WEST)
                                        .maneuver()
                                        .build())
                                .signalGroup(SIGNAL_GROUP_EAST_WEST)
                                .build()))
                .build();

        // Lane 4 – egress to west: 3 nodes offset westward (x = −1500 cm each),
        // shifted 350 cm south to model the opposite side of the road.
        GenericLane egressLaneEastWest = GenericLane.builder()
                .laneId(LANE_ID_EGRESS_EAST_WEST)
                .egressApproach(2)
                .laneAttributes(LaneAttributes.builder()
                        .directionalUse(LaneDirection.EGRESS_PATH)
                        .sharedWith()
                        .laneType(new LaneType(List.of(), null, null, null, null, null, null, null))
                        .build())
                .nodeList(new NodeList(List.of(
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(-1500, -350), null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(-1500, 0),    null)).build(),
                        NodeXY.builder().delta(new NodeDelta(new NodeXYOffset(-1500, 0),    null)).build()
                ), null))
                .build();

        IntersectionGeometry intersection = IntersectionGeometry.builder()
                .id(new IntersectionReferenceId(REGION_ID, INTERSECTION_ID))
                .revision(INTERSECTION_REVISION)
                .refPoint(refPoint)
                .laneWidth(LANE_WIDTH_CM)
                .laneSet(List.of(
                        ingressLaneNorthSouth,
                        egressLaneNorthSouth,
                        ingressLaneEastWest,
                        egressLaneEastWest))
                .build();

        return MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(MapemMessage200.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .msgIssueRevision(MSG_ISSUE_REVISION)
                        .intersections(List.of(intersection))
                        .build())
                .build();
    }
}
