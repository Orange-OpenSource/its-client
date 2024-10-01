/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

public class JsonCpmKey {

    public enum Cpm {
        PROTOCOL_VERSION("protocol_version"),
        STATION_ID("station_id"),
        MESSAGE_ID("message_id"),
        GENERATION_DELTA_TIME("generation_delta_time"),
        MANAGEMENT_CONTAINER("management_container"),
        STATION_DATA_CONTAINER("station_data_container"),
        ORIGINATING_RSU_CONTAINER("originating_rsu_container"),
        SENSOR_INFORMATION_CONTAINER("sensor_information_container"),
        PERCEIVED_OBJECT_CONTAINER("perceived_object_container"),
        NUMBER_OF_PERCEIVED_OBJECTS("number_of_perceived_objects");

        private String key;

        Cpm(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ManagementContainer {
        STATION_TYPE("station_type"),
        REFERENCE_POSITION("reference_position"),
        CONFIDENCE("confidence");

        private String key;

        ManagementContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum StationDataContainer {

        ORIGINATING_VEHICLE_CONTAINER("originating_vehicle_container"),
        ORIGINATING_RSU_CONTAINER("originating_rsu_container");

        private String key;

        StationDataContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum OriginatingVehicleContainer {

        HEADING("heading"),
        SPEED("speed"),
        DRIVE_DIRECTION("drive_direction"),
        VEHICLE_LENGTH("vehicle_length"),
        VEHICLE_WIDTH("vehicle_width"),
        LONGITUDINAL_ACCELERATION("longitudinal_acceleration"),
        YAW_RATE("yaw_rate"),
        LATERAL_ACCELERATION("lateral_acceleration"),
        VERTICAL_ACCELERATION("vertical_acceleration"),
        CONFIDENCE("confidence");

        private String key;

        OriginatingVehicleContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum OriginatingRsuContainer {

        REGION("region"),
        INTERSECTION_REFERENCE_ID("intersection_reference_id"),
        ROAD_SEGMENT_REFERENCE_ID("road_segment_reference_id");

        private String key;

        OriginatingRsuContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum IntersectionReferenceId {

        ROAD_REGULATOR_ID("road_regulator_id"),
        INTERSECTION_ID("intersection_id");

        private String key;

        IntersectionReferenceId(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum SensorInformationContainer {

        SENSOR_ID("sensor_id"),
        TYPE("type"),
        DETECTION_AREA("detection_area");

        private String key;

        SensorInformationContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum DetectionArea {

        VEHICLE_SENSOR("vehicle_sensor"),
        STATIONARY_SENSOR_RADIAL("stationary_sensor_radial"),
        STATIONARY_SENSOR_POLYGON("stationary_sensor_polygon"),
        STATIONARY_SENSOR_CIRCULAR("stationary_sensor_circular"),
        STATIONARY_SENSOR_ELLIPSE("stationary_sensor_ellipse"),
        STATIONARY_SENSOR_RECTANGLE("stationary_sensor_rectangle");

        private String key;

        DetectionArea(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum VehicleSensor {

        REF_POINT_ID("ref_point_id"),
        X_SENSOR_OFFSET("x_sensor_offset"),
        Y_SENSOR_OFFSET("y_sensor_offset"),
        Z_SENSOR_OFFSET("z_sensor_offset"),
        VEHICLE_SENSOR_PROPERTY_LIST("vehicle_sensor_property_list");

        private String key;

        VehicleSensor(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum VehicleSensorProperty {

        RANGE("range"),
        HORIZONTAL_OPENING_ANGLE_START("horizontal_opening_angle_start"),
        HORIZONTAL_OPENING_ANGLE_END("horizontal_opening_angle_end"),
        VERTICAL_OPENING_ANGLE_START("vertical_opening_angle_start"),
        VERTICAL_OPENING_ANGLE_END("vertical_opening_angle_end");

        private String key;

        VehicleSensorProperty(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum StationarySensorRadial {

        RANGE("range"),
        HORIZONTAL_OPENING_ANGLE_START("horizontal_opening_angle_start"),
        HORIZONTAL_OPENING_ANGLE_END("horizontal_opening_angle_end"),
        VERTICAL_OPENING_ANGLE_START("vertical_opening_angle_start"),
        VERTICAL_OPENING_ANGLE_END("vertical_opening_angle_end"),
        SENSOR_POSITION_OFFSET("sensor_position_offset"),
        SENSOR_HEIGHT("sensor_height");

        private String key;

        StationarySensorRadial(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum StationarySensorCircular {

        NODE_CENTER_POINT("node_center_point"),
        RADIUS("radius");

        private String key;

        StationarySensorCircular(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum StationarySensorEllipseRect {

        NODE_CENTER_POINT("node_center_point"),
        SEMI_MAJOR_RANGE_LENGTH("semi_major_range_length"),
        SEMI_MINOR_RANGE_LENGTH("semi_minor_range_length"),
        SEMI_MAJOR_RANGE_ORIENTATION("semi_major_range_orientation"),
        SEMI_HEIGHT("semi_height");

        private String key;

        StationarySensorEllipseRect(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum PerceivedObjectContainer {

        OBJECT("object"),
        OBJECT_ID("object_id"),
        TIME_OF_MEASUREMENT("time_of_measurement"),
        OBJECT_CONFIDENCE("object_confidence"),
        CONFIDENCE("confidence"),
        DISTANCE("distance"),
        DISTANCE_CONFIDENCE("distance_confidence"),
        SPEED("speed"),
        SPEED_CONFIDENCE("speed_confidence"),
        OBJECT_REF_POINT("object_ref_point"),
        OBJECT_AGE("object_age"),
        SENSOR_ID_LIST("sensor_id_list"),
        DYNAMIC_STATUS("dynamic_status"),
        CLASSIFICATION("classification");

        private String key;

        PerceivedObjectContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectDistance {

        X_DISTANCE("x_distance"),
        Y_DISTANCE("y_distance"),
        Z_DISTANCE("z_distance");

        private String key;

        ObjectDistance(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectAngle {

        ROLL_ANGLE("roll_angle"),
        PITCH_ANGLE("pitch_angle"),
        YAW_ANGLE("yaw_angle");

        private String key;

        ObjectAngle(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectSpeed {

        X_SPEED("x_speed"),
        Y_SPEED("y_speed"),
        Z_SPEED("z_speed"),
        ROLL_RATE("roll_rate"),
        PITCH_RATE("pitch_rate"),
        YAW_RATE("yaw_rate");

        private String key;

        ObjectSpeed(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectAcceleration {

        X_ACCELERATION("x_acceleration"),
        Y_ACCELERATION("y_acceleration"),
        Z_ACCELERATION("z_acceleration"),
        ROLL_ACCELERATION("roll_acceleration"),
        PITCH_ACCELERATION("pitch_acceleration"),
        YAW_ACCELERATION("yaw_acceleration");

        private String key;

        ObjectAcceleration(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectDimension {

        PLANAR_DIMENSION_1("planar_object_dimension_1"),
        PLANAR_DIMENSION_2("planar_object_dimension_2"),
        VERTICAL_DIMENSION("vertical_object_dimension");

        private String key;

        ObjectDimension(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectLanePosition {

        LONGITUDINAL_LANE_POSITION("longitudinal_lane_position");

        private String key;

        ObjectLanePosition(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Classification {

        OBJECT_CLASS("object_class"),
        CONFIDENCE("confidence");

        private String key;

        Classification(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ObjectClass {

        VEHICLE("vehicle"),
        SINGLE_VRU("single_vru"),
        PEDESTRIAN("pedestrian"),
        BICYCLIST("bicyclist"),
        MOTORCYLCIST("motorcyclist"),
        ANIMAL("animal"),
        VRU_GROUP("vru_group"),
        OTHER("other");

        private String key;

        ObjectClass(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Offset {

        X("x"),
        Y("y"),
        Z("z");

        private String key;

        Offset(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

}
