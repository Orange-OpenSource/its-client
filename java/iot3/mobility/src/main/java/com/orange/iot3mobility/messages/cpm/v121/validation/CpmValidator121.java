/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.validation;

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

import java.util.List;
import java.util.Objects;

public final class CpmValidator121 {

    private CpmValidator121() {}

    public static void validateEnvelope(CpmEnvelope121 env) {
        requireNonNull("envelope", env);
        requireEquals("type", env.type(), "cpm");
        requireEnum("origin", env.origin(),
                List.of("self", "global_application", "mec_application", "on_board_application"));
        requireEquals("version", env.version(), "1.2.1");
        requireNotBlank("source_uuid", env.sourceUuid());
        checkRange("timestamp", env.timestamp(), 1514764800000L, 1830297600000L);
        validateMessage(env.message());
    }

    public static void validateMessage(CpmMessage121 msg) {
        requireNonNull("message", msg);
        checkRange("protocol_version", msg.protocolVersion(), 0, 255);
        checkRange("station_id", msg.stationId(), 0, 4294967295L);
        checkRange("generation_delta_time", msg.generationDeltaTime(), 0, 65535);
        validateManagementContainer(msg.managementContainer());
        if (msg.stationDataContainer() != null) {
            validateStationDataContainer(msg.stationDataContainer());
        }
        if (msg.sensorInformationContainer() != null) {
            validateSensorInformationContainer(msg.sensorInformationContainer());
        }
        if (msg.perceivedObjectContainer() != null) {
            validatePerceivedObjectContainer(msg.perceivedObjectContainer());
        }
        if (msg.freeSpaceAddendumContainer() != null) {
            validateFreeSpaceAddendumContainer(msg.freeSpaceAddendumContainer());
        }
    }

    /* --------------------------------------------------------------------- */
    /* Management container                                                   */
    /* --------------------------------------------------------------------- */

    private static void validateManagementContainer(ManagementContainer container) {
        requireNonNull("management_container", container);
        checkRange("management_container.station_type", container.stationType(), 0, 254);
        validateReferencePosition(container.referencePosition());
        validateManagementConfidence(container.confidence());
    }

    private static void validateReferencePosition(ReferencePosition reference) {
        requireNonNull("reference_position", reference);
        checkRange("reference_position.latitude", reference.latitude(), -900000000, 900000001);
        checkRange("reference_position.longitude", reference.longitude(), -1800000000, 1800000001);
        checkRange("reference_position.altitude", reference.altitude(), -100000, 800001);
    }

    private static void validateManagementConfidence(ManagementConfidence confidence) {
        requireNonNull("management_container.confidence", confidence);
        validatePositionConfidenceEllipse("management_container.confidence.position_confidence_ellipse",
                confidence.positionConfidenceEllipse());
        checkRange("management_container.confidence.altitude", confidence.altitude(), 0, 15);
    }

    private static void validatePositionConfidenceEllipse(String prefix, PositionConfidenceEllipse ellipse) {
        requireNonNull(prefix, ellipse);
        checkRange(prefix + ".semi_major_confidence", ellipse.semiMajorConfidence(), 0, 4095);
        checkRange(prefix + ".semi_minor_confidence", ellipse.semiMinorConfidence(), 0, 4095);
        checkRange(prefix + ".semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
    }

    /* --------------------------------------------------------------------- */
    /* Station data container                                                 */
    /* --------------------------------------------------------------------- */

    private static void validateStationDataContainer(StationDataContainer container) {
        requireNonNull("station_data_container", container);
        boolean hasVehicle = container.originatingVehicleContainer() != null;
        boolean hasRsu = container.originatingRsuContainer() != null;
        if (hasVehicle == hasRsu) {
            throw new CpmValidationException("station_data_container must contain exactly one of originating_vehicle_container or originating_rsu_container");
        }
        if (hasVehicle) {
            validateOriginatingVehicleContainer(container.originatingVehicleContainer());
        }
        if (hasRsu) {
            validateOriginatingRsuContainer(container.originatingRsuContainer());
        }
    }

    private static void validateOriginatingVehicleContainer(OriginatingVehicleContainer container) {
        requireNonNull("originating_vehicle_container", container);
        checkRange("originating_vehicle_container.heading", container.heading(), 0, 3601);
        checkRange("originating_vehicle_container.speed", container.speed(), 0, 16383);
        validateVehicleConfidence(container.confidence());
        if (container.driveDirection() != null) {
            checkRange("originating_vehicle_container.drive_direction", container.driveDirection(), 0, 2);
        }
        if (container.vehicleLength() != null) {
            checkRange("originating_vehicle_container.vehicle_length", container.vehicleLength(), 1, 1023);
        }
        if (container.vehicleWidth() != null) {
            checkRange("originating_vehicle_container.vehicle_width", container.vehicleWidth(), 1, 62);
        }
        if (container.longitudinalAcceleration() != null) {
            checkRange("originating_vehicle_container.longitudinal_acceleration", container.longitudinalAcceleration(), -160, 161);
        }
        if (container.yawRate() != null) {
            checkRange("originating_vehicle_container.yaw_rate", container.yawRate(), -32766, 32767);
        }
        if (container.lateralAcceleration() != null) {
            checkRange("originating_vehicle_container.lateral_acceleration", container.lateralAcceleration(), -160, 161);
        }
        if (container.verticalAcceleration() != null) {
            checkRange("originating_vehicle_container.vertical_acceleration", container.verticalAcceleration(), -160, 161);
        }
    }

    private static void validateVehicleConfidence(VehicleConfidence confidence) {
        requireNonNull("originating_vehicle_container.confidence", confidence);
        checkRange("originating_vehicle_container.confidence.heading", confidence.heading(), 1, 127);
        checkRange("originating_vehicle_container.confidence.speed", confidence.speed(), 1, 127);
        if (confidence.vehicleLength() != null) {
            checkRange("originating_vehicle_container.confidence.vehicle_length", confidence.vehicleLength(), 0, 4);
        }
        if (confidence.yawRate() != null) {
            checkRange("originating_vehicle_container.confidence.yaw_rate", confidence.yawRate(), 0, 8);
        }
        if (confidence.longitudinalAcceleration() != null) {
            checkRange("originating_vehicle_container.confidence.longitudinal_acceleration", confidence.longitudinalAcceleration(), 0, 102);
        }
        if (confidence.lateralAcceleration() != null) {
            checkRange("originating_vehicle_container.confidence.lateral_acceleration", confidence.lateralAcceleration(), 0, 102);
        }
        if (confidence.verticalAcceleration() != null) {
            checkRange("originating_vehicle_container.confidence.vertical_acceleration", confidence.verticalAcceleration(), 0, 102);
        }
    }

    private static void validateOriginatingRsuContainer(OriginatingRsuContainer container) {
        requireNonNull("originating_rsu_container", container);
        if (container.region() != null) {
            checkRange("originating_rsu_container.region", container.region(), 0, 65535);
        }
        if (container.intersectionReferenceId() != null) {
            checkRange("originating_rsu_container.intersection_reference_id", container.intersectionReferenceId(), 0, 65535);
        }
        if (container.roadSegmentReferenceId() != null) {
            checkRange("originating_rsu_container.road_segment_reference_id", container.roadSegmentReferenceId(), 0, 65535);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Sensor information container                                           */
    /* --------------------------------------------------------------------- */

    private static void validateSensorInformationContainer(SensorInformationContainer container) {
        requireNonNull("sensor_information_container", container);
        List<SensorInformation> sensors = requireNonNull("sensor_information_container", container.sensorInformation());
        int size = sensors.size();
        if (size < 1 || size > 128) {
            throw new CpmValidationException("sensor_information_container size out of range [1,128]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateSensorInformation("sensor_information_container[" + i + "]", sensors.get(i));
        }
    }

    private static void validateSensorInformation(String prefix, SensorInformation info) {
        requireNonNull(prefix, info);
        checkRange(prefix + ".sensor_id", info.sensorId(), 0, 255);
        checkRange(prefix + ".type", info.type(), 0, 15);
        validateDetectionArea(prefix + ".detection_area", info.detectionArea());
    }

    private static void validateDetectionArea(String prefix, DetectionArea area) {
        requireNonNull(prefix, area);
        if (area.vehicleSensor() != null) {
            validateVehicleSensor(prefix + ".vehicle_sensor", area.vehicleSensor());
        }
        if (area.stationarySensorRadial() != null) {
            validateStationarySensorRadial(prefix + ".stationary_sensor_radial", area.stationarySensorRadial());
        }
        if (area.stationarySensorPolygon() != null) {
            validateAreaPolygon(prefix + ".stationary_sensor_polygon", area.stationarySensorPolygon());
        }
        if (area.stationarySensorCircular() != null) {
            validateAreaCircular(prefix + ".stationary_sensor_circular", area.stationarySensorCircular());
        }
        if (area.stationarySensorEllipse() != null) {
            validateAreaEllipse(prefix + ".stationary_sensor_ellipse", area.stationarySensorEllipse());
        }
        if (area.stationarySensorRectangle() != null) {
            validateAreaRectangle(prefix + ".stationary_sensor_rectangle", area.stationarySensorRectangle());
        }
    }

    private static void validateVehicleSensor(String prefix, VehicleSensor sensor) {
        requireNonNull(prefix, sensor);
        if (sensor.refPointId() != null) {
            checkRange(prefix + ".ref_point_id", sensor.refPointId(), 0, 255);
        }
        checkRange(prefix + ".x_sensor_offset", sensor.xSensorOffset(), -3094, 1001);
        checkRange(prefix + ".y_sensor_offset", sensor.ySensorOffset(), -1000, 1000);
        if (sensor.zSensorOffset() != null) {
            checkRange(prefix + ".z_sensor_offset", sensor.zSensorOffset(), 0, 1000);
        }
        List<VehicleSensorProperty> properties = requireNonNull(prefix + ".vehicle_sensor_property_list",
                sensor.vehicleSensorPropertyList());
        int size = properties.size();
        if (size < 1 || size > 10) {
            throw new CpmValidationException(prefix + ".vehicle_sensor_property_list size out of range [1,10]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateVehicleSensorProperty(prefix + ".vehicle_sensor_property_list[" + i + "]", properties.get(i));
        }
    }

    private static void validateVehicleSensorProperty(String prefix, VehicleSensorProperty property) {
        requireNonNull(prefix, property);
        checkRange(prefix + ".range", property.range(), 0, 10000);
        checkRange(prefix + ".horizontal_opening_angle_start", property.horizontalOpeningAngleStart(), 0, 3601);
        checkRange(prefix + ".horizontal_opening_angle_end", property.horizontalOpeningAngleEnd(), 0, 3601);
        if (property.verticalOpeningAngleStart() != null) {
            checkRange(prefix + ".vertical_opening_angle_start", property.verticalOpeningAngleStart(), 0, 3601);
        }
        if (property.verticalOpeningAngleEnd() != null) {
            checkRange(prefix + ".vertical_opening_angle_end", property.verticalOpeningAngleEnd(), 0, 3601);
        }
    }

    private static void validateStationarySensorRadial(String prefix, StationarySensorRadial radial) {
        requireNonNull(prefix, radial);
        checkRange(prefix + ".range", radial.range(), 0, 10000);
        checkRange(prefix + ".horizontal_opening_angle_start", radial.horizontalOpeningAngleStart(), 0, 3601);
        checkRange(prefix + ".horizontal_opening_angle_end", radial.horizontalOpeningAngleEnd(), 0, 3601);
        if (radial.verticalOpeningAngleStart() != null) {
            checkRange(prefix + ".vertical_opening_angle_start", radial.verticalOpeningAngleStart(), 0, 3601);
        }
        if (radial.verticalOpeningAngleEnd() != null) {
            checkRange(prefix + ".vertical_opening_angle_end", radial.verticalOpeningAngleEnd(), 0, 3601);
        }
        if (radial.sensorPositionOffset() != null) {
            validateOffset(prefix + ".sensor_position_offset", radial.sensorPositionOffset());
        }
    }

    /* --------------------------------------------------------------------- */
    /* Perceived object container                                             */
    /* --------------------------------------------------------------------- */

    private static void validatePerceivedObjectContainer(PerceivedObjectContainer container) {
        requireNonNull("perceived_object_container", container);
        List<PerceivedObject> objects = requireNonNull("perceived_object_container", container.perceivedObjects());
        int size = objects.size();
        if (size < 1 || size > 128) {
            throw new CpmValidationException("perceived_object_container size out of range [1,128]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validatePerceivedObject("perceived_object_container[" + i + "]", objects.get(i));
        }
    }

    private static void validatePerceivedObject(String prefix, PerceivedObject object) {
        requireNonNull(prefix, object);
        checkRange(prefix + ".object_id", object.objectId(), 0, 255);
        checkRange(prefix + ".time_of_measurement", object.timeOfMeasurement(), -1500, 1500);
        checkRange(prefix + ".x_distance", object.xDistance(), -132768, 132767);
        checkRange(prefix + ".y_distance", object.yDistance(), -132768, 132767);
        if (object.zDistance() != null) {
            checkRange(prefix + ".z_distance", object.zDistance(), -132768, 132767);
        }
        checkRange(prefix + ".x_speed", object.xSpeed(), -16383, 16383);
        checkRange(prefix + ".y_speed", object.ySpeed(), -16383, 16383);
        if (object.zSpeed() != null) {
            checkRange(prefix + ".z_speed", object.zSpeed(), -16383, 16383);
        }
        if (object.xAcceleration() != null) {
            checkRange(prefix + ".x_acceleration", object.xAcceleration(), -160, 161);
        }
        if (object.yAcceleration() != null) {
            checkRange(prefix + ".y_acceleration", object.yAcceleration(), -160, 161);
        }
        if (object.zAcceleration() != null) {
            checkRange(prefix + ".z_acceleration", object.zAcceleration(), -160, 161);
        }
        if (object.rollAngle() != null) {
            checkRange(prefix + ".roll_angle", object.rollAngle(), 0, 3601);
        }
        if (object.pitchAngle() != null) {
            checkRange(prefix + ".pitch_angle", object.pitchAngle(), 0, 3601);
        }
        if (object.yawAngle() != null) {
            checkRange(prefix + ".yaw_angle", object.yawAngle(), 0, 3601);
        }
        if (object.rollRate() != null) {
            checkRange(prefix + ".roll_rate", object.rollRate(), -32766, 32767);
        }
        if (object.pitchRate() != null) {
            checkRange(prefix + ".pitch_rate", object.pitchRate(), -32766, 32767);
        }
        if (object.yawRate() != null) {
            checkRange(prefix + ".yaw_rate", object.yawRate(), -32766, 32767);
        }
        if (object.rollAcceleration() != null) {
            checkRange(prefix + ".roll_acceleration", object.rollAcceleration(), -32766, 32767);
        }
        if (object.pitchAcceleration() != null) {
            checkRange(prefix + ".pitch_acceleration", object.pitchAcceleration(), -32766, 32767);
        }
        if (object.yawAcceleration() != null) {
            checkRange(prefix + ".yaw_acceleration", object.yawAcceleration(), -32766, 32767);
        }
        if (object.lowerTriangularCorrelationMatrixColumns() != null) {
            validateLowerTriangularCorrelationMatrix(prefix + ".lower_triangular_correlation_matrix_columns",
                    object.lowerTriangularCorrelationMatrixColumns());
        }
        if (object.planarObjectDimension1() != null) {
            checkRange(prefix + ".planar_object_dimension_1", object.planarObjectDimension1(), 0, 1023);
        }
        if (object.planarObjectDimension2() != null) {
            checkRange(prefix + ".planar_object_dimension_2", object.planarObjectDimension2(), 0, 1023);
        }
        if (object.verticalObjectDimension() != null) {
            checkRange(prefix + ".vertical_object_dimension", object.verticalObjectDimension(), 0, 1023);
        }
        if (object.objectRefPoint() != null) {
            checkRange(prefix + ".object_ref_point", object.objectRefPoint(), 0, 8);
        }
        checkRange(prefix + ".object_age", object.objectAge(), 0, 1500);
        if (object.sensorIdList() != null) {
            int size = object.sensorIdList().size();
            if (size < 1 || size > 128) {
                throw new CpmValidationException(prefix + ".sensor_id_list size out of range [1,128]: " + size);
            }
            for (int i = 0; i < size; i++) {
                checkRange(prefix + ".sensor_id_list[" + i + "]", object.sensorIdList().get(i), 0, 255);
            }
        }
        if (object.dynamicStatus() != null) {
            checkRange(prefix + ".dynamic_status", object.dynamicStatus(), 0, 2);
        }
        if (object.classification() != null) {
            validateClassification(prefix + ".classification", object.classification());
        }
        if (object.matchedPosition() != null) {
            validateMapPosition(prefix + ".matched_position", object.matchedPosition());
        }
        validatePerceivedObjectConfidence(prefix + ".confidence", object.confidence());
    }

    private static void validateLowerTriangularCorrelationMatrix(String prefix, LowerTriangularCorrelationMatrix matrix) {
        requireNonNull(prefix, matrix);
        List<List<Integer>> columns = requireNonNull(prefix, matrix.columns());
        int size = columns.size();
        if (size < 1 || size > 17) {
            throw new CpmValidationException(prefix + " size out of range [1,17]: " + size);
        }
        for (int i = 0; i < size; i++) {
            List<Integer> column = requireNonNull(prefix + "[" + i + "]", columns.get(i));
            int columnSize = column.size();
            if (columnSize < 1 || columnSize > 17) {
                throw new CpmValidationException(prefix + "[" + i + "] size out of range [1,17]: " + columnSize);
            }
            for (int j = 0; j < columnSize; j++) {
                checkRange(prefix + "[" + i + "][" + j + "]", column.get(j), -100, 100);
            }
        }
    }

    private static void validatePerceivedObjectConfidence(String prefix, PerceivedObjectConfidence confidence) {
        requireNonNull(prefix, confidence);
        checkRange(prefix + ".x_distance", confidence.xDistance(), 0, 4095);
        checkRange(prefix + ".y_distance", confidence.yDistance(), 0, 4095);
        if (confidence.zDistance() != null) {
            checkRange(prefix + ".z_distance", confidence.zDistance(), 0, 4095);
        }
        checkRange(prefix + ".x_speed", confidence.xSpeed(), 0, 7);
        checkRange(prefix + ".y_speed", confidence.ySpeed(), 0, 7);
        if (confidence.zSpeed() != null) {
            checkRange(prefix + ".z_speed", confidence.zSpeed(), 0, 7);
        }
        if (confidence.xAcceleration() != null) {
            checkRange(prefix + ".x_acceleration", confidence.xAcceleration(), 0, 102);
        }
        if (confidence.yAcceleration() != null) {
            checkRange(prefix + ".y_acceleration", confidence.yAcceleration(), 0, 102);
        }
        if (confidence.zAcceleration() != null) {
            checkRange(prefix + ".z_acceleration", confidence.zAcceleration(), 0, 102);
        }
        if (confidence.rollAngle() != null) {
            checkRange(prefix + ".roll_angle", confidence.rollAngle(), 1, 127);
        }
        if (confidence.pitchAngle() != null) {
            checkRange(prefix + ".pitch_angle", confidence.pitchAngle(), 1, 127);
        }
        if (confidence.yawAngle() != null) {
            checkRange(prefix + ".yaw_angle", confidence.yawAngle(), 1, 127);
        }
        if (confidence.rollRate() != null) {
            checkRange(prefix + ".roll_rate", confidence.rollRate(), 0, 8);
        }
        if (confidence.pitchRate() != null) {
            checkRange(prefix + ".pitch_rate", confidence.pitchRate(), 0, 8);
        }
        if (confidence.yawRate() != null) {
            checkRange(prefix + ".yaw_rate", confidence.yawRate(), 0, 8);
        }
        if (confidence.rollAcceleration() != null) {
            checkRange(prefix + ".roll_acceleration", confidence.rollAcceleration(), 0, 8);
        }
        if (confidence.pitchAcceleration() != null) {
            checkRange(prefix + ".pitch_acceleration", confidence.pitchAcceleration(), 0, 8);
        }
        if (confidence.yawAcceleration() != null) {
            checkRange(prefix + ".yaw_acceleration", confidence.yawAcceleration(), 0, 8);
        }
        if (confidence.planarObjectDimension1() != null) {
            checkRange(prefix + ".planar_object_dimension_1", confidence.planarObjectDimension1(), 0, 102);
        }
        if (confidence.planarObjectDimension2() != null) {
            checkRange(prefix + ".planar_object_dimension_2", confidence.planarObjectDimension2(), 0, 102);
        }
        if (confidence.verticalObjectDimension() != null) {
            checkRange(prefix + ".vertical_object_dimension", confidence.verticalObjectDimension(), 0, 102);
        }
        if (confidence.longitudinalLanePosition() != null) {
            checkRange(prefix + ".longitudinal_lane_position", confidence.longitudinalLanePosition(), 0, 102);
        }
        checkRange(prefix + ".object", confidence.object(), 0, 15);
    }

    private static void validateClassification(String prefix, List<ObjectClassification> list) {
        int size = list.size();
        if (size < 1 || size > 8) {
            throw new CpmValidationException(prefix + " size out of range [1,8]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateObjectClassification(prefix + "[" + i + "]", list.get(i));
        }
    }

    private static void validateObjectClassification(String prefix, ObjectClassification classification) {
        requireNonNull(prefix, classification);
        validateObjectClass(prefix + ".object_class", classification.objectClass());
        checkRange(prefix + ".confidence", classification.confidence(), 0, 101);
    }

    private static void validateObjectClass(String prefix, ObjectClass objectClass) {
        requireNonNull(prefix, objectClass);
        int count = 0;
        if (objectClass.vehicle() != null) {
            count++;
            checkRange(prefix + ".vehicle", objectClass.vehicle(), 0, 255);
        }
        if (objectClass.singleVru() != null) {
            count++;
            validateObjectClassVru(prefix + ".single_vru", objectClass.singleVru());
        }
        if (objectClass.vruGroup() != null) {
            count++;
            validateObjectClassGroup(prefix + ".vru_group", objectClass.vruGroup());
        }
        if (objectClass.other() != null) {
            count++;
            checkRange(prefix + ".other", objectClass.other(), 0, 255);
        }
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one class option");
        }
    }

    private static void validateObjectClassVru(String prefix, ObjectClassVru vru) {
        requireNonNull(prefix, vru);
        int count = 0;
        if (vru.pedestrian() != null) {
            count++;
            checkRange(prefix + ".pedestrian", vru.pedestrian(), 0, 15);
        }
        if (vru.bicyclist() != null) {
            count++;
            checkRange(prefix + ".bicyclist", vru.bicyclist(), 0, 15);
        }
        if (vru.motorcylist() != null) {
            count++;
            checkRange(prefix + ".motorcylist", vru.motorcylist(), 0, 15);
        }
        if (vru.animal() != null) {
            count++;
            checkRange(prefix + ".animal", vru.animal(), 0, 15);
        }
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one VRU option");
        }
    }

    private static void validateObjectClassGroup(String prefix, ObjectClassGroup group) {
        requireNonNull(prefix, group);
        validateGroupType(prefix + ".group_type", group.groupType());
        checkRange(prefix + ".group_size", group.groupSize(), 0, 255);
        if (group.clusterId() != null) {
            checkRange(prefix + ".cluster_id", group.clusterId(), 0, 255);
        }
    }

    private static void validateGroupType(String prefix, GroupType type) {
        requireNonNull(prefix, type);
    }

    private static void validateMapPosition(String prefix, MapPosition position) {
        requireNonNull(prefix, position);
        if (position.laneId() != null) {
            checkRange(prefix + ".lane_id", position.laneId(), 0, 255);
        }
        if (position.longitudinalLanePosition() != null) {
            checkRange(prefix + ".longitudinal_lane_position", position.longitudinalLanePosition(), 0, 32767);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Free space addendum container                                          */
    /* --------------------------------------------------------------------- */

    private static void validateFreeSpaceAddendumContainer(FreeSpaceAddendumContainer container) {
        requireNonNull("free_space_addendum_container", container);
        List<FreeSpaceAddendum> addenda = requireNonNull("free_space_addendum_container", container.freeSpaceAddenda());
        int size = addenda.size();
        if (size < 1 || size > 128) {
            throw new CpmValidationException("free_space_addendum_container size out of range [1,128]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateFreeSpaceAddendum("free_space_addendum_container[" + i + "]", addenda.get(i));
        }
    }

    private static void validateFreeSpaceAddendum(String prefix, FreeSpaceAddendum addendum) {
        requireNonNull(prefix, addendum);
        validateFreeSpaceArea(prefix + ".free_space_area", addendum.freeSpaceArea());
        checkRange(prefix + ".free_space_confidence", addendum.freeSpaceConfidence(), 0, 101);
        if (addendum.sensorIdList() != null) {
            int size = addendum.sensorIdList().size();
            if (size < 1 || size > 128) {
                throw new CpmValidationException(prefix + ".sensor_id_list size out of range [1,128]: " + size);
            }
            for (int i = 0; i < size; i++) {
                checkRange(prefix + ".sensor_id_list[" + i + "]", addendum.sensorIdList().get(i), 0, 255);
            }
        }
    }

    private static void validateFreeSpaceArea(String prefix, FreeSpaceArea area) {
        requireNonNull(prefix, area);
        int count = 0;
        if (area.freeSpacePolygon() != null) {
            count++;
            validateAreaPolygon(prefix + ".free_space_polygon", area.freeSpacePolygon());
        }
        if (area.freeSpaceCircular() != null) {
            count++;
            validateAreaCircular(prefix + ".free_space_circular", area.freeSpaceCircular());
        }
        if (area.freeSpaceEllipse() != null) {
            count++;
            validateAreaEllipse(prefix + ".free_space_ellipse", area.freeSpaceEllipse());
        }
        if (area.freeSpaceRectangle() != null) {
            count++;
            validateAreaRectangle(prefix + ".free_space_rectangle", area.freeSpaceRectangle());
        }
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one area option");
        }
    }

    /* --------------------------------------------------------------------- */
    /* Shared helpers                                                         */
    /* --------------------------------------------------------------------- */

    private static void validateOffset(String prefix, Offset offset) {
        requireNonNull(prefix, offset);
        checkRange(prefix + ".x", offset.x(), -32768, 32767);
        checkRange(prefix + ".y", offset.y(), -32768, 32767);
        if (offset.z() != null) {
            checkRange(prefix + ".z", offset.z(), -32768, 32767);
        }
    }

    private static void validateAreaPolygon(String prefix, AreaPolygon polygon) {
        requireNonNull(prefix, polygon);
        List<Offset> offsets = requireNonNull(prefix, polygon.offsets());
        int size = offsets.size();
        if (size < 3 || size > 16) {
            throw new CpmValidationException(prefix + " size out of range [3,16]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateOffset(prefix + "[" + i + "]", offsets.get(i));
        }
    }

    private static void validateAreaCircular(String prefix, AreaCircular circular) {
        requireNonNull(prefix, circular);
        if (circular.nodeCenterPoint() != null) {
            validateOffset(prefix + ".node_center_point", circular.nodeCenterPoint());
        }
        checkRange(prefix + ".radius", circular.radius(), 0, 10000);
    }

    private static void validateAreaEllipse(String prefix, AreaEllipse ellipse) {
        requireNonNull(prefix, ellipse);
        if (ellipse.nodeCenterPoint() != null) {
            validateOffset(prefix + ".node_center_point", ellipse.nodeCenterPoint());
        }
        checkRange(prefix + ".semi_major_range_length", ellipse.semiMajorRangeLength(), 0, 10000);
        checkRange(prefix + ".semi_minor_range_length", ellipse.semiMinorRangeLength(), 0, 10000);
        checkRange(prefix + ".semi_major_range_orientation", ellipse.semiMajorRangeOrientation(), 0, 3601);
        if (ellipse.semiHeight() != null) {
            checkRange(prefix + ".semi_height", ellipse.semiHeight(), 0, 10000);
        }
    }

    private static void validateAreaRectangle(String prefix, AreaRectangle rectangle) {
        requireNonNull(prefix, rectangle);
        if (rectangle.nodeCenterPoint() != null) {
            validateOffset(prefix + ".node_center_point", rectangle.nodeCenterPoint());
        }
        checkRange(prefix + ".semi_major_range_length", rectangle.semiMajorRangeLength(), 0, 10000);
        checkRange(prefix + ".semi_minor_range_length", rectangle.semiMinorRangeLength(), 0, 10000);
        checkRange(prefix + ".semi_major_range_orientation", rectangle.semiMajorRangeOrientation(), 0, 3601);
        if (rectangle.semiHeight() != null) {
            checkRange(prefix + ".semi_height", rectangle.semiHeight(), 0, 10000);
        }
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CpmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void requireEnum(String field, String actual, List<String> allowed) {
        if (!allowed.contains(actual)) {
            throw new CpmValidationException(field + " must be one of " + allowed);
        }
    }

    private static void checkRange(String field, Integer value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CpmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, Long value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CpmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }
}
