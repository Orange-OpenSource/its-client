/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

public class JsonKey {

    public enum Header {

        TYPE("type"),
        CONTEXT("context"),
        ORIGIN("origin"),
        VERSION("version"),
        SOURCE_UUID("source_uuid"),
        DESTINATION_UUID("destination_uuid"),
        TIMESTAMP("timestamp"),
        MESSAGE_ID("message_id"),
        MESSAGE("message"),
        SIGNATURE("signature");

        private final String key;

        Header(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Cam {

        PROTOCOL_VERSION("protocol_version"),
        STATION_ID("station_id"),
        GENERATION_DELTA_TIME("generation_delta_time"),
        BASIC_CONTAINER("basic_container"),
        HIGH_FREQ_CONTAINER("high_frequency_container"),
        LOW_FREQ_CONTAINER("low_frequency_container");

        private final String key;

        Cam(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum BasicContainer {

        STATION_TYPE("station_type"),
        POSITION("reference_position");

        private final String key;

        BasicContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum HighFrequencyContainer {

        HEADING("heading"),
        SPEED("speed"),
        DRIVE_DIRECTION("drive_direction"),
        VEHICLE_LENGTH("vehicle_length"),
        VEHICLE_WIDTH("vehicle_width"),
        LONGITUDINAL_ACCELERATION("longitudinal_acceleration"),
        LATERAL_ACCELERATION("lateral_acceleration"),
        VERTICAL_ACCELERATION("vertical_acceleration"),
        YAW_RATE("yaw_rate"),
        LANE_POSITION("lane_position"),
        CURVATURE("curvature"),
        CURVATURE_CALCULATION_MODE("curvature_calculation_mode"),
        ACCELERATION_CONTROL("acceleration_control"),
        CONFIDENCE("confidence");

        private final String key;

        HighFrequencyContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum LowFrequencyContainer {

        VEHICLE_ROLE("vehicle_role"),
        EXTERIOR_LIGHTS("exterior_lights"),
        PATH_HISTORY("path_history");

        private final String key;

        LowFrequencyContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Position {

        LATITUDE("latitude"),
        LONGITUDE("longitude"),
        ALTITUDE("altitude"),
        CONFIDENCE("confidence");

        private final String key;

        Position(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum PathPosition {

        DELTA_LATITUDE("delta_latitude"),
        DELTA_LONGITUDE("delta_longitude"),
        DELTA_ALTITUDE("delta_altitude");

        private final String key;

        PathPosition(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum PathPoint {

        PATH_POSITION("path_position"),
        PATH_DELTA_TIME("path_delta_time");

        private final String key;

        PathPoint(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Denm {
        PROTOCOL_VERSION("protocol_version"),
        STATION_ID("station_id"),
        MANAGEMENT_CONTAINER("management_container"),
        SITUATION_CONTAINER("situation_container"),
        LOCATION_CONTAINER("location_container"),
        ALACARTE_CONTAINER("alacarte_container");

        private final String key;

        Denm(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ManagementContainer {
        ACTION_ID("action_id"),
        DETECTION_TIME("detection_time"),
        REFERENCE_TIME("reference_time"),
        TERMINATION("termination"),
        EVENT_POSITION("event_position"),
        RELEVANCE_DISTANCE("relevance_distance"),
        RELEVANCE_TRAFFIC_DIRECTION("relevance_traffic_direction"),
        VALIDITY_DURATION("validity_duration"),
        TRANSMISSION_INTERVAL("transmission_interval"),
        STATION_TYPE("station_type");

        private final String key;

        ManagementContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum ActionId {
        ORIGINATING_STATION_ID("originating_station_id"),
        SEQUENCE_NUMBER("sequence_number");

        private final String key;

        ActionId(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum SituationContainer {
        INFO_QUALITY("information_quality"),
        EVENT_TYPE("event_type"),
        LINKED_CAUSE("linked_cause"),
        EVENT_HISTORY("event_history");

        private final String key;

        SituationContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum EventType {
        CAUSE("cause"),
        SUBCAUSE("subcause");

        private final String key;

        EventType(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum LinkedCause {
        CAUSE("cause"),
        SUBCAUSE("subcause");

        private final String key;

        LinkedCause(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum LocationContainer {
        EVENT_SPEED("event_speed"),
        EVENT_POSITION_HEADING("event_position_heading"),
        TRACES("traces"),
        ROAD_TYPE("road_type"),
        CONFIDENCE("confidence");

        private final String key;

        LocationContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum AlacarteContainer {
        LANE_POSITION("lane_position"),
        IMPACT_REDUCTION("impact_reduction"),
        EXTERNAL_TEMPERATURE("external_temperature"),
        ROAD_WORKS("road_works"),
        POSITION_SOLUTION_TYPE("position_solution_type");

        private final String key;

        AlacarteContainer(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum Confidence {

        POSITION_SEMI_MAJOR_CONFIDENCE("semi_major_confidence"),
        POSITION_SEMI_MINOR_CONFIDENCE("semi_minor_confidence"),
        POSITION_SEMI_MAJOR_ORIENTATION("semi_major_orientation"),
        ALTITUDE("altitude"),
        HEADING("heading"),
        SPEED("speed"),
        SIZE("size"),
        VEHICLE_LENGTH("vehicle_length"),
        ACCELERATION("acceleration"),
        LONGITUDINAL_ACCELERATION("longitudinal_acceleration"),
        LATERAL_ACCELERATION("lateral_acceleration"),
        VERTICAL_ACCELERATION("vertical_acceleration"),
        YAW_RATE("yaw_rate"),
        CURVATURE("curvature"),
        EVENT_SPEED("event_speed"),
        EVENT_POSITION_HEADING("event_position_heading"),
        POSITION_CONFIDENCE_ELLIPSE("position_confidence_ellipse");

        private final String key;

        Confidence(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

}
