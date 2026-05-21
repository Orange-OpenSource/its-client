/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mapem;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemException;
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
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapemCodecTest {

    @Test
    void writeReadV200RoundTrip() throws Exception {
        MapemEnvelope200 envelope = validEnvelope200();
        MapemCodec codec = new MapemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(MapemVersion.V2_0_0, envelope, out);

        MapemCodec.MapemFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(MapemVersion.V2_0_0, frame.version());
        assertTrue(frame.envelope() instanceof MapemEnvelope200);
        MapemEnvelope200 parsed = (MapemEnvelope200) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void readStringV200RoundTrip() throws Exception {
        MapemEnvelope200 envelope = validEnvelope200();
        MapemCodec codec = new MapemCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(MapemVersion.V2_0_0, envelope, out);

        MapemCodec.MapemFrame<?> frame = codec.read(out.toString());
        assertEquals(MapemVersion.V2_0_0, frame.version());
        MapemEnvelope200 parsed = (MapemEnvelope200) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
    }

    @Test
    void readRejectsMissingVersion() {
        MapemCodec codec = new MapemCodec(new JsonFactory());
        String json = "{\"type\":\"mapem\"}";
        assertThrows(MapemException.class, () -> codec.read(json));
    }

    private static MapemEnvelope200 validEnvelope200() {
        NodeXYOffset nodeXYOffset = new NodeXYOffset(0, 0);
        NodeDelta nodeDelta = new NodeDelta(nodeXYOffset, null);
        NodeXY nodeXY = NodeXY.builder().delta(nodeDelta).build();

        LaneType laneType = new LaneType(List.of(), null, null, null, null, null, null, null);
        LaneAttributes laneAttributes = LaneAttributes.builder()
                .directionalUse(LaneDirection.INGRESS_PATH)
                .sharedWith()
                .laneType(laneType)
                .build();

        NodeList nodeList = new NodeList(List.of(nodeXY, nodeXY), null);

        GenericLane genericLane = GenericLane.builder()
                .laneId(1)
                .laneAttributes(laneAttributes)
                .nodeList(nodeList)
                .build();

        Position3D refPoint = Position3D.builder()
                .latitude(485000000)
                .longitude(22000000)
                .build();

        IntersectionReferenceId intersectionReferenceId = new IntersectionReferenceId(null, 1001);
        IntersectionGeometry intersectionGeometry = IntersectionGeometry.builder()
                .id(intersectionReferenceId)
                .revision(0)
                .refPoint(refPoint)
                .laneSet(List.of(genericLane))
                .build();

        MapemMessage200 message = MapemMessage200.builder()
                .protocolVersion(1)
                .stationId(42L)
                .msgIssueRevision(0)
                .intersections(List.of(intersectionGeometry))
                .build();

        return MapemEnvelope200.builder()
                .origin("self")
                .sourceUuid("rsu_intersection_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}

