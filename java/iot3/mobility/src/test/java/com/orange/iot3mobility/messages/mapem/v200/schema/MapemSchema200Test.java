/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mapem.v200.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemVersion;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.GenericLane;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneAttributes;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.LaneType;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeDelta;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeList;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.NodeXYOffset;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class MapemSchema200Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("mapem/mapem_schema_2-0-0.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        MapemEnvelope200 envelope = minimalEnvelope();
        MapemCodec codec = new MapemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(MapemVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        MapemEnvelope200 envelope = fullyPopulatedEnvelope();

        MapemCodec codec = new MapemCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(MapemVersion.V2_0_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static GenericLane validGenericLane() {
        NodeXYOffset nodeXYOffset = new NodeXYOffset(0, 0);
        NodeDelta nodeDelta = new NodeDelta(nodeXYOffset, null);
        NodeXY nodeXY = NodeXY.builder().delta(nodeDelta).build();
        NodeList nodeList = new NodeList(List.of(nodeXY, nodeXY), null);
        LaneType laneType = new LaneType(List.of(), null, null, null, null, null, null, null);
        LaneAttributes laneAttributes = LaneAttributes.builder()
                .directionalUse(LaneDirection.INGRESS_PATH)
                .sharedWith()
                .laneType(laneType)
                .build();
        return GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .build();
    }

    private static MapemEnvelope200 minimalEnvelope() {
        GenericLane lane = validGenericLane();

        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(null, 1001);
        IntersectionGeometry geometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(Position3D.builder().latitude(485000000).longitude(22000000).build())
                .laneSet(List.of(lane))
                .build();

        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .intersections(List.of(geometry))
                .build();

        return MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static MapemEnvelope200 fullyPopulatedEnvelope() {
        GenericLane lane = validGenericLane();

        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(null, 1001);
        IntersectionGeometry geometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(Position3D.builder().latitude(485000000).longitude(22000000).build())
                .laneSet(List.of(lane))
                .build();

        RoadSegmentReferenceId roadSegmentReferenceId = new RoadSegmentReferenceId(null, 2001);
        RoadSegmentData roadSegment = RoadSegmentData.builder()
                .id(roadSegmentReferenceId)
                .revision(0)
                .refPoint(Position3D.builder().latitude(485100000).longitude(22100000).build())
                .roadLaneSet(List.of(lane))
                .build();

        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .intersections(List.of(geometry))
                .roadSegments(List.of(roadSegment))
                .build();

        return MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
