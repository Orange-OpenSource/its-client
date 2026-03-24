/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidator121;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class CpmWriter121 {

    private final JsonFactory jsonFactory;

    public CpmWriter121(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(CpmEnvelope121 envelope, OutputStream out) throws IOException {
        CpmValidator121.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("type", envelope.type());
            gen.writeStringField("origin", envelope.origin());
            gen.writeStringField("version", envelope.version());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, CpmMessage121 msg) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", msg.protocolVersion());
        gen.writeNumberField("station_id", msg.stationId());
        gen.writeNumberField("generation_delta_time", msg.generationDeltaTime());

        gen.writeFieldName("management_container");
        writeManagementContainer(gen, msg.managementContainer());

        if (msg.stationDataContainer() != null) {
            gen.writeFieldName("station_data_container");
            writeStationDataContainer(gen, msg.stationDataContainer());
        }
        if (msg.sensorInformationContainer() != null) {
            SensorInformationContainer container = msg.sensorInformationContainer();
            gen.writeArrayFieldStart("sensor_information_container");
            for (SensorInformation info : container.sensorInformation()) {
                writeSensorInformation(gen, info);
            }
            gen.writeEndArray();
        }
        if (msg.perceivedObjectContainer() != null) {
            PerceivedObjectContainer container = msg.perceivedObjectContainer();
            gen.writeArrayFieldStart("perceived_object_container");
            for (PerceivedObject obj : container.perceivedObjects()) {
                writePerceivedObject(gen, obj);
            }
            gen.writeEndArray();
        }
        if (msg.freeSpaceAddendumContainer() != null) {
            FreeSpaceAddendumContainer container = msg.freeSpaceAddendumContainer();
            gen.writeArrayFieldStart("free_space_addendum_container");
            for (FreeSpaceAddendum addendum : container.freeSpaceAddenda()) {
                writeFreeSpaceAddendum(gen, addendum);
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
        gen.writeNumberField("station_type", container.stationType());
        gen.writeFieldName("reference_position");
        writeReferencePosition(gen, container.referencePosition());
        gen.writeFieldName("confidence");
        writeManagementConfidence(gen, container.confidence());
        gen.writeEndObject();
    }

    private void writeReferencePosition(JsonGenerator gen, ReferencePosition reference) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", reference.latitude());
        gen.writeNumberField("longitude", reference.longitude());
        gen.writeNumberField("altitude", reference.altitude());
        gen.writeEndObject();
    }

    private void writeManagementConfidence(JsonGenerator gen, ManagementConfidence confidence) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("position_confidence_ellipse");
        writePositionConfidenceEllipse(gen, confidence.positionConfidenceEllipse());
        gen.writeNumberField("altitude", confidence.altitude());
        gen.writeEndObject();
    }

    private void writePositionConfidenceEllipse(JsonGenerator gen, PositionConfidenceEllipse ellipse) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("semi_major_confidence", ellipse.semiMajorConfidence());
        gen.writeNumberField("semi_minor_confidence", ellipse.semiMinorConfidence());
        gen.writeNumberField("semi_major_orientation", ellipse.semiMajorOrientation());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Station data container                                                 */
    /* --------------------------------------------------------------------- */

    private void writeStationDataContainer(JsonGenerator gen, StationDataContainer container) throws IOException {
        gen.writeStartObject();
        if (container.originatingVehicleContainer() != null) {
            gen.writeFieldName("originating_vehicle_container");
            writeOriginatingVehicleContainer(gen, container.originatingVehicleContainer());
        }
        if (container.originatingRsuContainer() != null) {
            gen.writeFieldName("originating_rsu_container");
            writeOriginatingRsuContainer(gen, container.originatingRsuContainer());
        }
        gen.writeEndObject();
    }

    private void writeOriginatingVehicleContainer(JsonGenerator gen, OriginatingVehicleContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("heading", container.heading());
        gen.writeNumberField("speed", container.speed());
        gen.writeFieldName("confidence");
        writeVehicleConfidence(gen, container.confidence());
        if (container.driveDirection() != null) {
            gen.writeNumberField("drive_direction", container.driveDirection());
        }
        if (container.vehicleLength() != null) {
            gen.writeNumberField("vehicle_length", container.vehicleLength());
        }
        if (container.vehicleWidth() != null) {
            gen.writeNumberField("vehicle_width", container.vehicleWidth());
        }
        if (container.longitudinalAcceleration() != null) {
            gen.writeNumberField("longitudinal_acceleration", container.longitudinalAcceleration());
        }
        if (container.yawRate() != null) {
            gen.writeNumberField("yaw_rate", container.yawRate());
        }
        if (container.lateralAcceleration() != null) {
            gen.writeNumberField("lateral_acceleration", container.lateralAcceleration());
        }
        if (container.verticalAcceleration() != null) {
            gen.writeNumberField("vertical_acceleration", container.verticalAcceleration());
        }
        gen.writeEndObject();
    }

    private void writeVehicleConfidence(JsonGenerator gen, VehicleConfidence confidence) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("heading", confidence.heading());
        gen.writeNumberField("speed", confidence.speed());
        if (confidence.vehicleLength() != null) {
            gen.writeNumberField("vehicle_length", confidence.vehicleLength());
        }
        if (confidence.yawRate() != null) {
            gen.writeNumberField("yaw_rate", confidence.yawRate());
        }
        if (confidence.longitudinalAcceleration() != null) {
            gen.writeNumberField("longitudinal_acceleration", confidence.longitudinalAcceleration());
        }
        if (confidence.lateralAcceleration() != null) {
            gen.writeNumberField("lateral_acceleration", confidence.lateralAcceleration());
        }
        if (confidence.verticalAcceleration() != null) {
            gen.writeNumberField("vertical_acceleration", confidence.verticalAcceleration());
        }
        gen.writeEndObject();
    }

    private void writeOriginatingRsuContainer(JsonGenerator gen, OriginatingRsuContainer container) throws IOException {
        gen.writeStartObject();
        if (container.region() != null) {
            gen.writeNumberField("region", container.region());
        }
        if (container.intersectionReferenceId() != null) {
            gen.writeNumberField("intersection_reference_id", container.intersectionReferenceId());
        }
        if (container.roadSegmentReferenceId() != null) {
            gen.writeNumberField("road_segment_reference_id", container.roadSegmentReferenceId());
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Sensor information container                                           */
    /* --------------------------------------------------------------------- */

    private void writeSensorInformation(JsonGenerator gen, SensorInformation info) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("sensor_id", info.sensorId());
        gen.writeNumberField("type", info.type());
        gen.writeFieldName("detection_area");
        writeDetectionArea(gen, info.detectionArea());
        gen.writeEndObject();
    }

    private void writeDetectionArea(JsonGenerator gen, DetectionArea area) throws IOException {
        gen.writeStartObject();
        if (area.vehicleSensor() != null) {
            gen.writeFieldName("vehicle_sensor");
            writeVehicleSensor(gen, area.vehicleSensor());
        }
        if (area.stationarySensorRadial() != null) {
            gen.writeFieldName("stationary_sensor_radial");
            writeStationarySensorRadial(gen, area.stationarySensorRadial());
        }
        if (area.stationarySensorPolygon() != null) {
            gen.writeFieldName("stationary_sensor_polygon");
            writeAreaPolygon(gen, area.stationarySensorPolygon());
        }
        if (area.stationarySensorCircular() != null) {
            gen.writeFieldName("stationary_sensor_circular");
            writeAreaCircular(gen, area.stationarySensorCircular());
        }
        if (area.stationarySensorEllipse() != null) {
            gen.writeFieldName("stationary_sensor_ellipse");
            writeAreaEllipse(gen, area.stationarySensorEllipse());
        }
        if (area.stationarySensorRectangle() != null) {
            gen.writeFieldName("stationary_sensor_rectangle");
            writeAreaRectangle(gen, area.stationarySensorRectangle());
        }
        gen.writeEndObject();
    }

    private void writeVehicleSensor(JsonGenerator gen, VehicleSensor sensor) throws IOException {
        gen.writeStartObject();
        if (sensor.refPointId() != null) {
            gen.writeNumberField("ref_point_id", sensor.refPointId());
        }
        gen.writeNumberField("x_sensor_offset", sensor.xSensorOffset());
        gen.writeNumberField("y_sensor_offset", sensor.ySensorOffset());
        if (sensor.zSensorOffset() != null) {
            gen.writeNumberField("z_sensor_offset", sensor.zSensorOffset());
        }
        gen.writeArrayFieldStart("vehicle_sensor_property_list");
        for (VehicleSensorProperty property : sensor.vehicleSensorPropertyList()) {
            writeVehicleSensorProperty(gen, property);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeVehicleSensorProperty(JsonGenerator gen, VehicleSensorProperty property) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("range", property.range());
        gen.writeNumberField("horizontal_opening_angle_start", property.horizontalOpeningAngleStart());
        gen.writeNumberField("horizontal_opening_angle_end", property.horizontalOpeningAngleEnd());
        if (property.verticalOpeningAngleStart() != null) {
            gen.writeNumberField("vertical_opening_angle_start", property.verticalOpeningAngleStart());
        }
        if (property.verticalOpeningAngleEnd() != null) {
            gen.writeNumberField("vertical_opening_angle_end", property.verticalOpeningAngleEnd());
        }
        gen.writeEndObject();
    }

    private void writeStationarySensorRadial(JsonGenerator gen, StationarySensorRadial radial) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("range", radial.range());
        gen.writeNumberField("horizontal_opening_angle_start", radial.horizontalOpeningAngleStart());
        gen.writeNumberField("horizontal_opening_angle_end", radial.horizontalOpeningAngleEnd());
        if (radial.verticalOpeningAngleStart() != null) {
            gen.writeNumberField("vertical_opening_angle_start", radial.verticalOpeningAngleStart());
        }
        if (radial.verticalOpeningAngleEnd() != null) {
            gen.writeNumberField("vertical_opening_angle_end", radial.verticalOpeningAngleEnd());
        }
        if (radial.sensorPositionOffset() != null) {
            gen.writeFieldName("sensor_position_offset");
            writeOffset(gen, radial.sensorPositionOffset());
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Perceived object container                                             */
    /* --------------------------------------------------------------------- */

    private void writePerceivedObject(JsonGenerator gen, PerceivedObject object) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("object_id", object.objectId());
        gen.writeNumberField("time_of_measurement", object.timeOfMeasurement());
        gen.writeNumberField("x_distance", object.xDistance());
        gen.writeNumberField("y_distance", object.yDistance());
        if (object.zDistance() != null) {
            gen.writeNumberField("z_distance", object.zDistance());
        }
        gen.writeNumberField("x_speed", object.xSpeed());
        gen.writeNumberField("y_speed", object.ySpeed());
        if (object.zSpeed() != null) {
            gen.writeNumberField("z_speed", object.zSpeed());
        }
        if (object.xAcceleration() != null) {
            gen.writeNumberField("x_acceleration", object.xAcceleration());
        }
        if (object.yAcceleration() != null) {
            gen.writeNumberField("y_acceleration", object.yAcceleration());
        }
        if (object.zAcceleration() != null) {
            gen.writeNumberField("z_acceleration", object.zAcceleration());
        }
        if (object.rollAngle() != null) {
            gen.writeNumberField("roll_angle", object.rollAngle());
        }
        if (object.pitchAngle() != null) {
            gen.writeNumberField("pitch_angle", object.pitchAngle());
        }
        if (object.yawAngle() != null) {
            gen.writeNumberField("yaw_angle", object.yawAngle());
        }
        if (object.rollRate() != null) {
            gen.writeNumberField("roll_rate", object.rollRate());
        }
        if (object.pitchRate() != null) {
            gen.writeNumberField("pitch_rate", object.pitchRate());
        }
        if (object.yawRate() != null) {
            gen.writeNumberField("yaw_rate", object.yawRate());
        }
        if (object.rollAcceleration() != null) {
            gen.writeNumberField("roll_acceleration", object.rollAcceleration());
        }
        if (object.pitchAcceleration() != null) {
            gen.writeNumberField("pitch_acceleration", object.pitchAcceleration());
        }
        if (object.yawAcceleration() != null) {
            gen.writeNumberField("yaw_acceleration", object.yawAcceleration());
        }
        if (object.lowerTriangularCorrelationMatrixColumns() != null) {
            gen.writeArrayFieldStart("lower_triangular_correlation_matrix_columns");
            for (List<Integer> column : object.lowerTriangularCorrelationMatrixColumns().columns()) {
                writeIntegerArray(gen, column);
            }
            gen.writeEndArray();
        }
        if (object.planarObjectDimension1() != null) {
            gen.writeNumberField("planar_object_dimension_1", object.planarObjectDimension1());
        }
        if (object.planarObjectDimension2() != null) {
            gen.writeNumberField("planar_object_dimension_2", object.planarObjectDimension2());
        }
        if (object.verticalObjectDimension() != null) {
            gen.writeNumberField("vertical_object_dimension", object.verticalObjectDimension());
        }
        if (object.objectRefPoint() != null) {
            gen.writeNumberField("object_ref_point", object.objectRefPoint());
        }
        gen.writeNumberField("object_age", object.objectAge());
        if (object.sensorIdList() != null) {
            gen.writeArrayFieldStart("sensor_id_list");
            for (Integer id : object.sensorIdList()) {
                gen.writeNumber(id);
            }
            gen.writeEndArray();
        }
        if (object.dynamicStatus() != null) {
            gen.writeNumberField("dynamic_status", object.dynamicStatus());
        }
        if (object.classification() != null) {
            gen.writeArrayFieldStart("classification");
            for (ObjectClassification classification : object.classification()) {
                writeObjectClassification(gen, classification);
            }
            gen.writeEndArray();
        }
        if (object.matchedPosition() != null) {
            gen.writeFieldName("matched_position");
            writeMapPosition(gen, object.matchedPosition());
        }
        gen.writeFieldName("confidence");
        writePerceivedObjectConfidence(gen, object.confidence());
        gen.writeEndObject();
    }

    private void writePerceivedObjectConfidence(JsonGenerator gen, PerceivedObjectConfidence confidence) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x_distance", confidence.xDistance());
        gen.writeNumberField("y_distance", confidence.yDistance());
        if (confidence.zDistance() != null) {
            gen.writeNumberField("z_distance", confidence.zDistance());
        }
        gen.writeNumberField("x_speed", confidence.xSpeed());
        gen.writeNumberField("y_speed", confidence.ySpeed());
        if (confidence.zSpeed() != null) {
            gen.writeNumberField("z_speed", confidence.zSpeed());
        }
        if (confidence.xAcceleration() != null) {
            gen.writeNumberField("x_acceleration", confidence.xAcceleration());
        }
        if (confidence.yAcceleration() != null) {
            gen.writeNumberField("y_acceleration", confidence.yAcceleration());
        }
        if (confidence.zAcceleration() != null) {
            gen.writeNumberField("z_acceleration", confidence.zAcceleration());
        }
        if (confidence.rollAngle() != null) {
            gen.writeNumberField("roll_angle", confidence.rollAngle());
        }
        if (confidence.pitchAngle() != null) {
            gen.writeNumberField("pitch_angle", confidence.pitchAngle());
        }
        if (confidence.yawAngle() != null) {
            gen.writeNumberField("yaw_angle", confidence.yawAngle());
        }
        if (confidence.rollRate() != null) {
            gen.writeNumberField("roll_rate", confidence.rollRate());
        }
        if (confidence.pitchRate() != null) {
            gen.writeNumberField("pitch_rate", confidence.pitchRate());
        }
        if (confidence.yawRate() != null) {
            gen.writeNumberField("yaw_rate", confidence.yawRate());
        }
        if (confidence.rollAcceleration() != null) {
            gen.writeNumberField("roll_acceleration", confidence.rollAcceleration());
        }
        if (confidence.pitchAcceleration() != null) {
            gen.writeNumberField("pitch_acceleration", confidence.pitchAcceleration());
        }
        if (confidence.yawAcceleration() != null) {
            gen.writeNumberField("yaw_acceleration", confidence.yawAcceleration());
        }
        if (confidence.planarObjectDimension1() != null) {
            gen.writeNumberField("planar_object_dimension_1", confidence.planarObjectDimension1());
        }
        if (confidence.planarObjectDimension2() != null) {
            gen.writeNumberField("planar_object_dimension_2", confidence.planarObjectDimension2());
        }
        if (confidence.verticalObjectDimension() != null) {
            gen.writeNumberField("vertical_object_dimension", confidence.verticalObjectDimension());
        }
        if (confidence.longitudinalLanePosition() != null) {
            gen.writeNumberField("longitudinal_lane_position", confidence.longitudinalLanePosition());
        }
        gen.writeNumberField("object", confidence.object());
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
        }
        if (objectClass.singleVru() != null) {
            gen.writeFieldName("single_vru");
            writeObjectClassVru(gen, objectClass.singleVru());
        }
        if (objectClass.vruGroup() != null) {
            gen.writeFieldName("vru_group");
            writeObjectClassGroup(gen, objectClass.vruGroup());
        }
        if (objectClass.other() != null) {
            gen.writeNumberField("other", objectClass.other());
        }
        gen.writeEndObject();
    }

    private void writeObjectClassVru(JsonGenerator gen, ObjectClassVru vru) throws IOException {
        gen.writeStartObject();
        if (vru.pedestrian() != null) {
            gen.writeNumberField("pedestrian", vru.pedestrian());
        }
        if (vru.bicyclist() != null) {
            gen.writeNumberField("bicyclist", vru.bicyclist());
        }
        if (vru.motorcylist() != null) {
            gen.writeNumberField("motorcylist", vru.motorcylist());
        }
        if (vru.animal() != null) {
            gen.writeNumberField("animal", vru.animal());
        }
        gen.writeEndObject();
    }

    private void writeObjectClassGroup(JsonGenerator gen, ObjectClassGroup group) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("group_type");
        writeGroupType(gen, group.groupType());
        gen.writeNumberField("group_size", group.groupSize());
        if (group.clusterId() != null) {
            gen.writeNumberField("cluster_id", group.clusterId());
        }
        gen.writeEndObject();
    }

    private void writeGroupType(JsonGenerator gen, GroupType type) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("pedestrian", type.pedestrian());
        gen.writeBooleanField("bicyclist", type.bicyclist());
        gen.writeBooleanField("motorcyclist", type.motorcyclist());
        gen.writeBooleanField("animal", type.animal());
        gen.writeEndObject();
    }

    private void writeMapPosition(JsonGenerator gen, MapPosition position) throws IOException {
        gen.writeStartObject();
        if (position.laneId() != null) {
            gen.writeNumberField("lane_id", position.laneId());
        }
        if (position.longitudinalLanePosition() != null) {
            gen.writeNumberField("longitudinal_lane_position", position.longitudinalLanePosition());
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Free space addendum container                                          */
    /* --------------------------------------------------------------------- */

    private void writeFreeSpaceAddendum(JsonGenerator gen, FreeSpaceAddendum addendum) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("free_space_area");
        writeFreeSpaceArea(gen, addendum.freeSpaceArea());
        gen.writeNumberField("free_space_confidence", addendum.freeSpaceConfidence());
        if (addendum.sensorIdList() != null) {
            gen.writeArrayFieldStart("sensor_id_list");
            for (Integer id : addendum.sensorIdList()) {
                gen.writeNumber(id);
            }
            gen.writeEndArray();
        }
        if (addendum.shadowingApplies() != null) {
            gen.writeBooleanField("shadowing_applies", addendum.shadowingApplies());
        }
        gen.writeEndObject();
    }

    private void writeFreeSpaceArea(JsonGenerator gen, FreeSpaceArea area) throws IOException {
        gen.writeStartObject();
        if (area.freeSpacePolygon() != null) {
            gen.writeFieldName("free_space_polygon");
            writeAreaPolygon(gen, area.freeSpacePolygon());
        }
        if (area.freeSpaceCircular() != null) {
            gen.writeFieldName("free_space_circular");
            writeAreaCircular(gen, area.freeSpaceCircular());
        }
        if (area.freeSpaceEllipse() != null) {
            gen.writeFieldName("free_space_ellipse");
            writeAreaEllipse(gen, area.freeSpaceEllipse());
        }
        if (area.freeSpaceRectangle() != null) {
            gen.writeFieldName("free_space_rectangle");
            writeAreaRectangle(gen, area.freeSpaceRectangle());
        }
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Shared helpers                                                         */
    /* --------------------------------------------------------------------- */

    private void writeOffset(JsonGenerator gen, Offset offset) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", offset.x());
        gen.writeNumberField("y", offset.y());
        if (offset.z() != null) {
            gen.writeNumberField("z", offset.z());
        }
        gen.writeEndObject();
    }

    private void writeAreaPolygon(JsonGenerator gen, AreaPolygon polygon) throws IOException {
        gen.writeStartArray();
        for (Offset offset : polygon.offsets()) {
            writeOffset(gen, offset);
        }
        gen.writeEndArray();
    }

    private void writeAreaCircular(JsonGenerator gen, AreaCircular circular) throws IOException {
        gen.writeStartObject();
        if (circular.nodeCenterPoint() != null) {
            gen.writeFieldName("node_center_point");
            writeOffset(gen, circular.nodeCenterPoint());
        }
        gen.writeNumberField("radius", circular.radius());
        gen.writeEndObject();
    }

    private void writeAreaEllipse(JsonGenerator gen, AreaEllipse ellipse) throws IOException {
        gen.writeStartObject();
        if (ellipse.nodeCenterPoint() != null) {
            gen.writeFieldName("node_center_point");
            writeOffset(gen, ellipse.nodeCenterPoint());
        }
        gen.writeNumberField("semi_major_range_length", ellipse.semiMajorRangeLength());
        gen.writeNumberField("semi_minor_range_length", ellipse.semiMinorRangeLength());
        gen.writeNumberField("semi_major_range_orientation", ellipse.semiMajorRangeOrientation());
        if (ellipse.semiHeight() != null) {
            gen.writeNumberField("semi_height", ellipse.semiHeight());
        }
        gen.writeEndObject();
    }

    private void writeAreaRectangle(JsonGenerator gen, AreaRectangle rectangle) throws IOException {
        gen.writeStartObject();
        if (rectangle.nodeCenterPoint() != null) {
            gen.writeFieldName("node_center_point");
            writeOffset(gen, rectangle.nodeCenterPoint());
        }
        gen.writeNumberField("semi_major_range_length", rectangle.semiMajorRangeLength());
        gen.writeNumberField("semi_minor_range_length", rectangle.semiMinorRangeLength());
        gen.writeNumberField("semi_major_range_orientation", rectangle.semiMajorRangeOrientation());
        if (rectangle.semiHeight() != null) {
            gen.writeNumberField("semi_height", rectangle.semiHeight());
        }
        gen.writeEndObject();
    }

    private void writeIntegerArray(JsonGenerator gen, List<Integer> values) throws IOException {
        gen.writeStartArray();
        for (Integer value : values) {
            gen.writeNumber(value);
        }
        gen.writeEndArray();
    }
}
