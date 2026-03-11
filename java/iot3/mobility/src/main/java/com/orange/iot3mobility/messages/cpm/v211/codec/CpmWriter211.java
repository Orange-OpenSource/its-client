/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.*;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.MessageRateRange;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.SegmentationInfo;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.TrailerData;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.*;
import com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer.PerceptionRegion;
import com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer.PerceptionRegionContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.messages.cpm.v211.validation.CpmValidator211;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer.OriginatingRsuContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Streaming JSON writer for CPM 2.1.1
 */
public final class CpmWriter211 {

    private final JsonFactory jsonFactory;

    public CpmWriter211(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public void write(CpmEnvelope211 envelope, OutputStream out) throws IOException {
        CpmValidator211.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeStringField("version", envelope.version());
            if (envelope.objectIdRotationCount() != null) {
                gen.writeNumberField("object_id_rotation_count", envelope.objectIdRotationCount());
            }
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, CpmMessage211 message) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", message.protocolVersion());
        gen.writeNumberField("station_id", message.stationId());

        gen.writeFieldName("management_container");
        writeManagementContainer(gen, message.managementContainer());

        if (message.originatingVehicleContainer() != null) {
            gen.writeFieldName("originating_vehicle_container");
            writeOriginatingVehicleContainer(gen, message.originatingVehicleContainer());
        }
        if (message.originatingRsuContainer() != null) {
            OriginatingRsuContainer rsuContainer = message.originatingRsuContainer();
            gen.writeArrayFieldStart("originating_rsu_container");
            for (MapReference reference : rsuContainer.mapReferences()) {
                writeMapReference(gen, reference);
            }
            gen.writeEndArray();
        }
        if (message.sensorInformationContainer() != null) {
            SensorInformationContainer sensorContainer = message.sensorInformationContainer();
            gen.writeArrayFieldStart("sensor_information_container");
            for (SensorInformation info : sensorContainer.sensorInformation()) {
                writeSensorInformation(gen, info);
            }
            gen.writeEndArray();
        }
        if (message.perceptionRegionContainer() != null) {
            PerceptionRegionContainer regionContainer = message.perceptionRegionContainer();
            gen.writeArrayFieldStart("perception_region_container");
            for (PerceptionRegion region : regionContainer.perceptionRegions()) {
                writePerceptionRegion(gen, region);
            }
            gen.writeEndArray();
        }
        if (message.perceivedObjectContainer() != null) {
            PerceivedObjectContainer objectContainer = message.perceivedObjectContainer();
            gen.writeArrayFieldStart("perceived_object_container");
            for (PerceivedObject obj : objectContainer.perceivedObjects()) {
                writePerceivedObject(gen, obj);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Management container                                                   */
    /* --------------------------------------------------------------------- */

    private void writeManagementContainer(JsonGenerator gen, ManagementContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("reference_time", container.referenceTime());
        gen.writeFieldName("reference_position");
        writeReferencePosition(gen, container.referencePosition());
        if (container.segmentationInfo() != null) {
            gen.writeFieldName("segmentation_info");
            writeSegmentationInfo(gen, container.segmentationInfo());
        }
        if (container.messageRateRange() != null) {
            gen.writeFieldName("message_rate_range");
            writeMessageRateRange(gen, container.messageRateRange());
        }
        gen.writeEndObject();
    }

    private void writeReferencePosition(JsonGenerator gen, ReferencePosition reference) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", reference.latitude());
        gen.writeNumberField("longitude", reference.longitude());
        gen.writeFieldName("position_confidence_ellipse");
        writePositionConfidenceEllipse(gen, reference.positionConfidenceEllipse());
        gen.writeFieldName("altitude");
        writeAltitude(gen, reference.altitude());
        gen.writeEndObject();
    }

    private void writePositionConfidenceEllipse(JsonGenerator gen, PositionConfidenceEllipse ellipse) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("semi_major", ellipse.semiMajor());
        gen.writeNumberField("semi_minor", ellipse.semiMinor());
        gen.writeNumberField("semi_major_orientation", ellipse.semiMajorOrientation());
        gen.writeEndObject();
    }

    private void writeAltitude(JsonGenerator gen, Altitude altitude) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", altitude.value());
        gen.writeNumberField("confidence", altitude.confidence());
        gen.writeEndObject();
    }

    private void writeSegmentationInfo(JsonGenerator gen, SegmentationInfo info) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("total_msg_no", info.totalMsgNo());
        gen.writeNumberField("this_msg_no", info.thisMsgNo());
        gen.writeEndObject();
    }

    private void writeMessageRateRange(JsonGenerator gen, MessageRateRange range) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("message_rate_min");
        writeMessageRateHz(gen, range.messageRateMin());
        gen.writeFieldName("message_rate_max");
        writeMessageRateHz(gen, range.messageRateMax());
        gen.writeEndObject();
    }

    private void writeMessageRateHz(JsonGenerator gen, MessageRateHz rate) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("mantissa", rate.mantissa());
        gen.writeNumberField("exponent", rate.exponent());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Originating vehicle container                                          */
    /* --------------------------------------------------------------------- */

    private void writeOriginatingVehicleContainer(JsonGenerator gen, OriginatingVehicleContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("orientation_angle");
        writeAngle(gen, container.orientationAngle());
        if (container.pitchAngle() != null) {
            gen.writeFieldName("pitch_angle");
            writeAngle(gen, container.pitchAngle());
        }
        if (container.rollAngle() != null) {
            gen.writeFieldName("roll_angle");
            writeAngle(gen, container.rollAngle());
        }
        if (container.trailerDataSet() != null) {
            gen.writeArrayFieldStart("trailer_data_set");
            for (TrailerData trailer : container.trailerDataSet()) {
                writeTrailerData(gen, trailer);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    private void writeTrailerData(JsonGenerator gen, TrailerData trailer) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("ref_point_id", trailer.refPointId());
        gen.writeNumberField("hitch_point_offset", trailer.hitchPointOffset());
        gen.writeFieldName("hitch_angle");
        writeAngle(gen, trailer.hitchAngle());
        if (trailer.frontOverhang() != null) {
            gen.writeNumberField("front_overhang", trailer.frontOverhang());
        }
        if (trailer.rearOverhang() != null) {
            gen.writeNumberField("rear_overhang", trailer.rearOverhang());
        }
        if (trailer.trailerWidth() != null) {
            gen.writeNumberField("trailer_width", trailer.trailerWidth());
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Sensor information container                                           */
    /* --------------------------------------------------------------------- */

    private void writeSensorInformation(JsonGenerator gen, SensorInformation info) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("sensor_id", info.sensorId());
        gen.writeNumberField("sensor_type", info.sensorType());
        if (info.perceptionRegionShape() != null) {
            gen.writeFieldName("perception_region_shape");
            writeShape(gen, info.perceptionRegionShape());
        }
        if (info.perceptionRegionConfidence() != null) {
            gen.writeNumberField("perception_region_confidence", info.perceptionRegionConfidence());
        }
        gen.writeBooleanField("shadowing_applies", info.shadowingApplies());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Perception region container                                            */
    /* --------------------------------------------------------------------- */

    private void writePerceptionRegion(JsonGenerator gen, PerceptionRegion region) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("measurement_delta_time", region.measurementDeltaTime());
        gen.writeNumberField("perception_region_confidence", region.perceptionRegionConfidence());
        gen.writeFieldName("perception_region_shape");
        writeShape(gen, region.perceptionRegionShape());
        gen.writeBooleanField("shadowing_applies", region.shadowingApplies());
        if (region.sensorIdList() != null) {
            gen.writeArrayFieldStart("sensor_id_list");
            for (Integer id : region.sensorIdList()) {
                gen.writeNumber(id);
            }
            gen.writeEndArray();
        }
        if (region.perceivedObjectIds() != null) {
            gen.writeArrayFieldStart("perceived_object_ids");
            for (Integer id : region.perceivedObjectIds()) {
                gen.writeNumber(id);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Perceived object container                                             */
    /* --------------------------------------------------------------------- */

    private void writePerceivedObject(JsonGenerator gen, PerceivedObject object) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("measurement_delta_time", object.measurementDeltaTime());
        gen.writeFieldName("position");
        writeCartesianPosition3dWithConfidence(gen, object.position());
        if (object.objectId() != null) {
            gen.writeNumberField("object_id", object.objectId());
        }
        if (object.velocity() != null) {
            gen.writeFieldName("velocity");
            writeVelocity(gen, object.velocity());
        }
        if (object.acceleration() != null) {
            gen.writeFieldName("acceleration");
            writeAcceleration(gen, object.acceleration());
        }
        if (object.angles() != null) {
            gen.writeFieldName("angles");
            writeEulerAngles(gen, object.angles());
        }
        if (object.zAngularVelocity() != null) {
            gen.writeFieldName("z_angular_velocity");
            writeCartesianAngularVelocityComponent(gen, object.zAngularVelocity());
        }
        if (object.lowerTriangularCorrelationMatrices() != null) {
            gen.writeArrayFieldStart("lower_triangular_correlation_matrices");
            for (LowerTriangularCorrelationMatrix matrix : object.lowerTriangularCorrelationMatrices()) {
                writeLowerTriangularCorrelationMatrix(gen, matrix);
            }
            gen.writeEndArray();
        }
        if (object.objectDimensionZ() != null) {
            gen.writeFieldName("object_dimension_z");
            writeObjectDimension(gen, object.objectDimensionZ());
        }
        if (object.objectDimensionY() != null) {
            gen.writeFieldName("object_dimension_y");
            writeObjectDimension(gen, object.objectDimensionY());
        }
        if (object.objectDimensionX() != null) {
            gen.writeFieldName("object_dimension_x");
            writeObjectDimension(gen, object.objectDimensionX());
        }
        if (object.objectAge() != null) {
            gen.writeNumberField("object_age", object.objectAge());
        }
        if (object.objectPerceptionQuality() != null) {
            gen.writeNumberField("object_perception_quality", object.objectPerceptionQuality());
        }
        if (object.sensorIdList() != null) {
            gen.writeArrayFieldStart("sensor_id_list");
            for (Integer id : object.sensorIdList()) {
                gen.writeNumber(id);
            }
            gen.writeEndArray();
        }
        if (object.classification() != null) {
            gen.writeArrayFieldStart("classification");
            for (ObjectClassification classification : object.classification()) {
                writeObjectClassification(gen, classification);
            }
            gen.writeEndArray();
        }
        if (object.mapPosition() != null) {
            gen.writeFieldName("map_position");
            writeMapPosition(gen, object.mapPosition());
        }
        gen.writeEndObject();
    }

    private void writeCartesianPosition3dWithConfidence(JsonGenerator gen, CartesianPosition3dWithConfidence position) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("x_coordinate");
        writeCartesianCoordinateWithConfidence(gen, position.xCoordinate());
        gen.writeFieldName("y_coordinate");
        writeCartesianCoordinateWithConfidence(gen, position.yCoordinate());
        if (position.zCoordinate() != null) {
            gen.writeFieldName("z_coordinate");
            writeCartesianCoordinateWithConfidence(gen, position.zCoordinate());
        }
        gen.writeEndObject();
    }

    private void writeCartesianCoordinateWithConfidence(JsonGenerator gen, CartesianCoordinateWithConfidence coordinate) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", coordinate.value());
        gen.writeNumberField("confidence", coordinate.confidence());
        gen.writeEndObject();
    }

    private void writeVelocity(JsonGenerator gen, Velocity velocity) throws IOException {
        gen.writeStartObject();
        if (velocity.polarVelocity() != null) {
            gen.writeFieldName("polar_velocity");
            writePolarVelocity(gen, velocity.polarVelocity());
        } else if (velocity.cartesianVelocity() != null) {
            gen.writeFieldName("cartesian_velocity");
            writeCartesianVelocity(gen, velocity.cartesianVelocity());
        }
        gen.writeEndObject();
    }

    private void writePolarVelocity(JsonGenerator gen, PolarVelocity velocity) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("velocity_magnitude");
        writeSpeed(gen, velocity.velocityMagnitude());
        gen.writeFieldName("velocity_direction");
        writeAngle(gen, velocity.velocityDirection());
        if (velocity.zVelocity() != null) {
            gen.writeFieldName("z_velocity");
            writeVelocityComponent(gen, velocity.zVelocity());
        }
        gen.writeEndObject();
    }

    private void writeCartesianVelocity(JsonGenerator gen, CartesianVelocity velocity) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("x_velocity");
        writeVelocityComponent(gen, velocity.xVelocity());
        gen.writeFieldName("y_velocity");
        writeVelocityComponent(gen, velocity.yVelocity());
        if (velocity.zVelocity() != null) {
            gen.writeFieldName("z_velocity");
            writeVelocityComponent(gen, velocity.zVelocity());
        }
        gen.writeEndObject();
    }

    private void writeAcceleration(JsonGenerator gen, Acceleration acceleration) throws IOException {
        gen.writeStartObject();
        if (acceleration.polarAcceleration() != null) {
            gen.writeFieldName("polar_acceleration");
            writePolarAcceleration(gen, acceleration.polarAcceleration());
        } else if (acceleration.cartesianAcceleration() != null) {
            gen.writeFieldName("cartesian_acceleration");
            writeCartesianAcceleration(gen, acceleration.cartesianAcceleration());
        }
        gen.writeEndObject();
    }

    private void writePolarAcceleration(JsonGenerator gen, PolarAcceleration acceleration) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("acceleration_magnitude");
        writeAccelerationMagnitude(gen, acceleration.accelerationMagnitude());
        gen.writeFieldName("acceleration_direction");
        writeAngle(gen, acceleration.accelerationDirection());
        if (acceleration.zAcceleration() != null) {
            gen.writeFieldName("z_acceleration");
            writeAccelerationComponent(gen, acceleration.zAcceleration());
        }
        gen.writeEndObject();
    }

    private void writeAccelerationMagnitude(JsonGenerator gen, AccelerationMagnitude magnitude) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", magnitude.value());
        gen.writeNumberField("confidence", magnitude.confidence());
        gen.writeEndObject();
    }

    private void writeCartesianAcceleration(JsonGenerator gen, CartesianAcceleration acceleration) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("x_acceleration");
        writeAccelerationComponent(gen, acceleration.xAcceleration());
        gen.writeFieldName("y_acceleration");
        writeAccelerationComponent(gen, acceleration.yAcceleration());
        if (acceleration.zAcceleration() != null) {
            gen.writeFieldName("z_acceleration");
            writeAccelerationComponent(gen, acceleration.zAcceleration());
        }
        gen.writeEndObject();
    }

    private void writeAccelerationComponent(JsonGenerator gen, AccelerationComponent component) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", component.value());
        gen.writeNumberField("confidence", component.confidence());
        gen.writeEndObject();
    }

    private void writeEulerAngles(JsonGenerator gen, EulerAngles angles) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("z_angle");
        writeAngle(gen, angles.zAngle());
        if (angles.yAngle() != null) {
            gen.writeFieldName("y_angle");
            writeAngle(gen, angles.yAngle());
        }
        if (angles.xAngle() != null) {
            gen.writeFieldName("x_angle");
            writeAngle(gen, angles.xAngle());
        }
        gen.writeEndObject();
    }

    private void writeCartesianAngularVelocityComponent(JsonGenerator gen, CartesianAngularVelocityComponent component) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", component.value());
        gen.writeNumberField("confidence", component.confidence());
        gen.writeEndObject();
    }

    private void writeLowerTriangularCorrelationMatrix(JsonGenerator gen, LowerTriangularCorrelationMatrix matrix) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("components_included_in_the_matrix");
        writeLowerTriangularMatrixComponents(gen, matrix.componentsIncludedInTheMatrix());
        gen.writeFieldName("matrix");
        writeMatrix(gen, matrix.matrix());
        gen.writeEndObject();
    }

    private void writeLowerTriangularMatrixComponents(JsonGenerator gen, LowerTriangularMatrixComponents components) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("x_position", components.xPosition());
        gen.writeBooleanField("y_position", components.yPosition());
        gen.writeBooleanField("z_position", components.zPosition());
        gen.writeBooleanField("x_velocity_or_velocity_magnitude", components.xVelocityOrVelocityMagnitude());
        gen.writeBooleanField("y_velocity_or_velocity_direction", components.yVelocityOrVelocityDirection());
        gen.writeBooleanField("z_speed", components.zSpeed());
        gen.writeBooleanField("x_accel_or_accel_magnitude", components.xAccelOrAccelMagnitude());
        gen.writeBooleanField("y_accel_or_accel_direction", components.yAccelOrAccelDirection());
        gen.writeBooleanField("z_acceleration", components.zAcceleration());
        gen.writeBooleanField("z_angle", components.zAngle());
        gen.writeBooleanField("y_angle", components.yAngle());
        gen.writeBooleanField("x_angle", components.xAngle());
        gen.writeBooleanField("z_angular_velocity", components.zAngularVelocity());
        gen.writeEndObject();
    }

    private void writeMatrix(JsonGenerator gen, List<List<List<Integer>>> matrix) throws IOException {
        gen.writeStartArray();
        for (List<List<Integer>> column : matrix) {
            gen.writeStartArray();
            for (List<Integer> row : column) {
                gen.writeStartArray();
                for (Integer value : row) {
                    gen.writeNumber(value);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

    private void writeObjectDimension(JsonGenerator gen, ObjectDimension dimension) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", dimension.value());
        gen.writeNumberField("confidence", dimension.confidence());
        gen.writeEndObject();
    }

    private void writeObjectClassification(JsonGenerator gen, ObjectClassification classification) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("object_class");
        writeObjectClass(gen, classification.objectClass());
        gen.writeNumberField("confidence", classification.confidence());
        gen.writeEndObject();
    }

    private void writeObjectClass(JsonGenerator gen, ObjectClass objectClass) throws IOException {
        gen.writeStartObject();
        if (objectClass.vehicle() != null) {
            gen.writeNumberField("vehicle", objectClass.vehicle());
        } else if (objectClass.vru() != null) {
            gen.writeFieldName("vru");
            writeObjectClassVru(gen, objectClass.vru());
        } else if (objectClass.group() != null) {
            gen.writeFieldName("group");
            writeObjectClassGroup(gen, objectClass.group());
        } else if (objectClass.other() != null) {
            gen.writeNumberField("other", objectClass.other());
        }
        gen.writeEndObject();
    }

    private void writeObjectClassVru(JsonGenerator gen, ObjectClassVru vru) throws IOException {
        gen.writeStartObject();
        if (vru.pedestrian() != null) {
            gen.writeNumberField("pedestrian", vru.pedestrian());
        } else if (vru.bicyclistAndLightVruVehicle() != null) {
            gen.writeNumberField("bicyclist_and_light_vru_vehicle", vru.bicyclistAndLightVruVehicle());
        } else if (vru.motorcylist() != null) {
            gen.writeNumberField("motorcylist", vru.motorcylist());
        } else if (vru.animal() != null) {
            gen.writeNumberField("animal", vru.animal());
        }
        gen.writeEndObject();
    }

    private void writeObjectClassGroup(JsonGenerator gen, ObjectClassGroup group) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("cluster_bounding_box_shape");
        writeShape(gen, group.clusterBoundingBoxShape());
        gen.writeNumberField("cluster_cardinality_size", group.clusterCardinalitySize());
        if (group.clusterId() != null) {
            gen.writeNumberField("cluster_id", group.clusterId());
        }
        if (group.clusterProfiles() != null) {
            gen.writeFieldName("cluster_profiles");
            writeClusterProfiles(gen, group.clusterProfiles());
        }
        gen.writeEndObject();
    }

    private void writeClusterProfiles(JsonGenerator gen, ClusterProfiles profiles) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("pedestrian", profiles.pedestrian());
        gen.writeBooleanField("bicyclist", profiles.bicyclist());
        gen.writeBooleanField("motorcyclist", profiles.motorcyclist());
        gen.writeBooleanField("animal", profiles.animal());
        gen.writeEndObject();
    }

    private void writeMapPosition(JsonGenerator gen, MapPosition mapPosition) throws IOException {
        gen.writeStartObject();
        if (mapPosition.mapReference() != null) {
            gen.writeFieldName("map_reference");
            writeMapReference(gen, mapPosition.mapReference());
        }
        if (mapPosition.laneId() != null) {
            gen.writeNumberField("lane_id", mapPosition.laneId());
        }
        if (mapPosition.connectionId() != null) {
            gen.writeNumberField("connection_id", mapPosition.connectionId());
        }
        if (mapPosition.longitudinalLanePosition() != null) {
            gen.writeFieldName("longitudinal_lane_position");
            writeLongitudinalLanePosition(gen, mapPosition.longitudinalLanePosition());
        }
        gen.writeEndObject();
    }

    private void writeLongitudinalLanePosition(JsonGenerator gen, LongitudinalLanePosition position) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", position.value());
        gen.writeNumberField("confidence", position.confidence());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* CDD helpers                                                            */
    /* --------------------------------------------------------------------- */

    private void writeAngle(JsonGenerator gen, Angle angle) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", angle.value());
        gen.writeNumberField("confidence", angle.confidence());
        gen.writeEndObject();
    }

    private void writeSpeed(JsonGenerator gen, Speed speed) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", speed.value());
        gen.writeNumberField("confidence", speed.confidence());
        gen.writeEndObject();
    }

    private void writeVelocityComponent(JsonGenerator gen, VelocityComponent component) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", component.value());
        gen.writeNumberField("confidence", component.confidence());
        gen.writeEndObject();
    }

    private void writeMapReference(JsonGenerator gen, MapReference reference) throws IOException {
        gen.writeStartObject();
        if (reference.roadSegment() != null) {
            gen.writeFieldName("road_segment");
            writeRoadSegment(gen, reference.roadSegment());
        } else if (reference.intersection() != null) {
            gen.writeFieldName("intersection");
            writeIntersection(gen, reference.intersection());
        }
        gen.writeEndObject();
    }

    private void writeRoadSegment(JsonGenerator gen, RoadSegment segment) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", segment.id());
        if (segment.region() != null) {
            gen.writeNumberField("region", segment.region());
        }
        gen.writeEndObject();
    }

    private void writeIntersection(JsonGenerator gen, Intersection intersection) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", intersection.id());
        if (intersection.region() != null) {
            gen.writeNumberField("region", intersection.region());
        }
        gen.writeEndObject();
    }

    private void writeShape(JsonGenerator gen, Shape shape) throws IOException {
        gen.writeStartObject();
        if (shape.rectangular() != null) {
            gen.writeFieldName("rectangular");
            writeRectangular(gen, shape.rectangular());
        } else if (shape.circular() != null) {
            gen.writeFieldName("circular");
            writeCircular(gen, shape.circular());
        } else if (shape.polygonal() != null) {
            gen.writeFieldName("polygonal");
            writePolygonal(gen, shape.polygonal());
        } else if (shape.elliptical() != null) {
            gen.writeFieldName("elliptical");
            writeElliptical(gen, shape.elliptical());
        } else if (shape.radial() != null) {
            gen.writeFieldName("radial");
            writeRadial(gen, shape.radial());
        } else if (shape.radialShapes() != null) {
            gen.writeFieldName("radial_shapes");
            writeRadialShapes(gen, shape.radialShapes());
        }
        gen.writeEndObject();
    }

    private void writeRectangular(JsonGenerator gen, Rectangular rectangular) throws IOException {
        gen.writeStartObject();
        if (rectangular.centerPoint() != null) {
            gen.writeFieldName("center_point");
            writeCartesianPosition3d(gen, rectangular.centerPoint());
        }
        gen.writeNumberField("semi_length", rectangular.semiLength());
        gen.writeNumberField("semi_breadth", rectangular.semiBreadth());
        if (rectangular.orientation() != null) {
            gen.writeNumberField("orientation", rectangular.orientation());
        }
        if (rectangular.height() != null) {
            gen.writeNumberField("height", rectangular.height());
        }
        gen.writeEndObject();
    }

    private void writeCircular(JsonGenerator gen, Circular circular) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("radius", circular.radius());
        if (circular.shapeReferencePoint() != null) {
            gen.writeFieldName("shape_reference_point");
            writeCartesianPosition3d(gen, circular.shapeReferencePoint());
        }
        if (circular.height() != null) {
            gen.writeNumberField("height", circular.height());
        }
        gen.writeEndObject();
    }

    private void writePolygonal(JsonGenerator gen, Polygonal polygonal) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("polygon");
        for (CartesianPosition3d position : polygonal.polygon()) {
            writeCartesianPosition3d(gen, position);
        }
        gen.writeEndArray();
        if (polygonal.shapeReferencePoint() != null) {
            gen.writeFieldName("shape_reference_point");
            writeCartesianPosition3d(gen, polygonal.shapeReferencePoint());
        }
        if (polygonal.height() != null) {
            gen.writeNumberField("height", polygonal.height());
        }
        gen.writeEndObject();
    }

    private void writeElliptical(JsonGenerator gen, Elliptical elliptical) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("semi_major_axis_length", elliptical.semiMajorAxisLength());
        gen.writeNumberField("semi_minor_axis_length", elliptical.semiMinorAxisLength());
        if (elliptical.shapeReferencePoint() != null) {
            gen.writeFieldName("shape_reference_point");
            writeCartesianPosition3d(gen, elliptical.shapeReferencePoint());
        }
        if (elliptical.orientation() != null) {
            gen.writeNumberField("orientation", elliptical.orientation());
        }
        if (elliptical.height() != null) {
            gen.writeNumberField("height", elliptical.height());
        }
        gen.writeEndObject();
    }

    private void writeRadial(JsonGenerator gen, Radial radial) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("range", radial.range());
        gen.writeNumberField("stationary_horizontal_opening_angle_start", radial.stationaryHorizontalOpeningAngleStart());
        gen.writeNumberField("stationary_horizontal_opening_angle_end", radial.stationaryHorizontalOpeningAngleEnd());
        if (radial.shapeReferencePoint() != null) {
            gen.writeFieldName("shape_reference_point");
            writeCartesianPosition3d(gen, radial.shapeReferencePoint());
        }
        if (radial.verticalOpeningAngleStart() != null) {
            gen.writeNumberField("vertical_opening_angle_start", radial.verticalOpeningAngleStart());
        }
        if (radial.verticalOpeningAngleEnd() != null) {
            gen.writeNumberField("vertical_opening_angle_end", radial.verticalOpeningAngleEnd());
        }
        gen.writeEndObject();
    }

    private void writeRadialShapes(JsonGenerator gen, RadialShapes radialShapes) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("ref_point_id", radialShapes.refPointId());
        gen.writeNumberField("x_coordinate", radialShapes.xCoordinate());
        gen.writeNumberField("y_coordinate", radialShapes.yCoordinate());
        if (radialShapes.zCoordinate() != null) {
            gen.writeNumberField("z_coordinate", radialShapes.zCoordinate());
        }
        gen.writeArrayFieldStart("radial_shapes_list");
        for (Radial radial : radialShapes.radialShapesList()) {
            writeRadial(gen, radial);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeCartesianPosition3d(JsonGenerator gen, CartesianPosition3d position) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x_coordinate", position.xCoordinate());
        gen.writeNumberField("y_coordinate", position.yCoordinate());
        if (position.zCoordinate() != null) {
            gen.writeNumberField("z_coordinate", position.zCoordinate());
        }
        gen.writeEndObject();
    }
}
