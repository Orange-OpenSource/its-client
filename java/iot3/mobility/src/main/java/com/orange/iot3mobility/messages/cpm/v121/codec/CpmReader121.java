/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.*;
import com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer.FreeSpaceAddendum;
import com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer.FreeSpaceAddendumContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer.FreeSpaceArea;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.*;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.*;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.VehicleConfidence;
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidationException;
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidator121;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class CpmReader121 {

    private final JsonFactory jsonFactory;

    public CpmReader121(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public CpmEnvelope121 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String type = null;
            String origin = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            CpmMessage121 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "type" -> type = parser.getValueAsString();
                    case "origin" -> origin = parser.getValueAsString();
                    case "version" -> version = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            CpmEnvelope121 envelope = new CpmEnvelope121(
                    requireField(type, "type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(message, "message"));

            CpmValidator121.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CpmMessage121 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        Integer generationDeltaTime = null;
        ManagementContainer managementContainer = null;
        StationDataContainer stationDataContainer = null;
        SensorInformationContainer sensorInformationContainer = null;
        PerceivedObjectContainer perceivedObjectContainer = null;
        FreeSpaceAddendumContainer freeSpaceAddendumContainer = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "generation_delta_time" -> generationDeltaTime = parser.getIntValue();
                case "management_container" -> managementContainer = readManagementContainer(parser);
                case "station_data_container" -> stationDataContainer = readStationDataContainer(parser);
                case "sensor_information_container" -> sensorInformationContainer = readSensorInformationContainer(parser);
                case "perceived_object_container" -> perceivedObjectContainer = readPerceivedObjectContainer(parser);
                case "free_space_addendum_container" -> freeSpaceAddendumContainer = readFreeSpaceAddendumContainer(parser);
                default -> parser.skipChildren();
            }
        }

        return new CpmMessage121(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(generationDeltaTime, "generation_delta_time"),
                requireField(managementContainer, "management_container"),
                stationDataContainer,
                sensorInformationContainer,
                perceivedObjectContainer,
                freeSpaceAddendumContainer);
    }

    /* --------------------------------------------------------------------- */
    /* Management container                                                   */
    /* --------------------------------------------------------------------- */

    private ManagementContainer readManagementContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer stationType = null;
        ReferencePosition referencePosition = null;
        ManagementConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "station_type" -> stationType = parser.getIntValue();
                case "reference_position" -> referencePosition = readReferencePosition(parser);
                case "confidence" -> confidence = readManagementConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new ManagementContainer(
                requireField(stationType, "station_type"),
                requireField(referencePosition, "reference_position"),
                requireField(confidence, "confidence"));
    }

    private ReferencePosition readReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer latitude = null;
        Integer longitude = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> latitude = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ReferencePosition(
                requireField(latitude, "latitude"),
                requireField(longitude, "longitude"),
                requireField(altitude, "altitude"));
    }

    private ManagementConfidence readManagementConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PositionConfidenceEllipse ellipse = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "position_confidence_ellipse" -> ellipse = readPositionConfidenceEllipse(parser);
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ManagementConfidence(
                requireField(ellipse, "position_confidence_ellipse"),
                requireField(altitude, "altitude"));
    }

    private PositionConfidenceEllipse readPositionConfidenceEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer orientation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major_confidence" -> semiMajor = parser.getIntValue();
                case "semi_minor_confidence" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> orientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PositionConfidenceEllipse(
                requireField(semiMajor, "semi_major_confidence"),
                requireField(semiMinor, "semi_minor_confidence"),
                requireField(orientation, "semi_major_orientation"));
    }

    /* --------------------------------------------------------------------- */
    /* Station data container                                                 */
    /* --------------------------------------------------------------------- */

    private StationDataContainer readStationDataContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        OriginatingVehicleContainer vehicleContainer = null;
        OriginatingRsuContainer rsuContainer = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "originating_vehicle_container" -> vehicleContainer = readOriginatingVehicleContainer(parser);
                case "originating_rsu_container" -> rsuContainer = readOriginatingRsuContainer(parser);
                default -> parser.skipChildren();
            }
        }

        return new StationDataContainer(vehicleContainer, rsuContainer);
    }

    private OriginatingVehicleContainer readOriginatingVehicleContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer heading = null;
        Integer speed = null;
        VehicleConfidence confidence = null;
        Integer driveDirection = null;
        Integer vehicleLength = null;
        Integer vehicleWidth = null;
        Integer longitudinalAcceleration = null;
        Integer yawRate = null;
        Integer lateralAcceleration = null;
        Integer verticalAcceleration = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "heading" -> heading = parser.getIntValue();
                case "speed" -> speed = parser.getIntValue();
                case "confidence" -> confidence = readVehicleConfidence(parser);
                case "drive_direction" -> driveDirection = parser.getIntValue();
                case "vehicle_length" -> vehicleLength = parser.getIntValue();
                case "vehicle_width" -> vehicleWidth = parser.getIntValue();
                case "longitudinal_acceleration" -> longitudinalAcceleration = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "lateral_acceleration" -> lateralAcceleration = parser.getIntValue();
                case "vertical_acceleration" -> verticalAcceleration = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new OriginatingVehicleContainer(
                requireField(heading, "heading"),
                requireField(speed, "speed"),
                requireField(confidence, "confidence"),
                driveDirection,
                vehicleLength,
                vehicleWidth,
                longitudinalAcceleration,
                yawRate,
                lateralAcceleration,
                verticalAcceleration);
    }

    private VehicleConfidence readVehicleConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer heading = null;
        Integer speed = null;
        Integer vehicleLength = null;
        Integer yawRate = null;
        Integer longitudinalAcceleration = null;
        Integer lateralAcceleration = null;
        Integer verticalAcceleration = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "heading" -> heading = parser.getIntValue();
                case "speed" -> speed = parser.getIntValue();
                case "vehicle_length" -> vehicleLength = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "longitudinal_acceleration" -> longitudinalAcceleration = parser.getIntValue();
                case "lateral_acceleration" -> lateralAcceleration = parser.getIntValue();
                case "vertical_acceleration" -> verticalAcceleration = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new VehicleConfidence(
                requireField(heading, "heading"),
                requireField(speed, "speed"),
                vehicleLength,
                yawRate,
                longitudinalAcceleration,
                lateralAcceleration,
                verticalAcceleration);
    }

    private OriginatingRsuContainer readOriginatingRsuContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer region = null;
        Integer intersectionReferenceId = null;
        Integer roadSegmentReferenceId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "region" -> region = parser.getIntValue();
                case "intersection_reference_id" -> intersectionReferenceId = parser.getIntValue();
                case "road_segment_reference_id" -> roadSegmentReferenceId = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new OriginatingRsuContainer(region, intersectionReferenceId, roadSegmentReferenceId);
    }

    /* --------------------------------------------------------------------- */
    /* Sensor information container                                           */
    /* --------------------------------------------------------------------- */

    private SensorInformationContainer readSensorInformationContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<SensorInformation> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readSensorInformation(parser));
        }
        return new SensorInformationContainer(list);
    }

    private SensorInformation readSensorInformation(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer sensorId = null;
        Integer type = null;
        DetectionArea detectionArea = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "sensor_id" -> sensorId = parser.getIntValue();
                case "type" -> type = parser.getIntValue();
                case "detection_area" -> detectionArea = readDetectionArea(parser);
                default -> parser.skipChildren();
            }
        }

        return new SensorInformation(
                requireField(sensorId, "sensor_id"),
                requireField(type, "type"),
                requireField(detectionArea, "detection_area"));
    }

    private DetectionArea readDetectionArea(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        VehicleSensor vehicleSensor = null;
        StationarySensorRadial stationarySensorRadial = null;
        AreaPolygon stationarySensorPolygon = null;
        AreaCircular stationarySensorCircular = null;
        AreaEllipse stationarySensorEllipse = null;
        AreaRectangle stationarySensorRectangle = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_sensor" -> vehicleSensor = readVehicleSensor(parser);
                case "stationary_sensor_radial" -> stationarySensorRadial = readStationarySensorRadial(parser);
                case "stationary_sensor_polygon" -> stationarySensorPolygon = readAreaPolygon(parser);
                case "stationary_sensor_circular" -> stationarySensorCircular = readAreaCircular(parser);
                case "stationary_sensor_ellipse" -> stationarySensorEllipse = readAreaEllipse(parser);
                case "stationary_sensor_rectangle" -> stationarySensorRectangle = readAreaRectangle(parser);
                default -> parser.skipChildren();
            }
        }

        return new DetectionArea(
                vehicleSensor,
                stationarySensorRadial,
                stationarySensorPolygon,
                stationarySensorCircular,
                stationarySensorEllipse,
                stationarySensorRectangle);
    }

    private VehicleSensor readVehicleSensor(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer refPointId = null;
        Integer xSensorOffset = null;
        Integer ySensorOffset = null;
        Integer zSensorOffset = null;
        List<VehicleSensorProperty> properties = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "ref_point_id" -> refPointId = parser.getIntValue();
                case "x_sensor_offset" -> xSensorOffset = parser.getIntValue();
                case "y_sensor_offset" -> ySensorOffset = parser.getIntValue();
                case "z_sensor_offset" -> zSensorOffset = parser.getIntValue();
                case "vehicle_sensor_property_list" -> properties = readVehicleSensorPropertyList(parser);
                default -> parser.skipChildren();
            }
        }

        return new VehicleSensor(
                refPointId,
                requireField(xSensorOffset, "x_sensor_offset"),
                requireField(ySensorOffset, "y_sensor_offset"),
                zSensorOffset,
                requireField(properties, "vehicle_sensor_property_list"));
    }

    private List<VehicleSensorProperty> readVehicleSensorPropertyList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<VehicleSensorProperty> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readVehicleSensorProperty(parser));
        }
        return list;
    }

    private VehicleSensorProperty readVehicleSensorProperty(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer range = null;
        Integer horizontalStart = null;
        Integer horizontalEnd = null;
        Integer verticalStart = null;
        Integer verticalEnd = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "range" -> range = parser.getIntValue();
                case "horizontal_opening_angle_start" -> horizontalStart = parser.getIntValue();
                case "horizontal_opening_angle_end" -> horizontalEnd = parser.getIntValue();
                case "vertical_opening_angle_start" -> verticalStart = parser.getIntValue();
                case "vertical_opening_angle_end" -> verticalEnd = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new VehicleSensorProperty(
                requireField(range, "range"),
                requireField(horizontalStart, "horizontal_opening_angle_start"),
                requireField(horizontalEnd, "horizontal_opening_angle_end"),
                verticalStart,
                verticalEnd);
    }

    private StationarySensorRadial readStationarySensorRadial(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer range = null;
        Integer horizontalStart = null;
        Integer horizontalEnd = null;
        Integer verticalStart = null;
        Integer verticalEnd = null;
        Offset offset = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "range" -> range = parser.getIntValue();
                case "horizontal_opening_angle_start" -> horizontalStart = parser.getIntValue();
                case "horizontal_opening_angle_end" -> horizontalEnd = parser.getIntValue();
                case "vertical_opening_angle_start" -> verticalStart = parser.getIntValue();
                case "vertical_opening_angle_end" -> verticalEnd = parser.getIntValue();
                case "sensor_position_offset" -> offset = readOffset(parser);
                default -> parser.skipChildren();
            }
        }

        return new StationarySensorRadial(
                requireField(range, "range"),
                requireField(horizontalStart, "horizontal_opening_angle_start"),
                requireField(horizontalEnd, "horizontal_opening_angle_end"),
                verticalStart,
                verticalEnd,
                offset);
    }

    /* --------------------------------------------------------------------- */
    /* Perceived object container                                             */
    /* --------------------------------------------------------------------- */

    private PerceivedObjectContainer readPerceivedObjectContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<PerceivedObject> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readPerceivedObject(parser));
        }
        return new PerceivedObjectContainer(list);
    }

    private PerceivedObject readPerceivedObject(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer objectId = null;
        Integer timeOfMeasurement = null;
        Integer xDistance = null;
        Integer yDistance = null;
        Integer zDistance = null;
        Integer xSpeed = null;
        Integer ySpeed = null;
        Integer zSpeed = null;
        Integer xAcceleration = null;
        Integer yAcceleration = null;
        Integer zAcceleration = null;
        Integer rollAngle = null;
        Integer pitchAngle = null;
        Integer yawAngle = null;
        Integer rollRate = null;
        Integer pitchRate = null;
        Integer yawRate = null;
        Integer rollAcceleration = null;
        Integer pitchAcceleration = null;
        Integer yawAcceleration = null;
        LowerTriangularCorrelationMatrix correlationMatrix = null;
        Integer planarObjectDimension1 = null;
        Integer planarObjectDimension2 = null;
        Integer verticalObjectDimension = null;
        Integer objectRefPoint = null;
        Integer objectAge = null;
        List<Integer> sensorIdList = null;
        Integer dynamicStatus = null;
        List<ObjectClassification> classification = null;
        MapPosition matchedPosition = null;
        PerceivedObjectConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "object_id" -> objectId = parser.getIntValue();
                case "time_of_measurement" -> timeOfMeasurement = parser.getIntValue();
                case "x_distance" -> xDistance = parser.getIntValue();
                case "y_distance" -> yDistance = parser.getIntValue();
                case "z_distance" -> zDistance = parser.getIntValue();
                case "x_speed" -> xSpeed = parser.getIntValue();
                case "y_speed" -> ySpeed = parser.getIntValue();
                case "z_speed" -> zSpeed = parser.getIntValue();
                case "x_acceleration" -> xAcceleration = parser.getIntValue();
                case "y_acceleration" -> yAcceleration = parser.getIntValue();
                case "z_acceleration" -> zAcceleration = parser.getIntValue();
                case "roll_angle" -> rollAngle = parser.getIntValue();
                case "pitch_angle" -> pitchAngle = parser.getIntValue();
                case "yaw_angle" -> yawAngle = parser.getIntValue();
                case "roll_rate" -> rollRate = parser.getIntValue();
                case "pitch_rate" -> pitchRate = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "roll_acceleration" -> rollAcceleration = parser.getIntValue();
                case "pitch_acceleration" -> pitchAcceleration = parser.getIntValue();
                case "yaw_acceleration" -> yawAcceleration = parser.getIntValue();
                case "lower_triangular_correlation_matrix_columns" -> correlationMatrix = readLowerTriangularCorrelationMatrix(parser);
                case "planar_object_dimension_1" -> planarObjectDimension1 = parser.getIntValue();
                case "planar_object_dimension_2" -> planarObjectDimension2 = parser.getIntValue();
                case "vertical_object_dimension" -> verticalObjectDimension = parser.getIntValue();
                case "object_ref_point" -> objectRefPoint = parser.getIntValue();
                case "object_age" -> objectAge = parser.getIntValue();
                case "sensor_id_list" -> sensorIdList = readIntegerList(parser);
                case "dynamic_status" -> dynamicStatus = parser.getIntValue();
                case "classification" -> classification = readObjectClassificationList(parser);
                case "matched_position" -> matchedPosition = readMapPosition(parser);
                case "confidence" -> confidence = readPerceivedObjectConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new PerceivedObject(
                requireField(objectId, "object_id"),
                requireField(timeOfMeasurement, "time_of_measurement"),
                requireField(xDistance, "x_distance"),
                requireField(yDistance, "y_distance"),
                requireField(xSpeed, "x_speed"),
                requireField(ySpeed, "y_speed"),
                requireField(objectAge, "object_age"),
                requireField(confidence, "confidence"),
                zDistance,
                zSpeed,
                xAcceleration,
                yAcceleration,
                zAcceleration,
                rollAngle,
                pitchAngle,
                yawAngle,
                rollRate,
                pitchRate,
                yawRate,
                rollAcceleration,
                pitchAcceleration,
                yawAcceleration,
                correlationMatrix,
                planarObjectDimension1,
                planarObjectDimension2,
                verticalObjectDimension,
                objectRefPoint,
                sensorIdList,
                dynamicStatus,
                classification,
                matchedPosition);
    }

    private PerceivedObjectConfidence readPerceivedObjectConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer xDistance = null;
        Integer yDistance = null;
        Integer zDistance = null;
        Integer xSpeed = null;
        Integer ySpeed = null;
        Integer zSpeed = null;
        Integer xAcceleration = null;
        Integer yAcceleration = null;
        Integer zAcceleration = null;
        Integer rollAngle = null;
        Integer pitchAngle = null;
        Integer yawAngle = null;
        Integer rollRate = null;
        Integer pitchRate = null;
        Integer yawRate = null;
        Integer rollAcceleration = null;
        Integer pitchAcceleration = null;
        Integer yawAcceleration = null;
        Integer planarObjectDimension1 = null;
        Integer planarObjectDimension2 = null;
        Integer verticalObjectDimension = null;
        Integer longitudinalLanePosition = null;
        Integer object = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_distance" -> xDistance = parser.getIntValue();
                case "y_distance" -> yDistance = parser.getIntValue();
                case "z_distance" -> zDistance = parser.getIntValue();
                case "x_speed" -> xSpeed = parser.getIntValue();
                case "y_speed" -> ySpeed = parser.getIntValue();
                case "z_speed" -> zSpeed = parser.getIntValue();
                case "x_acceleration" -> xAcceleration = parser.getIntValue();
                case "y_acceleration" -> yAcceleration = parser.getIntValue();
                case "z_acceleration" -> zAcceleration = parser.getIntValue();
                case "roll_angle" -> rollAngle = parser.getIntValue();
                case "pitch_angle" -> pitchAngle = parser.getIntValue();
                case "yaw_angle" -> yawAngle = parser.getIntValue();
                case "roll_rate" -> rollRate = parser.getIntValue();
                case "pitch_rate" -> pitchRate = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "roll_acceleration" -> rollAcceleration = parser.getIntValue();
                case "pitch_acceleration" -> pitchAcceleration = parser.getIntValue();
                case "yaw_acceleration" -> yawAcceleration = parser.getIntValue();
                case "planar_object_dimension_1" -> planarObjectDimension1 = parser.getIntValue();
                case "planar_object_dimension_2" -> planarObjectDimension2 = parser.getIntValue();
                case "vertical_object_dimension" -> verticalObjectDimension = parser.getIntValue();
                case "longitudinal_lane_position" -> longitudinalLanePosition = parser.getIntValue();
                case "object" -> object = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PerceivedObjectConfidence(
                requireField(xDistance, "x_distance"),
                requireField(yDistance, "y_distance"),
                requireField(xSpeed, "x_speed"),
                requireField(ySpeed, "y_speed"),
                requireField(object, "object"),
                zDistance,
                zSpeed,
                xAcceleration,
                yAcceleration,
                zAcceleration,
                rollAngle,
                pitchAngle,
                yawAngle,
                rollRate,
                pitchRate,
                yawRate,
                rollAcceleration,
                pitchAcceleration,
                yawAcceleration,
                planarObjectDimension1,
                planarObjectDimension2,
                verticalObjectDimension,
                longitudinalLanePosition);
    }

    private LowerTriangularCorrelationMatrix readLowerTriangularCorrelationMatrix(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<List<Integer>> columns = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            columns.add(readIntegerList(parser));
        }
        return new LowerTriangularCorrelationMatrix(columns);
    }

    private List<ObjectClassification> readObjectClassificationList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ObjectClassification> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readObjectClassification(parser));
        }
        return list;
    }

    private ObjectClassification readObjectClassification(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        ObjectClass objectClass = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "object_class" -> objectClass = readObjectClass(parser);
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClassification(
                requireField(objectClass, "object_class"),
                requireField(confidence, "confidence"));
    }

    private ObjectClass readObjectClass(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer vehicle = null;
        ObjectClassVru singleVru = null;
        ObjectClassGroup vruGroup = null;
        Integer other = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle" -> vehicle = parser.getIntValue();
                case "single_vru" -> singleVru = readObjectClassVru(parser);
                case "vru_group" -> vruGroup = readObjectClassGroup(parser);
                case "other" -> other = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClass(vehicle, singleVru, vruGroup, other);
    }

    private ObjectClassVru readObjectClassVru(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer pedestrian = null;
        Integer bicyclist = null;
        Integer motorcylist = null;
        Integer animal = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "pedestrian" -> pedestrian = parser.getIntValue();
                case "bicyclist" -> bicyclist = parser.getIntValue();
                case "motorcylist" -> motorcylist = parser.getIntValue();
                case "animal" -> animal = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClassVru(pedestrian, bicyclist, motorcylist, animal);
    }

    private ObjectClassGroup readObjectClassGroup(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        GroupType groupType = null;
        Integer groupSize = null;
        Integer clusterId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "group_type" -> groupType = readGroupType(parser);
                case "group_size" -> groupSize = parser.getIntValue();
                case "cluster_id" -> clusterId = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClassGroup(
                requireField(groupType, "group_type"),
                requireField(groupSize, "group_size"),
                clusterId);
    }

    private GroupType readGroupType(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean pedestrian = null;
        Boolean bicyclist = null;
        Boolean motorcyclist = null;
        Boolean animal = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "pedestrian" -> pedestrian = parser.getBooleanValue();
                case "bicyclist" -> bicyclist = parser.getBooleanValue();
                case "motorcyclist" -> motorcyclist = parser.getBooleanValue();
                case "animal" -> animal = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }

        return new GroupType(
                requireField(pedestrian, "pedestrian"),
                requireField(bicyclist, "bicyclist"),
                requireField(motorcyclist, "motorcyclist"),
                requireField(animal, "animal"));
    }

    private MapPosition readMapPosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer laneId = null;
        Integer longitudinalLanePosition = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "lane_id" -> laneId = parser.getIntValue();
                case "longitudinal_lane_position" -> longitudinalLanePosition = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new MapPosition(laneId, longitudinalLanePosition);
    }

    /* --------------------------------------------------------------------- */
    /* Free space addendum container                                          */
    /* --------------------------------------------------------------------- */

    private FreeSpaceAddendumContainer readFreeSpaceAddendumContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<FreeSpaceAddendum> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readFreeSpaceAddendum(parser));
        }
        return new FreeSpaceAddendumContainer(list);
    }

    private FreeSpaceAddendum readFreeSpaceAddendum(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        FreeSpaceArea area = null;
        Integer confidence = null;
        List<Integer> sensorIdList = null;
        Boolean shadowingApplies = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "free_space_area" -> area = readFreeSpaceArea(parser);
                case "free_space_confidence" -> confidence = parser.getIntValue();
                case "sensor_id_list" -> sensorIdList = readIntegerList(parser);
                case "shadowing_applies" -> shadowingApplies = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }

        return new FreeSpaceAddendum(
                requireField(area, "free_space_area"),
                requireField(confidence, "free_space_confidence"),
                sensorIdList,
                shadowingApplies);
    }

    private FreeSpaceArea readFreeSpaceArea(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        AreaPolygon polygon = null;
        AreaCircular circular = null;
        AreaEllipse ellipse = null;
        AreaRectangle rectangle = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "free_space_polygon" -> polygon = readAreaPolygon(parser);
                case "free_space_circular" -> circular = readAreaCircular(parser);
                case "free_space_ellipse" -> ellipse = readAreaEllipse(parser);
                case "free_space_rectangle" -> rectangle = readAreaRectangle(parser);
                default -> parser.skipChildren();
            }
        }

        return new FreeSpaceArea(polygon, circular, ellipse, rectangle);
    }

    /* --------------------------------------------------------------------- */
    /* Shared helpers                                                         */
    /* --------------------------------------------------------------------- */

    private Offset readOffset(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer x = null;
        Integer y = null;
        Integer z = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x" -> x = parser.getIntValue();
                case "y" -> y = parser.getIntValue();
                case "z" -> z = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Offset(
                requireField(x, "x"),
                requireField(y, "y"),
                z);
    }

    private AreaPolygon readAreaPolygon(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Offset> offsets = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            offsets.add(readOffset(parser));
        }
        return new AreaPolygon(offsets);
    }

    private AreaCircular readAreaCircular(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Offset center = null;
        Integer radius = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "node_center_point" -> center = readOffset(parser);
                case "radius" -> radius = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AreaCircular(center, requireField(radius, "radius"));
    }

    private AreaEllipse readAreaEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Offset center = null;
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer orientation = null;
        Integer semiHeight = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "node_center_point" -> center = readOffset(parser);
                case "semi_major_range_length" -> semiMajor = parser.getIntValue();
                case "semi_minor_range_length" -> semiMinor = parser.getIntValue();
                case "semi_major_range_orientation" -> orientation = parser.getIntValue();
                case "semi_height" -> semiHeight = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AreaEllipse(
                center,
                requireField(semiMajor, "semi_major_range_length"),
                requireField(semiMinor, "semi_minor_range_length"),
                requireField(orientation, "semi_major_range_orientation"),
                semiHeight);
    }

    private AreaRectangle readAreaRectangle(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Offset center = null;
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer orientation = null;
        Integer semiHeight = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "node_center_point" -> center = readOffset(parser);
                case "semi_major_range_length" -> semiMajor = parser.getIntValue();
                case "semi_minor_range_length" -> semiMinor = parser.getIntValue();
                case "semi_major_range_orientation" -> orientation = parser.getIntValue();
                case "semi_height" -> semiHeight = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AreaRectangle(
                center,
                requireField(semiMajor, "semi_major_range_length"),
                requireField(semiMinor, "semi_minor_range_length"),
                requireField(orientation, "semi_major_range_orientation"),
                semiHeight);
    }

    private List<Integer> readIntegerList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Integer> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getIntValue());
        }
        return list;
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected " + expected + " but got " + actual);
        }
    }
}
