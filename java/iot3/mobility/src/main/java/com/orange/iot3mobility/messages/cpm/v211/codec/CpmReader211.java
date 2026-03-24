/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.*;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.MessageRateRange;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.SegmentationInfo;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.TrailerData;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.*;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer.PerceptionRegion;
import com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer.PerceptionRegionContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.messages.cpm.v211.validation.CpmValidationException;
import com.orange.iot3mobility.messages.cpm.v211.validation.CpmValidator211;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Streaming JSON reader for CPM 2.1.1
 */
public final class CpmReader211 {

    private final JsonFactory jsonFactory;

    public CpmReader211(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public CpmEnvelope211 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String sourceUuid = null;
            Long timestamp = null;
            String version = null;
            Integer objectIdRotationCount = null;
            CpmMessage211 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "version" -> version = parser.getValueAsString();
                    case "object_id_rotation_count" -> objectIdRotationCount = parser.getIntValue();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            CpmEnvelope211 envelope = new CpmEnvelope211(
                    messageType,
                    sourceUuid,
                    requireField(timestamp, "timestamp"),
                    requireField(version, "version"),
                    objectIdRotationCount,
                    requireField(message, "message"));

            CpmValidator211.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CpmMessage211 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        ManagementContainer managementContainer = null;
        OriginatingVehicleContainer originatingVehicleContainer = null;
        OriginatingRsuContainer originatingRsuContainer = null;
        SensorInformationContainer sensorInformationContainer = null;
        PerceptionRegionContainer perceptionRegionContainer = null;
        PerceivedObjectContainer perceivedObjectContainer = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "management_container" -> managementContainer = readManagementContainer(parser);
                case "originating_vehicle_container" -> originatingVehicleContainer = readOriginatingVehicleContainer(parser);
                case "originating_rsu_container" -> originatingRsuContainer = readOriginatingRsuContainer(parser);
                case "sensor_information_container" -> sensorInformationContainer = readSensorInformationContainer(parser);
                case "perception_region_container" -> perceptionRegionContainer = readPerceptionRegionContainer(parser);
                case "perceived_object_container" -> perceivedObjectContainer = readPerceivedObjectContainer(parser);
                default -> parser.skipChildren();
            }
        }

        return new CpmMessage211(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(managementContainer, "management_container"),
                originatingVehicleContainer,
                originatingRsuContainer,
                sensorInformationContainer,
                perceptionRegionContainer,
                perceivedObjectContainer);
    }

    /* --------------------------------------------------------------------- */
    /* Management container                                                   */
    /* --------------------------------------------------------------------- */

    private ManagementContainer readManagementContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Long referenceTime = null;
        ReferencePosition referencePosition = null;
        SegmentationInfo segmentationInfo = null;
        MessageRateRange messageRateRange = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "reference_time" -> referenceTime = parser.getLongValue();
                case "reference_position" -> referencePosition = readReferencePosition(parser);
                case "segmentation_info" -> segmentationInfo = readSegmentationInfo(parser);
                case "message_rate_range" -> messageRateRange = readMessageRateRange(parser);
                default -> parser.skipChildren();
            }
        }

        return new ManagementContainer(
                requireField(referenceTime, "reference_time"),
                requireField(referencePosition, "reference_position"),
                segmentationInfo,
                messageRateRange);
    }

    private ReferencePosition readReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer latitude = null;
        Integer longitude = null;
        PositionConfidenceEllipse ellipse = null;
        Altitude altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> latitude = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "position_confidence_ellipse" -> ellipse = readPositionConfidenceEllipse(parser);
                case "altitude" -> altitude = readAltitude(parser);
                default -> parser.skipChildren();
            }
        }

        return new ReferencePosition(
                requireField(latitude, "latitude"),
                requireField(longitude, "longitude"),
                requireField(ellipse, "position_confidence_ellipse"),
                requireField(altitude, "altitude"));
    }

    private PositionConfidenceEllipse readPositionConfidenceEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer semiMajorOrientation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major" -> semiMajor = parser.getIntValue();
                case "semi_minor" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> semiMajorOrientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PositionConfidenceEllipse(
                requireField(semiMajor, "semi_major"),
                requireField(semiMinor, "semi_minor"),
                requireField(semiMajorOrientation, "semi_major_orientation"));
    }

    private Altitude readAltitude(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Altitude(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private SegmentationInfo readSegmentationInfo(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer totalMsgNo = null;
        Integer thisMsgNo = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "total_msg_no" -> totalMsgNo = parser.getIntValue();
                case "this_msg_no" -> thisMsgNo = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new SegmentationInfo(
                requireField(totalMsgNo, "total_msg_no"),
                requireField(thisMsgNo, "this_msg_no"));
    }

    private MessageRateRange readMessageRateRange(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        MessageRateHz min = null;
        MessageRateHz max = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "message_rate_min" -> min = readMessageRateHz(parser);
                case "message_rate_max" -> max = readMessageRateHz(parser);
                default -> parser.skipChildren();
            }
        }

        return new MessageRateRange(
                requireField(min, "message_rate_min"),
                requireField(max, "message_rate_max"));
    }

    private MessageRateHz readMessageRateHz(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer mantissa = null;
        Integer exponent = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "mantissa" -> mantissa = parser.getIntValue();
                case "exponent" -> exponent = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new MessageRateHz(
                requireField(mantissa, "mantissa"),
                requireField(exponent, "exponent"));
    }

    /* --------------------------------------------------------------------- */
    /* Originating vehicle container                                          */
    /* --------------------------------------------------------------------- */

    private OriginatingVehicleContainer readOriginatingVehicleContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Angle orientationAngle = null;
        Angle pitchAngle = null;
        Angle rollAngle = null;
        List<TrailerData> trailerDataSet = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "orientation_angle" -> orientationAngle = readAngle(parser);
                case "pitch_angle" -> pitchAngle = readAngle(parser);
                case "roll_angle" -> rollAngle = readAngle(parser);
                case "trailer_data_set" -> trailerDataSet = readTrailerDataSet(parser);
                default -> parser.skipChildren();
            }
        }

        return new OriginatingVehicleContainer(
                requireField(orientationAngle, "orientation_angle"),
                pitchAngle,
                rollAngle,
                trailerDataSet);
    }

    private List<TrailerData> readTrailerDataSet(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<TrailerData> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readTrailerData(parser));
        }
        return list;
    }

    private TrailerData readTrailerData(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer refPointId = null;
        Integer hitchPointOffset = null;
        Angle hitchAngle = null;
        Integer frontOverhang = null;
        Integer rearOverhang = null;
        Integer trailerWidth = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "ref_point_id" -> refPointId = parser.getIntValue();
                case "hitch_point_offset" -> hitchPointOffset = parser.getIntValue();
                case "hitch_angle" -> hitchAngle = readAngle(parser);
                case "front_overhang" -> frontOverhang = parser.getIntValue();
                case "rear_overhang" -> rearOverhang = parser.getIntValue();
                case "trailer_width" -> trailerWidth = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new TrailerData(
                requireField(refPointId, "ref_point_id"),
                requireField(hitchPointOffset, "hitch_point_offset"),
                requireField(hitchAngle, "hitch_angle"),
                frontOverhang,
                rearOverhang,
                trailerWidth);
    }

    /* --------------------------------------------------------------------- */
    /* Originating RSU container                                              */
    /* --------------------------------------------------------------------- */

    private OriginatingRsuContainer readOriginatingRsuContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<MapReference> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readMapReference(parser));
        }
        return new OriginatingRsuContainer(list);
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
        Integer sensorType = null;
        Shape perceptionRegionShape = null;
        Integer perceptionRegionConfidence = null;
        Boolean shadowingApplies = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "sensor_id" -> sensorId = parser.getIntValue();
                case "sensor_type" -> sensorType = parser.getIntValue();
                case "perception_region_shape" -> perceptionRegionShape = readShape(parser);
                case "perception_region_confidence" -> perceptionRegionConfidence = parser.getIntValue();
                case "shadowing_applies" -> shadowingApplies = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }

        return new SensorInformation(
                requireField(sensorId, "sensor_id"),
                requireField(sensorType, "sensor_type"),
                perceptionRegionShape,
                perceptionRegionConfidence,
                requireField(shadowingApplies, "shadowing_applies"));
    }

    /* --------------------------------------------------------------------- */
    /* Perception region container                                            */
    /* --------------------------------------------------------------------- */

    private PerceptionRegionContainer readPerceptionRegionContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<PerceptionRegion> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readPerceptionRegion(parser));
        }
        return new PerceptionRegionContainer(list);
    }

    private PerceptionRegion readPerceptionRegion(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer measurementDeltaTime = null;
        Integer perceptionRegionConfidence = null;
        Shape perceptionRegionShape = null;
        Boolean shadowingApplies = null;
        List<Integer> sensorIdList = null;
        List<Integer> perceivedObjectIds = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "measurement_delta_time" -> measurementDeltaTime = parser.getIntValue();
                case "perception_region_confidence" -> perceptionRegionConfidence = parser.getIntValue();
                case "perception_region_shape" -> perceptionRegionShape = readShape(parser);
                case "shadowing_applies" -> shadowingApplies = parser.getBooleanValue();
                case "sensor_id_list" -> sensorIdList = readIntegerArray(parser);
                case "perceived_object_ids" -> perceivedObjectIds = readIntegerArray(parser);
                default -> parser.skipChildren();
            }
        }

        return new PerceptionRegion(
                requireField(measurementDeltaTime, "measurement_delta_time"),
                requireField(perceptionRegionConfidence, "perception_region_confidence"),
                requireField(perceptionRegionShape, "perception_region_shape"),
                requireField(shadowingApplies, "shadowing_applies"),
                sensorIdList,
                perceivedObjectIds);
    }

    private List<Integer> readIntegerArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Integer> values = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getIntValue());
        }
        return values;
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
        Integer measurementDeltaTime = null;
        CartesianPosition3dWithConfidence position = null;
        Integer objectId = null;
        Velocity velocity = null;
        Acceleration acceleration = null;
        EulerAngles angles = null;
        CartesianAngularVelocityComponent zAngularVelocity = null;
        List<LowerTriangularCorrelationMatrix> matrices = null;
        ObjectDimension objectDimensionZ = null;
        ObjectDimension objectDimensionY = null;
        ObjectDimension objectDimensionX = null;
        Integer objectAge = null;
        Integer objectPerceptionQuality = null;
        List<Integer> sensorIdList = null;
        List<ObjectClassification> classification = null;
        MapPosition mapPosition = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "measurement_delta_time" -> measurementDeltaTime = parser.getIntValue();
                case "position" -> position = readCartesianPosition3dWithConfidence(parser);
                case "object_id" -> objectId = parser.getIntValue();
                case "velocity" -> velocity = readVelocity(parser);
                case "acceleration" -> acceleration = readAcceleration(parser);
                case "angles" -> angles = readEulerAngles(parser);
                case "z_angular_velocity" -> zAngularVelocity = readCartesianAngularVelocityComponent(parser);
                case "lower_triangular_correlation_matrices" -> matrices = readLowerTriangularCorrelationMatrices(parser);
                case "object_dimension_z" -> objectDimensionZ = readObjectDimension(parser);
                case "object_dimension_y" -> objectDimensionY = readObjectDimension(parser);
                case "object_dimension_x" -> objectDimensionX = readObjectDimension(parser);
                case "object_age" -> objectAge = parser.getIntValue();
                case "object_perception_quality" -> objectPerceptionQuality = parser.getIntValue();
                case "sensor_id_list" -> sensorIdList = readIntegerArray(parser);
                case "classification" -> classification = readClassification(parser);
                case "map_position" -> mapPosition = readMapPosition(parser);
                default -> parser.skipChildren();
            }
        }

        return new PerceivedObject(
                requireField(measurementDeltaTime, "measurement_delta_time"),
                requireField(position, "position"),
                objectId,
                velocity,
                acceleration,
                angles,
                zAngularVelocity,
                matrices,
                objectDimensionZ,
                objectDimensionY,
                objectDimensionX,
                objectAge,
                objectPerceptionQuality,
                sensorIdList,
                classification,
                mapPosition);
    }

    private CartesianPosition3dWithConfidence readCartesianPosition3dWithConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        CartesianCoordinateWithConfidence x = null;
        CartesianCoordinateWithConfidence y = null;
        CartesianCoordinateWithConfidence z = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_coordinate" -> x = readCartesianCoordinateWithConfidence(parser);
                case "y_coordinate" -> y = readCartesianCoordinateWithConfidence(parser);
                case "z_coordinate" -> z = readCartesianCoordinateWithConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new CartesianPosition3dWithConfidence(
                requireField(x, "x_coordinate"),
                requireField(y, "y_coordinate"),
                z);
    }

    private CartesianCoordinateWithConfidence readCartesianCoordinateWithConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new CartesianCoordinateWithConfidence(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private Velocity readVelocity(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PolarVelocity polar = null;
        CartesianVelocity cartesian = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "polar_velocity" -> polar = readPolarVelocity(parser);
                case "cartesian_velocity" -> cartesian = readCartesianVelocity(parser);
                default -> parser.skipChildren();
            }
        }

        return new Velocity(polar, cartesian);
    }

    private PolarVelocity readPolarVelocity(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Speed velocityMagnitude = null;
        Angle velocityDirection = null;
        VelocityComponent zVelocity = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "velocity_magnitude" -> velocityMagnitude = readSpeed(parser);
                case "velocity_direction" -> velocityDirection = readAngle(parser);
                case "z_velocity" -> zVelocity = readVelocityComponent(parser);
                default -> parser.skipChildren();
            }
        }

        return new PolarVelocity(
                requireField(velocityMagnitude, "velocity_magnitude"),
                requireField(velocityDirection, "velocity_direction"),
                zVelocity);
    }

    private CartesianVelocity readCartesianVelocity(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        VelocityComponent x = null;
        VelocityComponent y = null;
        VelocityComponent z = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_velocity" -> x = readVelocityComponent(parser);
                case "y_velocity" -> y = readVelocityComponent(parser);
                case "z_velocity" -> z = readVelocityComponent(parser);
                default -> parser.skipChildren();
            }
        }

        return new CartesianVelocity(
                requireField(x, "x_velocity"),
                requireField(y, "y_velocity"),
                z);
    }

    private Acceleration readAcceleration(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PolarAcceleration polar = null;
        CartesianAcceleration cartesian = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "polar_acceleration" -> polar = readPolarAcceleration(parser);
                case "cartesian_acceleration" -> cartesian = readCartesianAcceleration(parser);
                default -> parser.skipChildren();
            }
        }

        return new Acceleration(polar, cartesian);
    }

    private PolarAcceleration readPolarAcceleration(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        AccelerationMagnitude magnitude = null;
        Angle direction = null;
        AccelerationComponent zAcceleration = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "acceleration_magnitude" -> magnitude = readAccelerationMagnitude(parser);
                case "acceleration_direction" -> direction = readAngle(parser);
                case "z_acceleration" -> zAcceleration = readAccelerationComponent(parser);
                default -> parser.skipChildren();
            }
        }

        return new PolarAcceleration(
                requireField(magnitude, "acceleration_magnitude"),
                requireField(direction, "acceleration_direction"),
                zAcceleration);
    }

    private AccelerationMagnitude readAccelerationMagnitude(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AccelerationMagnitude(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private CartesianAcceleration readCartesianAcceleration(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        AccelerationComponent x = null;
        AccelerationComponent y = null;
        AccelerationComponent z = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_acceleration" -> x = readAccelerationComponent(parser);
                case "y_acceleration" -> y = readAccelerationComponent(parser);
                case "z_acceleration" -> z = readAccelerationComponent(parser);
                default -> parser.skipChildren();
            }
        }

        return new CartesianAcceleration(
                requireField(x, "x_acceleration"),
                requireField(y, "y_acceleration"),
                z);
    }

    private AccelerationComponent readAccelerationComponent(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AccelerationComponent(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private EulerAngles readEulerAngles(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Angle zAngle = null;
        Angle yAngle = null;
        Angle xAngle = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "z_angle" -> zAngle = readAngle(parser);
                case "y_angle" -> yAngle = readAngle(parser);
                case "x_angle" -> xAngle = readAngle(parser);
                default -> parser.skipChildren();
            }
        }

        return new EulerAngles(
                requireField(zAngle, "z_angle"),
                yAngle,
                xAngle);
    }

    private CartesianAngularVelocityComponent readCartesianAngularVelocityComponent(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new CartesianAngularVelocityComponent(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private List<LowerTriangularCorrelationMatrix> readLowerTriangularCorrelationMatrices(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<LowerTriangularCorrelationMatrix> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readLowerTriangularCorrelationMatrix(parser));
        }
        return list;
    }

    private LowerTriangularCorrelationMatrix readLowerTriangularCorrelationMatrix(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        LowerTriangularMatrixComponents components = null;
        List<List<List<Integer>>> matrix = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "components_included_in_the_matrix" -> components = readLowerTriangularMatrixComponents(parser);
                case "matrix" -> matrix = readMatrix(parser);
                default -> parser.skipChildren();
            }
        }

        return new LowerTriangularCorrelationMatrix(
                requireField(components, "components_included_in_the_matrix"),
                requireField(matrix, "matrix"));
    }

    private LowerTriangularMatrixComponents readLowerTriangularMatrixComponents(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean xPosition = null;
        Boolean yPosition = null;
        Boolean zPosition = null;
        Boolean xVelocityOrVelocityMagnitude = null;
        Boolean yVelocityOrVelocityDirection = null;
        Boolean zSpeed = null;
        Boolean xAccelOrAccelMagnitude = null;
        Boolean yAccelOrAccelDirection = null;
        Boolean zAcceleration = null;
        Boolean zAngle = null;
        Boolean yAngle = null;
        Boolean xAngle = null;
        Boolean zAngularVelocity = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_position" -> xPosition = parser.getBooleanValue();
                case "y_position" -> yPosition = parser.getBooleanValue();
                case "z_position" -> zPosition = parser.getBooleanValue();
                case "x_velocity_or_velocity_magnitude" -> xVelocityOrVelocityMagnitude = parser.getBooleanValue();
                case "y_velocity_or_velocity_direction" -> yVelocityOrVelocityDirection = parser.getBooleanValue();
                case "z_speed" -> zSpeed = parser.getBooleanValue();
                case "x_accel_or_accel_magnitude" -> xAccelOrAccelMagnitude = parser.getBooleanValue();
                case "y_accel_or_accel_direction" -> yAccelOrAccelDirection = parser.getBooleanValue();
                case "z_acceleration" -> zAcceleration = parser.getBooleanValue();
                case "z_angle" -> zAngle = parser.getBooleanValue();
                case "y_angle" -> yAngle = parser.getBooleanValue();
                case "x_angle" -> xAngle = parser.getBooleanValue();
                case "z_angular_velocity" -> zAngularVelocity = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }

        return new LowerTriangularMatrixComponents(
                requireField(xPosition, "x_position"),
                requireField(yPosition, "y_position"),
                requireField(zPosition, "z_position"),
                requireField(xVelocityOrVelocityMagnitude, "x_velocity_or_velocity_magnitude"),
                requireField(yVelocityOrVelocityDirection, "y_velocity_or_velocity_direction"),
                requireField(zSpeed, "z_speed"),
                requireField(xAccelOrAccelMagnitude, "x_accel_or_accel_magnitude"),
                requireField(yAccelOrAccelDirection, "y_accel_or_accel_direction"),
                requireField(zAcceleration, "z_acceleration"),
                requireField(zAngle, "z_angle"),
                requireField(yAngle, "y_angle"),
                requireField(xAngle, "x_angle"),
                requireField(zAngularVelocity, "z_angular_velocity"));
    }

    private List<List<List<Integer>>> readMatrix(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<List<List<Integer>>> matrix = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            matrix.add(readMatrixColumn(parser));
        }
        return matrix;
    }

    private List<List<Integer>> readMatrixColumn(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<List<Integer>> column = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            column.add(readMatrixRow(parser));
        }
        return column;
    }

    private List<Integer> readMatrixRow(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Integer> row = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            row.add(parser.getIntValue());
        }
        return row;
    }

    private ObjectDimension readObjectDimension(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectDimension(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private List<ObjectClassification> readClassification(JsonParser parser) throws IOException {
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
        ObjectClassVru vru = null;
        ObjectClassGroup group = null;
        Integer other = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle" -> vehicle = parser.getIntValue();
                case "vru" -> vru = readObjectClassVru(parser);
                case "group" -> group = readObjectClassGroup(parser);
                case "other" -> other = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClass(vehicle, vru, group, other);
    }

    private ObjectClassVru readObjectClassVru(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer pedestrian = null;
        Integer bicyclistAndLightVruVehicle = null;
        Integer motorcylist = null;
        Integer animal = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "pedestrian" -> pedestrian = parser.getIntValue();
                case "bicyclist_and_light_vru_vehicle" -> bicyclistAndLightVruVehicle = parser.getIntValue();
                case "motorcylist" -> motorcylist = parser.getIntValue();
                case "animal" -> animal = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ObjectClassVru(pedestrian, bicyclistAndLightVruVehicle, motorcylist, animal);
    }

    private ObjectClassGroup readObjectClassGroup(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Shape clusterBoundingBoxShape = null;
        Integer clusterCardinalitySize = null;
        Integer clusterId = null;
        ClusterProfiles clusterProfiles = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "cluster_bounding_box_shape" -> clusterBoundingBoxShape = readShape(parser);
                case "cluster_cardinality_size" -> clusterCardinalitySize = parser.getIntValue();
                case "cluster_id" -> clusterId = parser.getIntValue();
                case "cluster_profiles" -> clusterProfiles = readClusterProfiles(parser);
                default -> parser.skipChildren();
            }
        }

        return new ObjectClassGroup(
                requireField(clusterBoundingBoxShape, "cluster_bounding_box_shape"),
                requireField(clusterCardinalitySize, "cluster_cardinality_size"),
                clusterId,
                clusterProfiles);
    }

    private ClusterProfiles readClusterProfiles(JsonParser parser) throws IOException {
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

        return new ClusterProfiles(
                requireField(pedestrian, "pedestrian"),
                requireField(bicyclist, "bicyclist"),
                requireField(motorcyclist, "motorcyclist"),
                requireField(animal, "animal"));
    }

    private MapPosition readMapPosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        MapReference mapReference = null;
        Integer laneId = null;
        Integer connectionId = null;
        LongitudinalLanePosition longitudinalLanePosition = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "map_reference" -> mapReference = readMapReference(parser);
                case "lane_id" -> laneId = parser.getIntValue();
                case "connection_id" -> connectionId = parser.getIntValue();
                case "longitudinal_lane_position" -> longitudinalLanePosition = readLongitudinalLanePosition(parser);
                default -> parser.skipChildren();
            }
        }

        return new MapPosition(mapReference, laneId, connectionId, longitudinalLanePosition);
    }

    private LongitudinalLanePosition readLongitudinalLanePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new LongitudinalLanePosition(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    /* --------------------------------------------------------------------- */
    /* CDD helpers                                                            */
    /* --------------------------------------------------------------------- */

    private Angle readAngle(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Angle(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private Speed readSpeed(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Speed(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private VelocityComponent readVelocityComponent(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new VelocityComponent(
                requireField(value, "value"),
                requireField(confidence, "confidence"));
    }

    private MapReference readMapReference(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        RoadSegment roadSegment = null;
        Intersection intersection = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "road_segment" -> roadSegment = readRoadSegment(parser);
                case "intersection" -> intersection = readIntersection(parser);
                default -> parser.skipChildren();
            }
        }

        return new MapReference(roadSegment, intersection);
    }

    private RoadSegment readRoadSegment(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer id = null;
        Integer region = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "id" -> id = parser.getIntValue();
                case "region" -> region = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new RoadSegment(
                requireField(id, "id"),
                region);
    }

    private Intersection readIntersection(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer id = null;
        Integer region = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "id" -> id = parser.getIntValue();
                case "region" -> region = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Intersection(
                requireField(id, "id"),
                region);
    }

    private Shape readShape(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Rectangular rectangular = null;
        Circular circular = null;
        Polygonal polygonal = null;
        Elliptical elliptical = null;
        Radial radial = null;
        RadialShapes radialShapes = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "rectangular" -> rectangular = readRectangular(parser);
                case "circular" -> circular = readCircular(parser);
                case "polygonal" -> polygonal = readPolygonal(parser);
                case "elliptical" -> elliptical = readElliptical(parser);
                case "radial" -> radial = readRadial(parser);
                case "radial_shapes" -> radialShapes = readRadialShapes(parser);
                default -> parser.skipChildren();
            }
        }

        return new Shape(rectangular, circular, polygonal, elliptical, radial, radialShapes);
    }

    private Rectangular readRectangular(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        CartesianPosition3d centerPoint = null;
        Integer semiLength = null;
        Integer semiBreadth = null;
        Integer orientation = null;
        Integer height = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "center_point" -> centerPoint = readCartesianPosition3d(parser);
                case "semi_length" -> semiLength = parser.getIntValue();
                case "semi_breadth" -> semiBreadth = parser.getIntValue();
                case "orientation" -> orientation = parser.getIntValue();
                case "height" -> height = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Rectangular(
                centerPoint,
                requireField(semiLength, "semi_length"),
                requireField(semiBreadth, "semi_breadth"),
                orientation,
                height);
    }

    private Circular readCircular(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer radius = null;
        CartesianPosition3d shapeReferencePoint = null;
        Integer height = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "radius" -> radius = parser.getIntValue();
                case "shape_reference_point" -> shapeReferencePoint = readCartesianPosition3d(parser);
                case "height" -> height = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Circular(
                requireField(radius, "radius"),
                shapeReferencePoint,
                height);
    }

    private Polygonal readPolygonal(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<CartesianPosition3d> polygon = null;
        CartesianPosition3d shapeReferencePoint = null;
        Integer height = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "polygon" -> polygon = readCartesianPosition3dArray(parser);
                case "shape_reference_point" -> shapeReferencePoint = readCartesianPosition3d(parser);
                case "height" -> height = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Polygonal(
                requireField(polygon, "polygon"),
                shapeReferencePoint,
                height);
    }

    private Elliptical readElliptical(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajorAxisLength = null;
        Integer semiMinorAxisLength = null;
        CartesianPosition3d shapeReferencePoint = null;
        Integer orientation = null;
        Integer height = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major_axis_length" -> semiMajorAxisLength = parser.getIntValue();
                case "semi_minor_axis_length" -> semiMinorAxisLength = parser.getIntValue();
                case "shape_reference_point" -> shapeReferencePoint = readCartesianPosition3d(parser);
                case "orientation" -> orientation = parser.getIntValue();
                case "height" -> height = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Elliptical(
                requireField(semiMajorAxisLength, "semi_major_axis_length"),
                requireField(semiMinorAxisLength, "semi_minor_axis_length"),
                shapeReferencePoint,
                orientation,
                height);
    }

    private Radial readRadial(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer range = null;
        Integer stationaryHorizontalOpeningAngleStart = null;
        Integer stationaryHorizontalOpeningAngleEnd = null;
        CartesianPosition3d shapeReferencePoint = null;
        Integer verticalOpeningAngleStart = null;
        Integer verticalOpeningAngleEnd = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "range" -> range = parser.getIntValue();
                case "stationary_horizontal_opening_angle_start" -> stationaryHorizontalOpeningAngleStart = parser.getIntValue();
                case "stationary_horizontal_opening_angle_end" -> stationaryHorizontalOpeningAngleEnd = parser.getIntValue();
                case "shape_reference_point" -> shapeReferencePoint = readCartesianPosition3d(parser);
                case "vertical_opening_angle_start" -> verticalOpeningAngleStart = parser.getIntValue();
                case "vertical_opening_angle_end" -> verticalOpeningAngleEnd = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Radial(
                requireField(range, "range"),
                requireField(stationaryHorizontalOpeningAngleStart, "stationary_horizontal_opening_angle_start"),
                requireField(stationaryHorizontalOpeningAngleEnd, "stationary_horizontal_opening_angle_end"),
                shapeReferencePoint,
                verticalOpeningAngleStart,
                verticalOpeningAngleEnd);
    }

    private RadialShapes readRadialShapes(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer refPointId = null;
        Integer xCoordinate = null;
        Integer yCoordinate = null;
        Integer zCoordinate = null;
        List<Radial> radialShapesList = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "ref_point_id" -> refPointId = parser.getIntValue();
                case "x_coordinate" -> xCoordinate = parser.getIntValue();
                case "y_coordinate" -> yCoordinate = parser.getIntValue();
                case "z_coordinate" -> zCoordinate = parser.getIntValue();
                case "radial_shapes_list" -> radialShapesList = readRadialList(parser);
                default -> parser.skipChildren();
            }
        }

        return new RadialShapes(
                requireField(refPointId, "ref_point_id"),
                requireField(xCoordinate, "x_coordinate"),
                requireField(yCoordinate, "y_coordinate"),
                zCoordinate,
                requireField(radialShapesList, "radial_shapes_list"));
    }

    private List<Radial> readRadialList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Radial> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readRadial(parser));
        }
        return list;
    }

    private List<CartesianPosition3d> readCartesianPosition3dArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<CartesianPosition3d> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readCartesianPosition3d(parser));
        }
        return list;
    }

    private CartesianPosition3d readCartesianPosition3d(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer xCoordinate = null;
        Integer yCoordinate = null;
        Integer zCoordinate = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "x_coordinate" -> xCoordinate = parser.getIntValue();
                case "y_coordinate" -> yCoordinate = parser.getIntValue();
                case "z_coordinate" -> zCoordinate = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new CartesianPosition3d(
                requireField(xCoordinate, "x_coordinate"),
                requireField(yCoordinate, "y_coordinate"),
                zCoordinate);
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected token " + expected + " but got " + actual);
        }
    }
}

