/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
import com.orange.iot3mobility.messages.mapem.v200.validation.MapemValidationException;
import com.orange.iot3mobility.messages.mapem.v200.validation.MapemValidator200;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Streaming JSON reader for MAPEM v2.0.0 payloads.
 */
public final class MapemReader200 {

    private final JsonFactory jsonFactory;

    public MapemReader200(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public MapemEnvelope200 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String origin = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            MapemMessage200 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "origin"       -> origin = parser.getValueAsString();
                    case "version"      -> version = parser.getValueAsString();
                    case "source_uuid"  -> sourceUuid = parser.getValueAsString();
                    case "timestamp"    -> timestamp = parser.getLongValue();
                    case "message"      -> message = readMessage(parser);
                    default             -> parser.skipChildren();
                }
            }

            MapemEnvelope200 envelope = new MapemEnvelope200(
                    requireField(messageType, "message_type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(message, "message"));

            MapemValidator200.validateEnvelope(envelope);
            return envelope;
        }
    }

    /* --------------------------------------------------------------------- */
    /* Message                                                               */
    /* --------------------------------------------------------------------- */

    private MapemMessage200 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        Integer timestamp = null;
        Integer msgIssueRevision = null;
        String layerType = null;
        Integer layerId = null;
        List<IntersectionGeometry> intersections = null;
        List<RoadSegmentData> roadSegments = null;
        DataParameters dataParameters = null;
        List<RestrictionClassAssignment> restrictionList = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version"   -> protocolVersion = parser.getIntValue();
                case "station_id"         -> stationId = parser.getLongValue();
                case "timestamp"          -> timestamp = parser.getIntValue();
                case "msg_issue_revision" -> msgIssueRevision = parser.getIntValue();
                case "layer_type"         -> layerType = parser.getValueAsString();
                case "layer_id"           -> layerId = parser.getIntValue();
                case "intersections"      -> intersections = readIntersections(parser);
                case "road_segments"      -> roadSegments = readRoadSegments(parser);
                case "data_parameters"    -> dataParameters = readDataParameters(parser);
                case "restriction_list"   -> restrictionList = readRestrictionList(parser);
                default                   -> parser.skipChildren();
            }
        }

        return new MapemMessage200(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                timestamp,
                requireField(msgIssueRevision, "msg_issue_revision"),
                layerType,
                layerId,
                intersections,
                roadSegments,
                dataParameters,
                restrictionList);
    }

    /* --------------------------------------------------------------------- */
    /* Intersections                                                         */
    /* --------------------------------------------------------------------- */

    private List<IntersectionGeometry> readIntersections(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<IntersectionGeometry> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readIntersectionGeometry(parser));
        }
        return list;
    }

    private IntersectionGeometry readIntersectionGeometry(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        String name = null;
        IntersectionReferenceId id = null;
        Integer revision = null;
        Position3D refPoint = null;
        Integer laneWidth = null;
        List<SpeedLimit> speedLimits = null;
        List<GenericLane> laneSet = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "name"         -> name = parser.getValueAsString();
                case "id"           -> id = readIntersectionReferenceId(parser);
                case "revision"     -> revision = parser.getIntValue();
                case "ref_point"    -> refPoint = readPosition3D(parser);
                case "lane_width"   -> laneWidth = parser.getIntValue();
                case "speed_limits" -> speedLimits = readSpeedLimits(parser);
                case "lane_set"     -> laneSet = readLaneSet(parser);
                default             -> parser.skipChildren();
            }
        }

        return new IntersectionGeometry(
                name,
                requireField(id, "id"),
                requireField(revision, "revision"),
                requireField(refPoint, "ref_point"),
                laneWidth,
                speedLimits,
                requireField(laneSet, "lane_set"));
    }

    private IntersectionReferenceId readIntersectionReferenceId(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer region = null;
        Integer id = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "region" -> region = parser.getIntValue();
                case "id"     -> id = parser.getIntValue();
                default       -> parser.skipChildren();
            }
        }
        return new IntersectionReferenceId(region, requireField(id, "intersection_reference_id.id"));
    }

    /* --------------------------------------------------------------------- */
    /* Road segments                                                         */
    /* --------------------------------------------------------------------- */

    private List<RoadSegmentData> readRoadSegments(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<RoadSegmentData> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readRoadSegmentData(parser));
        }
        return list;
    }

    private RoadSegmentData readRoadSegmentData(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        String name = null;
        RoadSegmentReferenceId id = null;
        Integer revision = null;
        Position3D refPoint = null;
        Integer laneWidth = null;
        List<SpeedLimit> speedLimits = null;
        List<GenericLane> roadLaneSet = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "name"           -> name = parser.getValueAsString();
                case "id"             -> id = readRoadSegmentReferenceId(parser);
                case "revision"       -> revision = parser.getIntValue();
                case "ref_point"      -> refPoint = readPosition3D(parser);
                case "lane_width"     -> laneWidth = parser.getIntValue();
                case "speed_limits"   -> speedLimits = readSpeedLimits(parser);
                case "road_lane_set"  -> roadLaneSet = readLaneSet(parser);
                default               -> parser.skipChildren();
            }
        }

        return new RoadSegmentData(
                name,
                requireField(id, "id"),
                requireField(revision, "revision"),
                requireField(refPoint, "ref_point"),
                laneWidth,
                speedLimits,
                requireField(roadLaneSet, "road_lane_set"));
    }

    private RoadSegmentReferenceId readRoadSegmentReferenceId(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer region = null;
        Integer id = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "region" -> region = parser.getIntValue();
                case "id"     -> id = parser.getIntValue();
                default       -> parser.skipChildren();
            }
        }
        return new RoadSegmentReferenceId(region, requireField(id, "road_segment_reference_id.id"));
    }

    /* --------------------------------------------------------------------- */
    /* Shared: Position3D, SpeedLimits                                      */
    /* --------------------------------------------------------------------- */

    private Position3D readPosition3D(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null;
        Integer lon = null;
        Integer elevation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude"  -> lat = parser.getIntValue();
                case "longitude" -> lon = parser.getIntValue();
                case "elevation" -> elevation = parser.getIntValue();
                default          -> parser.skipChildren();
            }
        }
        return new Position3D(
                requireField(lat, "position_3d.latitude"),
                requireField(lon, "position_3d.longitude"),
                elevation);
    }

    private List<SpeedLimit> readSpeedLimits(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<SpeedLimit> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readSpeedLimit(parser));
        }
        return list;
    }

    private SpeedLimit readSpeedLimit(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        String type = null;
        Integer speed = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "type"  -> type = parser.getValueAsString();
                case "speed" -> speed = parser.getIntValue();
                default      -> parser.skipChildren();
            }
        }
        return new SpeedLimit(
                requireField(type, "speed_limit.type"),
                requireField(speed, "speed_limit.speed"));
    }

    /* --------------------------------------------------------------------- */
    /* Lanes                                                                 */
    /* --------------------------------------------------------------------- */

    private List<GenericLane> readLaneSet(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<GenericLane> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readGenericLane(parser));
        }
        return list;
    }

    private GenericLane readGenericLane(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer laneId = null;
        String name = null;
        Integer ingressApproach = null;
        Integer egressApproach = null;
        LaneAttributes laneAttributes = null;
        List<String> maneuvers = null;
        NodeList nodeList = null;
        List<ConnectsTo> connectsTo = null;
        List<SpeedLimit> speedLimits = null;
        List<Integer> overlays = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "lane_id"           -> laneId = parser.getIntValue();
                case "name"              -> name = parser.getValueAsString();
                case "ingress_approach"  -> ingressApproach = parser.getIntValue();
                case "egress_approach"   -> egressApproach = parser.getIntValue();
                case "lane_attributes"   -> laneAttributes = readLaneAttributes(parser);
                case "maneuvers"         -> maneuvers = readStringArray(parser);
                case "node_list"         -> nodeList = readNodeList(parser);
                case "connects_to"       -> connectsTo = readConnectsTo(parser);
                case "speed_limits"      -> speedLimits = readSpeedLimits(parser);
                case "overlays"          -> overlays = readIntArray(parser);
                default                  -> parser.skipChildren();
            }
        }

        return new GenericLane(
                requireField(laneId, "lane_id"),
                name,
                ingressApproach,
                egressApproach,
                requireField(laneAttributes, "lane_attributes"),
                maneuvers,
                requireField(nodeList, "node_list"),
                connectsTo,
                speedLimits,
                overlays);
    }

    private LaneAttributes readLaneAttributes(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<String> directionalUse = null;
        List<String> sharedWith = null;
        LaneType laneType = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "directional_use" -> directionalUse = readStringArray(parser);
                case "shared_with"     -> sharedWith = readStringArray(parser);
                case "lane_type"       -> laneType = readLaneType(parser);
                default                -> parser.skipChildren();
            }
        }

        return new LaneAttributes(
                requireField(directionalUse, "directional_use"),
                requireField(sharedWith, "shared_with"),
                requireField(laneType, "lane_type"));
    }

    private LaneType readLaneType(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<String> vehicle = null;
        List<String> crosswalk = null;
        List<String> bikeLane = null;
        List<String> sidewalk = null;
        List<String> median = null;
        List<String> striping = null;
        List<String> trackedVehicle = null;
        List<String> parking = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle"         -> vehicle = readStringArray(parser);
                case "crosswalk"       -> crosswalk = readStringArray(parser);
                case "bike_lane"       -> bikeLane = readStringArray(parser);
                case "sidewalk"        -> sidewalk = readStringArray(parser);
                case "median"          -> median = readStringArray(parser);
                case "striping"        -> striping = readStringArray(parser);
                case "tracked_vehicle" -> trackedVehicle = readStringArray(parser);
                case "parking"         -> parking = readStringArray(parser);
                default                -> parser.skipChildren();
            }
        }
        return new LaneType(vehicle, crosswalk, bikeLane, sidewalk, median, striping, trackedVehicle, parking);
    }

    private NodeList readNodeList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<NodeXY> nodes = null;
        ComputedLane computed = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "nodes"    -> nodes = readNodes(parser);
                case "computed" -> computed = readComputedLane(parser);
                default         -> parser.skipChildren();
            }
        }
        return new NodeList(nodes, computed);
    }

    private List<NodeXY> readNodes(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<NodeXY> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readNodeXY(parser));
        }
        return list;
    }

    private NodeXY readNodeXY(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        NodeDelta delta = null;
        NodeAttributes attributes = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "delta"      -> delta = readNodeDelta(parser);
                case "attributes" -> attributes = readNodeAttributes(parser);
                default           -> parser.skipChildren();
            }
        }
        return new NodeXY(requireField(delta, "delta"), attributes);
    }

    private NodeDelta readNodeDelta(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        NodeXYOffset nodeXy = null;
        NodeLatLon nodeLatLon = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "node_xy"      -> nodeXy = readNodeXYOffset(parser);
                case "node_lat_lon" -> nodeLatLon = readNodeLatLon(parser);
                default             -> parser.skipChildren();
            }
        }
        return new NodeDelta(nodeXy, nodeLatLon);
    }

    private NodeXYOffset readNodeXYOffset(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer x = null;
        Integer y = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x" -> x = parser.getIntValue();
                case "y" -> y = parser.getIntValue();
                default  -> parser.skipChildren();
            }
        }
        return new NodeXYOffset(requireField(x, "node_xy.x"), requireField(y, "node_xy.y"));
    }

    private NodeLatLon readNodeLatLon(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null;
        Integer lon = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "lat" -> lat = parser.getIntValue();
                case "lon" -> lon = parser.getIntValue();
                default    -> parser.skipChildren();
            }
        }
        return new NodeLatLon(requireField(lat, "node_lat_lon.lat"), requireField(lon, "node_lat_lon.lon"));
    }

    private NodeAttributes readNodeAttributes(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<String> localNode = null;
        List<String> disabled = null;
        List<String> enabled = null;
        List<NodeAttributeData> data = null;
        Integer dWidth = null;
        Integer dElevation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "local_node"   -> localNode = readStringArray(parser);
                case "disabled"     -> disabled = readStringArray(parser);
                case "enabled"      -> enabled = readStringArray(parser);
                case "data"         -> data = readNodeAttributeDataList(parser);
                case "d_width"      -> dWidth = parser.getIntValue();
                case "d_elevation"  -> dElevation = parser.getIntValue();
                default             -> parser.skipChildren();
            }
        }
        return new NodeAttributes(localNode, disabled, enabled, data, dWidth, dElevation);
    }

    private List<NodeAttributeData> readNodeAttributeDataList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<NodeAttributeData> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readNodeAttributeData(parser));
        }
        return list;
    }

    private NodeAttributeData readNodeAttributeData(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer pathEndPointAngle = null;
        Integer laneCrownPointCenter = null;
        Integer laneCrownPointLeft = null;
        Integer laneCrownPointRight = null;
        Integer laneAngle = null;
        List<SpeedLimit> speedLimits = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "path_end_point_angle"     -> pathEndPointAngle = parser.getIntValue();
                case "lane_crown_point_center"  -> laneCrownPointCenter = parser.getIntValue();
                case "lane_crown_point_left"    -> laneCrownPointLeft = parser.getIntValue();
                case "lane_crown_point_right"   -> laneCrownPointRight = parser.getIntValue();
                case "lane_angle"               -> laneAngle = parser.getIntValue();
                case "speed_limits"             -> speedLimits = readSpeedLimits(parser);
                default                         -> parser.skipChildren();
            }
        }
        return new NodeAttributeData(pathEndPointAngle, laneCrownPointCenter, laneCrownPointLeft,
                laneCrownPointRight, laneAngle, speedLimits);
    }

    private ComputedLane readComputedLane(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer referenceLaneId = null;
        Integer offsetXAxis = null;
        Integer offsetYAxis = null;
        Integer rotateXy = null;
        Integer scaleXAxis = null;
        Integer scaleYAxis = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "reference_lane_id" -> referenceLaneId = parser.getIntValue();
                case "offset_x_axis"     -> offsetXAxis = parser.getIntValue();
                case "offset_y_axis"     -> offsetYAxis = parser.getIntValue();
                case "rotate_xy"         -> rotateXy = parser.getIntValue();
                case "scale_x_axis"      -> scaleXAxis = parser.getIntValue();
                case "scale_y_axis"      -> scaleYAxis = parser.getIntValue();
                default                  -> parser.skipChildren();
            }
        }
        return new ComputedLane(
                requireField(referenceLaneId, "reference_lane_id"),
                requireField(offsetXAxis, "offset_x_axis"),
                requireField(offsetYAxis, "offset_y_axis"),
                rotateXy, scaleXAxis, scaleYAxis);
    }

    private List<ConnectsTo> readConnectsTo(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ConnectsTo> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readConnection(parser));
        }
        return list;
    }

    private ConnectsTo readConnection(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        ConnectingLane connectingLane = null;
        IntersectionReferenceId remoteIntersections = null;
        Integer signalGroup = null;
        Integer restrictionClassId = null;
        Integer connectionId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "connecting_lane"      -> connectingLane = readConnectingLane(parser);
                case "remote_intersections" -> remoteIntersections = readIntersectionReferenceId(parser);
                case "signal_group"         -> signalGroup = parser.getIntValue();
                case "restriction_class_id" -> restrictionClassId = parser.getIntValue();
                case "connection_id"        -> connectionId = parser.getIntValue();
                default                     -> parser.skipChildren();
            }
        }
        return new ConnectsTo(
                requireField(connectingLane, "connecting_lane"),
                remoteIntersections, signalGroup, restrictionClassId, connectionId);
    }

    private ConnectingLane readConnectingLane(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lane = null;
        List<String> maneuver = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "lane"     -> lane = parser.getIntValue();
                case "maneuver" -> maneuver = readStringArray(parser);
                default         -> parser.skipChildren();
            }
        }
        return new ConnectingLane(requireField(lane, "connecting_lane.lane"), maneuver);
    }

    /* --------------------------------------------------------------------- */
    /* Restriction list & data parameters                                    */
    /* --------------------------------------------------------------------- */

    private List<RestrictionClassAssignment> readRestrictionList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<RestrictionClassAssignment> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readRestrictionClassAssignment(parser));
        }
        return list;
    }

    private RestrictionClassAssignment readRestrictionClassAssignment(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer id = null;
        List<String> users = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "id"    -> id = parser.getIntValue();
                case "users" -> users = readStringArray(parser);
                default      -> parser.skipChildren();
            }
        }
        return new RestrictionClassAssignment(
                requireField(id, "restriction_class_assignment.id"),
                requireField(users, "restriction_class_assignment.users"));
    }

    private DataParameters readDataParameters(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        String processMethod = null;
        String processAgency = null;
        String lastCheckedDate = null;
        String geoidUsed = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "process_method"   -> processMethod = parser.getValueAsString();
                case "process_agency"   -> processAgency = parser.getValueAsString();
                case "last_checked_date" -> lastCheckedDate = parser.getValueAsString();
                case "geoid_used"       -> geoidUsed = parser.getValueAsString();
                default                 -> parser.skipChildren();
            }
        }
        return new DataParameters(processMethod, processAgency, lastCheckedDate, geoidUsed);
    }

    /* --------------------------------------------------------------------- */
    /* Utility helpers                                                       */
    /* --------------------------------------------------------------------- */

    private List<String> readStringArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<String> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getValueAsString());
        }
        return list;
    }

    private List<Integer> readIntArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Integer> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getIntValue());
        }
        return list;
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new MapemValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected token " + expected + " but got " + actual);
        }
    }
}

