/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemMessage200;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionGeometry;
import com.orange.iot3mobility.messages.mapem.v200.model.intersection.IntersectionReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.*;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentData;
import com.orange.iot3mobility.messages.mapem.v200.model.roadsegment.RoadSegmentReferenceId;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.DataParameters;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.Position3D;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.RestrictionClassAssignment;
import com.orange.iot3mobility.messages.mapem.v200.model.shared.SpeedLimit;
import com.orange.iot3mobility.messages.mapem.v200.validation.MapemValidator200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Streaming JSON writer for MAPEM v2.0.0 payloads.
 */
public final class MapemWriter200 {

    private final JsonFactory jsonFactory;

    public MapemWriter200(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public void write(MapemEnvelope200 envelope, OutputStream out) throws IOException {
        MapemValidator200.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            gen.writeStringField("origin", envelope.origin());
            gen.writeStringField("version", envelope.version());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Message                                                               */
    /* --------------------------------------------------------------------- */

    private void writeMessage(JsonGenerator gen, MapemMessage200 msg) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", msg.protocolVersion());
        gen.writeNumberField("station_id", msg.stationId());
        if (msg.timestamp() != null) gen.writeNumberField("timestamp", msg.timestamp());
        gen.writeNumberField("msg_issue_revision", msg.msgIssueRevision());
        if (msg.layerType() != null) gen.writeStringField("layer_type", msg.layerType());
        if (msg.layerId() != null) gen.writeNumberField("layer_id", msg.layerId());

        if (msg.intersections() != null && !msg.intersections().isEmpty()) {
            gen.writeArrayFieldStart("intersections");
            for (IntersectionGeometry ig : msg.intersections()) writeIntersectionGeometry(gen, ig);
            gen.writeEndArray();
        }
        if (msg.roadSegments() != null && !msg.roadSegments().isEmpty()) {
            gen.writeArrayFieldStart("road_segments");
            for (RoadSegmentData rs : msg.roadSegments()) writeRoadSegmentData(gen, rs);
            gen.writeEndArray();
        }
        if (msg.dataParameters() != null) {
            gen.writeFieldName("data_parameters");
            writeDataParameters(gen, msg.dataParameters());
        }
        if (msg.restrictionList() != null && !msg.restrictionList().isEmpty()) {
            gen.writeArrayFieldStart("restriction_list");
            for (RestrictionClassAssignment r : msg.restrictionList()) writeRestrictionClassAssignment(gen, r);
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Intersections                                                         */
    /* --------------------------------------------------------------------- */

    private void writeIntersectionGeometry(JsonGenerator gen, IntersectionGeometry ig) throws IOException {
        gen.writeStartObject();
        if (ig.name() != null) gen.writeStringField("name", ig.name());
        gen.writeFieldName("id");
        writeIntersectionReferenceId(gen, ig.id());
        gen.writeNumberField("revision", ig.revision());
        gen.writeFieldName("ref_point");
        writePosition3D(gen, ig.refPoint());
        if (ig.laneWidth() != null) gen.writeNumberField("lane_width", ig.laneWidth());
        if (ig.speedLimits() != null && !ig.speedLimits().isEmpty()) {
            gen.writeArrayFieldStart("speed_limits");
            for (SpeedLimit sl : ig.speedLimits()) writeSpeedLimit(gen, sl);
            gen.writeEndArray();
        }
        gen.writeArrayFieldStart("lane_set");
        for (GenericLane lane : ig.laneSet()) writeGenericLane(gen, lane);
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeIntersectionReferenceId(JsonGenerator gen, IntersectionReferenceId id) throws IOException {
        gen.writeStartObject();
        if (id.region() != null) gen.writeNumberField("region", id.region());
        gen.writeNumberField("id", id.id());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Road segments                                                         */
    /* --------------------------------------------------------------------- */

    private void writeRoadSegmentData(JsonGenerator gen, RoadSegmentData rs) throws IOException {
        gen.writeStartObject();
        if (rs.name() != null) gen.writeStringField("name", rs.name());
        gen.writeFieldName("id");
        writeRoadSegmentReferenceId(gen, rs.id());
        gen.writeNumberField("revision", rs.revision());
        gen.writeFieldName("ref_point");
        writePosition3D(gen, rs.refPoint());
        if (rs.laneWidth() != null) gen.writeNumberField("lane_width", rs.laneWidth());
        if (rs.speedLimits() != null && !rs.speedLimits().isEmpty()) {
            gen.writeArrayFieldStart("speed_limits");
            for (SpeedLimit sl : rs.speedLimits()) writeSpeedLimit(gen, sl);
            gen.writeEndArray();
        }
        gen.writeArrayFieldStart("road_lane_set");
        for (GenericLane lane : rs.roadLaneSet()) writeGenericLane(gen, lane);
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeRoadSegmentReferenceId(JsonGenerator gen, RoadSegmentReferenceId id) throws IOException {
        gen.writeStartObject();
        if (id.region() != null) gen.writeNumberField("region", id.region());
        gen.writeNumberField("id", id.id());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Shared: Position3D, SpeedLimit                                       */
    /* --------------------------------------------------------------------- */

    private void writePosition3D(JsonGenerator gen, Position3D pos) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", pos.latitude());
        gen.writeNumberField("longitude", pos.longitude());
        if (pos.elevation() != null) gen.writeNumberField("elevation", pos.elevation());
        gen.writeEndObject();
    }

    private void writeSpeedLimit(JsonGenerator gen, SpeedLimit sl) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", sl.type());
        gen.writeNumberField("speed", sl.speed());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Lanes                                                                 */
    /* --------------------------------------------------------------------- */

    private void writeGenericLane(JsonGenerator gen, GenericLane lane) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("lane_id", lane.laneId());
        if (lane.name() != null) gen.writeStringField("name", lane.name());
        if (lane.ingressApproach() != null) gen.writeNumberField("ingress_approach", lane.ingressApproach());
        if (lane.egressApproach() != null) gen.writeNumberField("egress_approach", lane.egressApproach());
        gen.writeFieldName("lane_attributes");
        writeLaneAttributes(gen, lane.laneAttributes());
        if (lane.maneuvers() != null && !lane.maneuvers().isEmpty()) {
            writeStringArray(gen, "maneuvers", lane.maneuvers());
        }
        gen.writeFieldName("node_list");
        writeNodeList(gen, lane.nodeList());
        if (lane.connectsTo() != null && !lane.connectsTo().isEmpty()) {
            gen.writeArrayFieldStart("connects_to");
            for (ConnectsTo c : lane.connectsTo()) writeConnectsTo(gen, c);
            gen.writeEndArray();
        }
        if (lane.speedLimits() != null && !lane.speedLimits().isEmpty()) {
            gen.writeArrayFieldStart("speed_limits");
            for (SpeedLimit sl : lane.speedLimits()) writeSpeedLimit(gen, sl);
            gen.writeEndArray();
        }
        if (lane.overlays() != null && !lane.overlays().isEmpty()) {
            gen.writeArrayFieldStart("overlays");
            for (Integer o : lane.overlays()) gen.writeNumber(o);
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    private void writeLaneAttributes(JsonGenerator gen, LaneAttributes la) throws IOException {
        gen.writeStartObject();
        writeStringArray(gen, "directional_use", la.directionalUse());
        writeStringArray(gen, "shared_with", la.sharedWith());
        gen.writeFieldName("lane_type");
        writeLaneType(gen, la.laneType());
        gen.writeEndObject();
    }

    private void writeLaneType(JsonGenerator gen, LaneType lt) throws IOException {
        gen.writeStartObject();
        if (lt.vehicle() != null)        writeStringArray(gen, "vehicle", lt.vehicle());
        if (lt.crosswalk() != null)      writeStringArray(gen, "crosswalk", lt.crosswalk());
        if (lt.bikeLane() != null)       writeStringArray(gen, "bike_lane", lt.bikeLane());
        if (lt.sidewalk() != null)       writeStringArray(gen, "sidewalk", lt.sidewalk());
        if (lt.median() != null)         writeStringArray(gen, "median", lt.median());
        if (lt.striping() != null)       writeStringArray(gen, "striping", lt.striping());
        if (lt.trackedVehicle() != null) writeStringArray(gen, "tracked_vehicle", lt.trackedVehicle());
        if (lt.parking() != null)        writeStringArray(gen, "parking", lt.parking());
        gen.writeEndObject();
    }

    private void writeNodeList(JsonGenerator gen, NodeList nl) throws IOException {
        gen.writeStartObject();
        if (nl.nodes() != null) {
            gen.writeArrayFieldStart("nodes");
            for (NodeXY node : nl.nodes()) writeNodeXY(gen, node);
            gen.writeEndArray();
        } else if (nl.computed() != null) {
            gen.writeFieldName("computed");
            writeComputedLane(gen, nl.computed());
        }
        gen.writeEndObject();
    }

    private void writeNodeXY(JsonGenerator gen, NodeXY node) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("delta");
        writeNodeDelta(gen, node.delta());
        if (node.attributes() != null) {
            gen.writeFieldName("attributes");
            writeNodeAttributes(gen, node.attributes());
        }
        gen.writeEndObject();
    }

    private void writeNodeDelta(JsonGenerator gen, NodeDelta delta) throws IOException {
        gen.writeStartObject();
        if (delta.nodeXy() != null) {
            gen.writeFieldName("node_xy");
            gen.writeStartObject();
            gen.writeNumberField("x", delta.nodeXy().x());
            gen.writeNumberField("y", delta.nodeXy().y());
            gen.writeEndObject();
        } else if (delta.nodeLatLon() != null) {
            gen.writeFieldName("node_lat_lon");
            gen.writeStartObject();
            gen.writeNumberField("lat", delta.nodeLatLon().lat());
            gen.writeNumberField("lon", delta.nodeLatLon().lon());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private void writeNodeAttributes(JsonGenerator gen, NodeAttributes attrs) throws IOException {
        gen.writeStartObject();
        if (attrs.localNode() != null && !attrs.localNode().isEmpty())
            writeStringArray(gen, "local_node", attrs.localNode());
        if (attrs.disabled() != null && !attrs.disabled().isEmpty())
            writeStringArray(gen, "disabled", attrs.disabled());
        if (attrs.enabled() != null && !attrs.enabled().isEmpty())
            writeStringArray(gen, "enabled", attrs.enabled());
        if (attrs.data() != null && !attrs.data().isEmpty()) {
            gen.writeArrayFieldStart("data");
            for (NodeAttributeData d : attrs.data()) writeNodeAttributeData(gen, d);
            gen.writeEndArray();
        }
        if (attrs.dWidth() != null) gen.writeNumberField("d_width", attrs.dWidth());
        if (attrs.dElevation() != null) gen.writeNumberField("d_elevation", attrs.dElevation());
        gen.writeEndObject();
    }

    private void writeNodeAttributeData(JsonGenerator gen, NodeAttributeData d) throws IOException {
        gen.writeStartObject();
        if (d.pathEndPointAngle() != null) gen.writeNumberField("path_end_point_angle", d.pathEndPointAngle());
        if (d.laneCrownPointCenter() != null) gen.writeNumberField("lane_crown_point_center", d.laneCrownPointCenter());
        if (d.laneCrownPointLeft() != null) gen.writeNumberField("lane_crown_point_left", d.laneCrownPointLeft());
        if (d.laneCrownPointRight() != null) gen.writeNumberField("lane_crown_point_right", d.laneCrownPointRight());
        if (d.laneAngle() != null) gen.writeNumberField("lane_angle", d.laneAngle());
        if (d.speedLimits() != null && !d.speedLimits().isEmpty()) {
            gen.writeArrayFieldStart("speed_limits");
            for (SpeedLimit sl : d.speedLimits()) writeSpeedLimit(gen, sl);
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    private void writeComputedLane(JsonGenerator gen, ComputedLane cl) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("reference_lane_id", cl.referenceLaneId());
        gen.writeNumberField("offset_x_axis", cl.offsetXAxis());
        gen.writeNumberField("offset_y_axis", cl.offsetYAxis());
        if (cl.rotateXy() != null) gen.writeNumberField("rotate_xy", cl.rotateXy());
        if (cl.scaleXAxis() != null) gen.writeNumberField("scale_x_axis", cl.scaleXAxis());
        if (cl.scaleYAxis() != null) gen.writeNumberField("scale_y_axis", cl.scaleYAxis());
        gen.writeEndObject();
    }

    private void writeConnectsTo(JsonGenerator gen, ConnectsTo ct) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("connecting_lane");
        gen.writeStartObject();
        gen.writeNumberField("lane", ct.connectingLane().lane());
        if (ct.connectingLane().maneuver() != null && !ct.connectingLane().maneuver().isEmpty()) {
            writeStringArray(gen, "maneuver", ct.connectingLane().maneuver());
        }
        gen.writeEndObject();
        if (ct.remoteIntersections() != null) {
            gen.writeFieldName("remote_intersections");
            writeIntersectionReferenceId(gen, ct.remoteIntersections());
        }
        if (ct.signalGroup() != null) gen.writeNumberField("signal_group", ct.signalGroup());
        if (ct.restrictionClassId() != null) gen.writeNumberField("restriction_class_id", ct.restrictionClassId());
        if (ct.connectionId() != null) gen.writeNumberField("connection_id", ct.connectionId());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Restriction list & data parameters                                    */
    /* --------------------------------------------------------------------- */

    private void writeRestrictionClassAssignment(JsonGenerator gen, RestrictionClassAssignment r) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", r.id());
        writeStringArray(gen, "users", r.users());
        gen.writeEndObject();
    }

    private void writeDataParameters(JsonGenerator gen, DataParameters dp) throws IOException {
        gen.writeStartObject();
        if (dp.processMethod() != null) gen.writeStringField("process_method", dp.processMethod());
        if (dp.processAgency() != null) gen.writeStringField("process_agency", dp.processAgency());
        if (dp.lastCheckedDate() != null) gen.writeStringField("last_checked_date", dp.lastCheckedDate());
        if (dp.geoidUsed() != null) gen.writeStringField("geoid_used", dp.geoidUsed());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Utility                                                               */
    /* --------------------------------------------------------------------- */

    private void writeStringArray(JsonGenerator gen, String fieldName, List<String> values) throws IOException {
        gen.writeArrayFieldStart(fieldName);
        for (String v : values) gen.writeString(v);
        gen.writeEndArray();
    }
}

