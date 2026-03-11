/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.validation;

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
import com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer.OriginatingRsuContainer;

import java.util.List;
import java.util.Objects;

/**
 * Static validation utility for CPM 2.1.1 envelope
 */
public final class CpmValidator211 {

    private static final long MIN_TIMESTAMP = 1514764800000L;   // 2018-01-01

    private CpmValidator211() {}

    public static void validateEnvelope(CpmEnvelope211 envelope) {
        requireNonNull("envelope", envelope);
        requireEquals("message_type", envelope.messageType(), "cpm");
        requireNotBlank("source_uuid", envelope.sourceUuid());
        checkMin("timestamp", envelope.timestamp(), MIN_TIMESTAMP);
        requireEquals("version", envelope.version(), "2.1.1");
        if (envelope.objectIdRotationCount() != null) {
            checkRange("object_id_rotation_count", envelope.objectIdRotationCount(), 0, 255);
        }

        validateMessage(requireNonNull("message", envelope.message()));
    }

    private static void validateMessage(CpmMessage211 message) {
        requireNonNull("message", message);
        checkRange("protocol_version", message.protocolVersion(), 0, 255);
        checkRange("station_id", message.stationId(), 0, 4294967295L);

        validateManagementContainer(message.managementContainer());

        if (message.originatingVehicleContainer() != null) {
            validateOriginatingVehicleContainer(message.originatingVehicleContainer());
        }
        if (message.originatingRsuContainer() != null) {
            validateOriginatingRsuContainer(message.originatingRsuContainer());
        }
        if (message.sensorInformationContainer() != null) {
            validateSensorInformationContainer(message.sensorInformationContainer());
        }
        if (message.perceptionRegionContainer() != null) {
            validatePerceptionRegionContainer(message.perceptionRegionContainer());
        }
        if (message.perceivedObjectContainer() != null) {
            validatePerceivedObjectContainer(message.perceivedObjectContainer());
        }
    }

    /* --------------------------------------------------------------------- */
    /* Management container                                                   */
    /* --------------------------------------------------------------------- */

    private static void validateManagementContainer(ManagementContainer container) {
        requireNonNull("management_container", container);
        checkRange("management_container.reference_time", container.referenceTime(), 0, 4398046511103L);
        validateReferencePosition(container.referencePosition());

        if (container.segmentationInfo() != null) {
            validateSegmentationInfo(container.segmentationInfo());
        }
        if (container.messageRateRange() != null) {
            validateMessageRateRange(container.messageRateRange());
        }
    }

    private static void validateReferencePosition(ReferencePosition reference) {
        requireNonNull("reference_position", reference);
        checkRange("reference_position.latitude", reference.latitude(), -900000000, 900000001);
        checkRange("reference_position.longitude", reference.longitude(), -1800000000, 1800000001);
        validatePositionConfidenceEllipse("reference_position.position_confidence_ellipse", reference.positionConfidenceEllipse());
        validateAltitude("reference_position.altitude", reference.altitude());
    }

    private static void validatePositionConfidenceEllipse(String prefix, PositionConfidenceEllipse ellipse) {
        requireNonNull(prefix, ellipse);
        checkRange(prefix + ".semi_major", ellipse.semiMajor(), 0, 4095);
        checkRange(prefix + ".semi_minor", ellipse.semiMinor(), 0, 4095);
        checkRange(prefix + ".semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
    }

    private static void validateAltitude(String prefix, Altitude altitude) {
        requireNonNull(prefix, altitude);
        checkRange(prefix + ".value", altitude.value(), -100000, 800001);
        checkRange(prefix + ".confidence", altitude.confidence(), 0, 15);
    }

    private static void validateSegmentationInfo(SegmentationInfo info) {
        requireNonNull("segmentation_info", info);
        checkRange("segmentation_info.total_msg_no", info.totalMsgNo(), 1, 8);
        checkRange("segmentation_info.this_msg_no", info.thisMsgNo(), 1, 8);
    }

    private static void validateMessageRateRange(MessageRateRange range) {
        requireNonNull("message_rate_range", range);
        validateMessageRateHz("message_rate_range.message_rate_min", range.messageRateMin());
        validateMessageRateHz("message_rate_range.message_rate_max", range.messageRateMax());
    }

    private static void validateMessageRateHz(String prefix, MessageRateHz rate) {
        requireNonNull(prefix, rate);
        checkRange(prefix + ".mantissa", rate.mantissa(), 1, 100);
        checkRange(prefix + ".exponent", rate.exponent(), -5, 2);
    }

    /* --------------------------------------------------------------------- */
    /* Originating vehicle container                                          */
    /* --------------------------------------------------------------------- */

    private static void validateOriginatingVehicleContainer(OriginatingVehicleContainer container) {
        requireNonNull("originating_vehicle_container", container);
        validateAngle("originating_vehicle_container.orientation_angle", container.orientationAngle());
        if (container.pitchAngle() != null) {
            validateAngle("originating_vehicle_container.pitch_angle", container.pitchAngle());
        }
        if (container.rollAngle() != null) {
            validateAngle("originating_vehicle_container.roll_angle", container.rollAngle());
        }
        if (container.trailerDataSet() != null) {
            for (int i = 0; i < container.trailerDataSet().size(); i++) {
                validateTrailerData("originating_vehicle_container.trailer_data_set[" + i + "]", container.trailerDataSet().get(i));
            }
        }
    }

    private static void validateTrailerData(String prefix, TrailerData trailer) {
        requireNonNull(prefix, trailer);
        checkRange(prefix + ".ref_point_id", trailer.refPointId(), 0, 255);
        checkRange(prefix + ".hitch_point_offset", trailer.hitchPointOffset(), 0, 255);
        validateAngle(prefix + ".hitch_angle", trailer.hitchAngle());
        if (trailer.frontOverhang() != null) {
            checkRange(prefix + ".front_overhang", trailer.frontOverhang(), 0, 255);
        }
        if (trailer.rearOverhang() != null) {
            checkRange(prefix + ".rear_overhang", trailer.rearOverhang(), 0, 255);
        }
        if (trailer.trailerWidth() != null) {
            checkRange(prefix + ".trailer_width", trailer.trailerWidth(), 1, 62);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Originating RSU container                                              */
    /* --------------------------------------------------------------------- */

    private static void validateOriginatingRsuContainer(OriginatingRsuContainer container) {
        requireNonNull("originating_rsu_container", container);
        List<MapReference> references = requireNonNull("originating_rsu_container", container.mapReferences());
        for (int i = 0; i < references.size(); i++) {
            validateMapReference("originating_rsu_container[" + i + "]", references.get(i));
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
        checkRange(prefix + ".sensor_type", info.sensorType(), 0, 31);
        if (info.perceptionRegionShape() != null) {
            validateShape(prefix + ".perception_region_shape", info.perceptionRegionShape());
        }
        if (info.perceptionRegionConfidence() != null) {
            checkRange(prefix + ".perception_region_confidence", info.perceptionRegionConfidence(), 1, 101);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Perception region container                                            */
    /* --------------------------------------------------------------------- */

    private static void validatePerceptionRegionContainer(PerceptionRegionContainer container) {
        requireNonNull("perception_region_container", container);
        List<PerceptionRegion> regions = requireNonNull("perception_region_container", container.perceptionRegions());
        int size = regions.size();
        if (size < 1 || size > 256) {
            throw new CpmValidationException("perception_region_container size out of range [1,256]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validatePerceptionRegion("perception_region_container[" + i + "]", regions.get(i));
        }
    }

    private static void validatePerceptionRegion(String prefix, PerceptionRegion region) {
        requireNonNull(prefix, region);
        checkRange(prefix + ".measurement_delta_time", region.measurementDeltaTime(), -2048, 2047);
        checkRange(prefix + ".perception_region_confidence", region.perceptionRegionConfidence(), 1, 101);
        validateShape(prefix + ".perception_region_shape", region.perceptionRegionShape());
        if (region.sensorIdList() != null) {
            int size = region.sensorIdList().size();
            if (size < 1 || size > 128) {
                throw new CpmValidationException(prefix + ".sensor_id_list size out of range [1,128]: " + size);
            }
            for (int i = 0; i < size; i++) {
                checkRange(prefix + ".sensor_id_list[" + i + "]", region.sensorIdList().get(i), 0, 255);
            }
        }
        if (region.perceivedObjectIds() != null) {
            int size = region.perceivedObjectIds().size();
            if (size > 255) {
                throw new CpmValidationException(prefix + ".perceived_object_ids size out of range [0,255]: " + size);
            }
            for (int i = 0; i < size; i++) {
                checkRange(prefix + ".perceived_object_ids[" + i + "]", region.perceivedObjectIds().get(i), 0, 65535);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* Perceived object container                                             */
    /* --------------------------------------------------------------------- */

    private static void validatePerceivedObjectContainer(PerceivedObjectContainer container) {
        requireNonNull("perceived_object_container", container);
        List<PerceivedObject> objects = requireNonNull("perceived_object_container", container.perceivedObjects());
        int size = objects.size();
        if (size > 255) {
            throw new CpmValidationException("perceived_object_container size out of range [0,255]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validatePerceivedObject("perceived_object_container[" + i + "]", objects.get(i));
        }
    }

    private static void validatePerceivedObject(String prefix, PerceivedObject object) {
        requireNonNull(prefix, object);
        checkRange(prefix + ".measurement_delta_time", object.measurementDeltaTime(), -2048, 2047);
        validateCartesianPosition3dWithConfidence(prefix + ".position", object.position());
        if (object.objectId() != null) {
            checkRange(prefix + ".object_id", object.objectId(), 0, 65535);
        }
        if (object.velocity() != null) {
            validateVelocity(prefix + ".velocity", object.velocity());
        }
        if (object.acceleration() != null) {
            validateAcceleration(prefix + ".acceleration", object.acceleration());
        }
        if (object.angles() != null) {
            validateEulerAngles(prefix + ".angles", object.angles());
        }
        if (object.zAngularVelocity() != null) {
            validateCartesianAngularVelocityComponent(prefix + ".z_angular_velocity", object.zAngularVelocity());
        }
        if (object.lowerTriangularCorrelationMatrices() != null) {
            validateLowerTriangularCorrelationMatrices(prefix + ".lower_triangular_correlation_matrices", object.lowerTriangularCorrelationMatrices());
        }
        if (object.objectDimensionZ() != null) {
            validateObjectDimension(prefix + ".object_dimension_z", object.objectDimensionZ());
        }
        if (object.objectDimensionY() != null) {
            validateObjectDimension(prefix + ".object_dimension_y", object.objectDimensionY());
        }
        if (object.objectDimensionX() != null) {
            validateObjectDimension(prefix + ".object_dimension_x", object.objectDimensionX());
        }
        if (object.objectAge() != null) {
            checkRange(prefix + ".object_age", object.objectAge(), -2048, 2047);
        }
        if (object.objectPerceptionQuality() != null) {
            checkRange(prefix + ".object_perception_quality", object.objectPerceptionQuality(), 0, 15);
        }
        if (object.sensorIdList() != null) {
            int size = object.sensorIdList().size();
            if (size < 1 || size > 128) {
                throw new CpmValidationException(prefix + ".sensor_id_list size out of range [1,128]: " + size);
            }
            for (int i = 0; i < size; i++) {
                checkRange(prefix + ".sensor_id_list[" + i + "]", object.sensorIdList().get(i), 0, 255);
            }
        }
        if (object.classification() != null) {
            validateClassification(prefix + ".classification", object.classification());
        }
        if (object.mapPosition() != null) {
            validateMapPosition(prefix + ".map_position", object.mapPosition());
        }
    }

    private static void validateCartesianPosition3dWithConfidence(String prefix, CartesianPosition3dWithConfidence position) {
        requireNonNull(prefix, position);
        validateCartesianCoordinateWithConfidence(prefix + ".x_coordinate", position.xCoordinate());
        validateCartesianCoordinateWithConfidence(prefix + ".y_coordinate", position.yCoordinate());
        if (position.zCoordinate() != null) {
            validateCartesianCoordinateWithConfidence(prefix + ".z_coordinate", position.zCoordinate());
        }
    }

    private static void validateCartesianCoordinateWithConfidence(String prefix, CartesianCoordinateWithConfidence coordinate) {
        requireNonNull(prefix, coordinate);
        checkRange(prefix + ".value", coordinate.value(), -131072, 131071);
        checkRange(prefix + ".confidence", coordinate.confidence(), 1, 4096);
    }

    private static void validateVelocity(String prefix, Velocity velocity) {
        requireNonNull(prefix, velocity);
        if (velocity.polarVelocity() != null && velocity.cartesianVelocity() != null) {
            throw new CpmValidationException(prefix + " must contain exactly one velocity option");
        }
        if (velocity.polarVelocity() != null) {
            validatePolarVelocity(prefix + ".polar_velocity", velocity.polarVelocity());
        } else if (velocity.cartesianVelocity() != null) {
            validateCartesianVelocity(prefix + ".cartesian_velocity", velocity.cartesianVelocity());
        } else {
            throw new CpmValidationException(prefix + " must contain a velocity option");
        }
    }

    private static void validatePolarVelocity(String prefix, PolarVelocity velocity) {
        requireNonNull(prefix, velocity);
        validateSpeed(prefix + ".velocity_magnitude", velocity.velocityMagnitude());
        validateAngle(prefix + ".velocity_direction", velocity.velocityDirection());
        if (velocity.zVelocity() != null) {
            validateVelocityComponent(prefix + ".z_velocity", velocity.zVelocity());
        }
    }

    private static void validateCartesianVelocity(String prefix, CartesianVelocity velocity) {
        requireNonNull(prefix, velocity);
        validateVelocityComponent(prefix + ".x_velocity", velocity.xVelocity());
        validateVelocityComponent(prefix + ".y_velocity", velocity.yVelocity());
        if (velocity.zVelocity() != null) {
            validateVelocityComponent(prefix + ".z_velocity", velocity.zVelocity());
        }
    }

    private static void validateAcceleration(String prefix, Acceleration acceleration) {
        requireNonNull(prefix, acceleration);
        if (acceleration.polarAcceleration() != null && acceleration.cartesianAcceleration() != null) {
            throw new CpmValidationException(prefix + " must contain exactly one acceleration option");
        }
        if (acceleration.polarAcceleration() != null) {
            validatePolarAcceleration(prefix + ".polar_acceleration", acceleration.polarAcceleration());
        } else if (acceleration.cartesianAcceleration() != null) {
            validateCartesianAcceleration(prefix + ".cartesian_acceleration", acceleration.cartesianAcceleration());
        } else {
            throw new CpmValidationException(prefix + " must contain an acceleration option");
        }
    }

    private static void validatePolarAcceleration(String prefix, PolarAcceleration acceleration) {
        requireNonNull(prefix, acceleration);
        validateAccelerationMagnitude(prefix + ".acceleration_magnitude", acceleration.accelerationMagnitude());
        validateAngle(prefix + ".acceleration_direction", acceleration.accelerationDirection());
        if (acceleration.zAcceleration() != null) {
            validateAccelerationComponent(prefix + ".z_acceleration", acceleration.zAcceleration());
        }
    }

    private static void validateAccelerationMagnitude(String prefix, AccelerationMagnitude magnitude) {
        requireNonNull(prefix, magnitude);
        checkRange(prefix + ".value", magnitude.value(), 0, 161);
        checkRange(prefix + ".confidence", magnitude.confidence(), 0, 102);
    }

    private static void validateCartesianAcceleration(String prefix, CartesianAcceleration acceleration) {
        requireNonNull(prefix, acceleration);
        validateAccelerationComponent(prefix + ".x_acceleration", acceleration.xAcceleration());
        validateAccelerationComponent(prefix + ".y_acceleration", acceleration.yAcceleration());
        if (acceleration.zAcceleration() != null) {
            validateAccelerationComponent(prefix + ".z_acceleration", acceleration.zAcceleration());
        }
    }

    private static void validateAccelerationComponent(String prefix, AccelerationComponent component) {
        requireNonNull(prefix, component);
        checkRange(prefix + ".value", component.value(), -160, 161);
        checkRange(prefix + ".confidence", component.confidence(), 0, 102);
    }

    private static void validateEulerAngles(String prefix, EulerAngles angles) {
        requireNonNull(prefix, angles);
        validateAngle(prefix + ".z_angle", angles.zAngle());
        if (angles.yAngle() != null) {
            validateAngle(prefix + ".y_angle", angles.yAngle());
        }
        if (angles.xAngle() != null) {
            validateAngle(prefix + ".x_angle", angles.xAngle());
        }
    }

    private static void validateCartesianAngularVelocityComponent(String prefix, CartesianAngularVelocityComponent component) {
        requireNonNull(prefix, component);
        checkRange(prefix + ".value", component.value(), -255, 256);
        checkRange(prefix + ".confidence", component.confidence(), 0, 7);
    }

    private static void validateLowerTriangularCorrelationMatrices(String prefix, List<LowerTriangularCorrelationMatrix> matrices) {
        requireNonNull(prefix, matrices);
        int size = matrices.size();
        if (size < 1 || size > 4) {
            throw new CpmValidationException(prefix + " size out of range [1,4]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateLowerTriangularCorrelationMatrix(prefix + "[" + i + "]", matrices.get(i));
        }
    }

    private static void validateLowerTriangularCorrelationMatrix(String prefix, LowerTriangularCorrelationMatrix matrix) {
        requireNonNull(prefix, matrix);
        validateLowerTriangularMatrixComponents(prefix + ".components_included_in_the_matrix", matrix.componentsIncludedInTheMatrix());
        validateMatrix(prefix + ".matrix", matrix.matrix());
    }

    private static void validateLowerTriangularMatrixComponents(String prefix, LowerTriangularMatrixComponents components) {
        requireNonNull(prefix, components);
    }

    private static void validateMatrix(String prefix, List<List<List<Integer>>> matrix) {
        requireNonNull(prefix, matrix);
        int size = matrix.size();
        if (size < 1 || size > 13) {
            throw new CpmValidationException(prefix + " size out of range [1,13]: " + size);
        }
        for (int i = 0; i < size; i++) {
            List<List<Integer>> column = matrix.get(i);
            if (column == null || column.isEmpty() || column.size() > 13) {
                throw new CpmValidationException(prefix + "[" + i + "] size out of range [1,13]");
            }
            for (int j = 0; j < column.size(); j++) {
                List<Integer> row = column.get(j);
                if (row == null || row.isEmpty() || row.size() > 13) {
                    throw new CpmValidationException(prefix + "[" + i + "][" + j + "] size out of range [1,13]");
                }
                for (int k = 0; k < row.size(); k++) {
                    checkRange(prefix + "[" + i + "][" + j + "][" + k + "]", row.get(k), -100, 101);
                }
            }
        }
    }

    private static void validateObjectDimension(String prefix, ObjectDimension dimension) {
        requireNonNull(prefix, dimension);
        checkRange(prefix + ".value", dimension.value(), 1, 256);
        checkRange(prefix + ".confidence", dimension.confidence(), 1, 32);
    }

    private static void validateClassification(String prefix, List<ObjectClassification> classification) {
        requireNonNull(prefix, classification);
        int size = classification.size();
        if (size < 1 || size > 8) {
            throw new CpmValidationException(prefix + " size out of range [1,8]: " + size);
        }
        for (int i = 0; i < size; i++) {
            validateObjectClassification(prefix + "[" + i + "]", classification.get(i));
        }
    }

    private static void validateObjectClassification(String prefix, ObjectClassification classification) {
        requireNonNull(prefix, classification);
        validateObjectClass(prefix + ".object_class", classification.objectClass());
        checkRange(prefix + ".confidence", classification.confidence(), 1, 101);
    }

    private static void validateObjectClass(String prefix, ObjectClass objectClass) {
        requireNonNull(prefix, objectClass);
        int count = 0;
        if (objectClass.vehicle() != null) count++;
        if (objectClass.vru() != null) count++;
        if (objectClass.group() != null) count++;
        if (objectClass.other() != null) count++;
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one class option");
        }
        if (objectClass.vehicle() != null) {
            checkRange(prefix + ".vehicle", objectClass.vehicle(), 0, 255);
        }
        if (objectClass.vru() != null) {
            validateObjectClassVru(prefix + ".vru", objectClass.vru());
        }
        if (objectClass.group() != null) {
            validateObjectClassGroup(prefix + ".group", objectClass.group());
        }
        if (objectClass.other() != null) {
            checkRange(prefix + ".other", objectClass.other(), 0, 255);
        }
    }

    private static void validateObjectClassVru(String prefix, ObjectClassVru vru) {
        requireNonNull(prefix, vru);
        int count = 0;
        if (vru.pedestrian() != null) count++;
        if (vru.bicyclistAndLightVruVehicle() != null) count++;
        if (vru.motorcylist() != null) count++;
        if (vru.animal() != null) count++;
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one VRU option");
        }
        if (vru.pedestrian() != null) {
            checkRange(prefix + ".pedestrian", vru.pedestrian(), 0, 15);
        }
        if (vru.bicyclistAndLightVruVehicle() != null) {
            checkRange(prefix + ".bicyclist_and_light_vru_vehicle", vru.bicyclistAndLightVruVehicle(), 0, 15);
        }
        if (vru.motorcylist() != null) {
            checkRange(prefix + ".motorcylist", vru.motorcylist(), 0, 15);
        }
        if (vru.animal() != null) {
            checkRange(prefix + ".animal", vru.animal(), 0, 15);
        }
    }

    private static void validateObjectClassGroup(String prefix, ObjectClassGroup group) {
        requireNonNull(prefix, group);
        validateShape(prefix + ".cluster_bounding_box_shape", group.clusterBoundingBoxShape());
        checkRange(prefix + ".cluster_cardinality_size", group.clusterCardinalitySize(), 0, 255);
        if (group.clusterId() != null) {
            checkRange(prefix + ".cluster_id", group.clusterId(), 0, 255);
        }
        if (group.clusterProfiles() != null) {
            validateClusterProfiles(prefix + ".cluster_profiles", group.clusterProfiles());
        }
    }

    private static void validateClusterProfiles(String prefix, ClusterProfiles profiles) {
        requireNonNull(prefix, profiles);
    }

    private static void validateMapPosition(String prefix, MapPosition mapPosition) {
        requireNonNull(prefix, mapPosition);
        if (mapPosition.mapReference() != null) {
            validateMapReference(prefix + ".map_reference", mapPosition.mapReference());
        }
        if (mapPosition.laneId() != null) {
            checkRange(prefix + ".lane_id", mapPosition.laneId(), 0, 255);
        }
        if (mapPosition.connectionId() != null) {
            checkRange(prefix + ".connection_id", mapPosition.connectionId(), 0, 255);
        }
        if (mapPosition.longitudinalLanePosition() != null) {
            validateLongitudinalLanePosition(prefix + ".longitudinal_lane_position", mapPosition.longitudinalLanePosition());
        }
    }

    private static void validateLongitudinalLanePosition(String prefix, LongitudinalLanePosition position) {
        requireNonNull(prefix, position);
        checkRange(prefix + ".value", position.value(), 0, 32767);
        checkRange(prefix + ".confidence", position.confidence(), 0, 1023);
    }

    /* --------------------------------------------------------------------- */
    /* CDD helpers                                                            */
    /* --------------------------------------------------------------------- */

    private static void validateAngle(String prefix, Angle angle) {
        requireNonNull(prefix, angle);
        checkRange(prefix + ".value", angle.value(), 0, 3601);
        checkRange(prefix + ".confidence", angle.confidence(), 1, 127);
    }

    private static void validateSpeed(String prefix, Speed speed) {
        requireNonNull(prefix, speed);
        checkRange(prefix + ".value", speed.value(), 0, 16383);
        checkRange(prefix + ".confidence", speed.confidence(), 1, 127);
    }

    private static void validateVelocityComponent(String prefix, VelocityComponent component) {
        requireNonNull(prefix, component);
        checkRange(prefix + ".value", component.value(), -16383, 16383);
        checkRange(prefix + ".confidence", component.confidence(), 1, 127);
    }

    private static void validateMapReference(String prefix, MapReference reference) {
        requireNonNull(prefix, reference);
        int count = 0;
        if (reference.roadSegment() != null) count++;
        if (reference.intersection() != null) count++;
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one map reference option");
        }
        if (reference.roadSegment() != null) {
            validateRoadSegment(prefix + ".road_segment", reference.roadSegment());
        }
        if (reference.intersection() != null) {
            validateIntersection(prefix + ".intersection", reference.intersection());
        }
    }

    private static void validateRoadSegment(String prefix, RoadSegment segment) {
        requireNonNull(prefix, segment);
        checkRange(prefix + ".id", segment.id(), 0, 65535);
        if (segment.region() != null) {
            checkRange(prefix + ".region", segment.region(), 0, 65535);
        }
    }

    private static void validateIntersection(String prefix, Intersection intersection) {
        requireNonNull(prefix, intersection);
        checkRange(prefix + ".id", intersection.id(), 0, 65535);
        if (intersection.region() != null) {
            checkRange(prefix + ".region", intersection.region(), 0, 65535);
        }
    }

    private static void validateShape(String prefix, Shape shape) {
        requireNonNull(prefix, shape);
        int count = 0;
        if (shape.rectangular() != null) count++;
        if (shape.circular() != null) count++;
        if (shape.polygonal() != null) count++;
        if (shape.elliptical() != null) count++;
        if (shape.radial() != null) count++;
        if (shape.radialShapes() != null) count++;
        if (count != 1) {
            throw new CpmValidationException(prefix + " must contain exactly one shape option");
        }
        if (shape.rectangular() != null) {
            validateRectangular(prefix + ".rectangular", shape.rectangular());
        }
        if (shape.circular() != null) {
            validateCircular(prefix + ".circular", shape.circular());
        }
        if (shape.polygonal() != null) {
            validatePolygonal(prefix + ".polygonal", shape.polygonal());
        }
        if (shape.elliptical() != null) {
            validateElliptical(prefix + ".elliptical", shape.elliptical());
        }
        if (shape.radial() != null) {
            validateRadial(prefix + ".radial", shape.radial());
        }
        if (shape.radialShapes() != null) {
            validateRadialShapes(prefix + ".radial_shapes", shape.radialShapes());
        }
    }

    private static void validateRectangular(String prefix, Rectangular rectangular) {
        requireNonNull(prefix, rectangular);
        if (rectangular.centerPoint() != null) {
            validateCartesianPosition3d(prefix + ".center_point", rectangular.centerPoint());
        }
        checkRange(prefix + ".semi_length", rectangular.semiLength(), 0, 102);
        checkRange(prefix + ".semi_breadth", rectangular.semiBreadth(), 0, 102);
        if (rectangular.orientation() != null) {
            checkRange(prefix + ".orientation", rectangular.orientation(), 0, 3601);
        }
        if (rectangular.height() != null) {
            checkRange(prefix + ".height", rectangular.height(), 0, 4095);
        }
    }

    private static void validateCircular(String prefix, Circular circular) {
        requireNonNull(prefix, circular);
        checkRange(prefix + ".radius", circular.radius(), 0, 4095);
        if (circular.shapeReferencePoint() != null) {
            validateCartesianPosition3d(prefix + ".shape_reference_point", circular.shapeReferencePoint());
        }
        if (circular.height() != null) {
            checkRange(prefix + ".height", circular.height(), 0, 4095);
        }
    }

    private static void validatePolygonal(String prefix, Polygonal polygonal) {
        requireNonNull(prefix, polygonal);
        requireNonNull(prefix + ".polygon", polygonal.polygon());
        for (int i = 0; i < polygonal.polygon().size(); i++) {
            validateCartesianPosition3d(prefix + ".polygon[" + i + "]", polygonal.polygon().get(i));
        }
        if (polygonal.shapeReferencePoint() != null) {
            validateCartesianPosition3d(prefix + ".shape_reference_point", polygonal.shapeReferencePoint());
        }
        if (polygonal.height() != null) {
            checkRange(prefix + ".height", polygonal.height(), 0, 4095);
        }
    }

    private static void validateElliptical(String prefix, Elliptical elliptical) {
        requireNonNull(prefix, elliptical);
        checkRange(prefix + ".semi_major_axis_length", elliptical.semiMajorAxisLength(), 0, 4095);
        checkRange(prefix + ".semi_minor_axis_length", elliptical.semiMinorAxisLength(), 0, 4095);
        if (elliptical.shapeReferencePoint() != null) {
            validateCartesianPosition3d(prefix + ".shape_reference_point", elliptical.shapeReferencePoint());
        }
        if (elliptical.orientation() != null) {
            checkRange(prefix + ".orientation", elliptical.orientation(), 0, 3601);
        }
        if (elliptical.height() != null) {
            checkRange(prefix + ".height", elliptical.height(), 0, 4095);
        }
    }

    private static void validateRadial(String prefix, Radial radial) {
        requireNonNull(prefix, radial);
        checkRange(prefix + ".range", radial.range(), 0, 4095);
        checkRange(prefix + ".stationary_horizontal_opening_angle_start", radial.stationaryHorizontalOpeningAngleStart(), 0, 3601);
        checkRange(prefix + ".stationary_horizontal_opening_angle_end", radial.stationaryHorizontalOpeningAngleEnd(), 0, 3601);
        if (radial.shapeReferencePoint() != null) {
            validateCartesianPosition3d(prefix + ".shape_reference_point", radial.shapeReferencePoint());
        }
        if (radial.verticalOpeningAngleStart() != null) {
            checkRange(prefix + ".vertical_opening_angle_start", radial.verticalOpeningAngleStart(), 0, 3601);
        }
        if (radial.verticalOpeningAngleEnd() != null) {
            checkRange(prefix + ".vertical_opening_angle_end", radial.verticalOpeningAngleEnd(), 0, 3601);
        }
    }

    private static void validateRadialShapes(String prefix, RadialShapes radialShapes) {
        requireNonNull(prefix, radialShapes);
        checkRange(prefix + ".ref_point_id", radialShapes.refPointId(), 0, 255);
        checkRange(prefix + ".x_coordinate", radialShapes.xCoordinate(), -3094, 1001);
        checkRange(prefix + ".y_coordinate", radialShapes.yCoordinate(), -3094, 1001);
        if (radialShapes.zCoordinate() != null) {
            checkRange(prefix + ".z_coordinate", radialShapes.zCoordinate(), -3094, 1001);
        }
        requireNonNull(prefix + ".radial_shapes_list", radialShapes.radialShapesList());
        for (int i = 0; i < radialShapes.radialShapesList().size(); i++) {
            validateRadial(prefix + ".radial_shapes_list[" + i + "]", radialShapes.radialShapesList().get(i));
        }
    }

    private static void validateCartesianPosition3d(String prefix, CartesianPosition3d position) {
        requireNonNull(prefix, position);
        checkRange(prefix + ".x_coordinate", position.xCoordinate(), -32768, 32767);
        checkRange(prefix + ".y_coordinate", position.yCoordinate(), -32768, 32767);
        if (position.zCoordinate() != null) {
            checkRange(prefix + ".z_coordinate", position.zCoordinate(), -32768, 32767);
        }
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static String requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CpmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void checkRange(String field, long value, long min, long max) {
        if (value < min || value > max) {
            throw new CpmValidationException(field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkMin(String field, long value, long min) {
        if (value < min) {
            throw new CpmValidationException(field + " inferior to min [" + min + "] (actual=" + value + ")");
        }
    }

    private static void checkStringLength(String field, String value, int min, int max) {
        requireNotBlank(field, value);
        int len = value.length();
        if (len < min || len > max) {
            throw new CpmValidationException(field + " length out of range [" + min + ", " + max + "] (actual=" + len + ")");
        }
    }
}
