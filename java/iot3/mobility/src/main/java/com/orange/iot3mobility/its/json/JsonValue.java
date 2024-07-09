/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

/**
 * Created by Mathieu Lefebvre on 18/01/18.
 * Feel free to improve it
 */

public class JsonValue {

    public enum Type {

        CAM("cam"),
        DENM("denm"),
        CPM("cpm"),
        RU_DESCRIPTION("ru_description"),
        MANEUVER("maneuver"),
        MANEUVER_FEEDBACK("maneuver_feedback"),
        COLLISION_ALERT("collision_alert"),
        PING("ping");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Context {

        LANE_MERGE("lane_merge"),
        SEE_THROUGH("see_through"),
        COOPERATIVE_PERCEPTION("cooperative_perception"),
        VULNERABLE_ROAD_USER("vru_protection"),
        ALL("+"),
        GENERAL("general"),
        EVERYTHING("#"),
        UNDEFINED("undefined");

        private String value;

        Context(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Origin {

        OBU("obu"),
        SELF("self"),
        GLOBAL_APPLICATION("global_application"),
        MEC_APPLICATION("mec_application"),
        ON_BOARD_APPLICATION("on_board_application"),
        ROADSIDE_CAMERA("roadside_camera"),
        ON_BOARD_CAMERA("onboard_camera"),
        TRAFFIC_ORCHESTRATOR("traffic_orchestrator"),
        DATA_FUSION("data_fusion"),
        GDM("gdm"),
        LOCATION_SERVER("location_server"),
        COLLISION_SERVER("collision_server"),
        CAN_GATEWAY("can_gateway"),
        V2X_GATEWAY("v2x_gateway");

        private String value;

        Origin(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Version {

        CURRENT("1.0.0"),
        ONE_ONE_ZERO("1.1.0"),
        BETA("1.0.0"),
        ALPHA("0.1.0");

        private String value;

        Version(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum ItsStationType {

        UNKNOWN("unknown", 0),
        PEDESTRIAN("pedestrian", 1),
        CYCLIST("cyclist", 2),
        MOPED("moped", 3),
        MOTORCYCLE("motorcycle", 4),
        PASSENGER_CAR("passengerCar", 5),
        BUS("bus", 6),
        LIGHT_TRUCK("lightTruck", 7),
        HEAVY_TRUCK("heavyTruck", 8),
        TRAILER("trailer", 9),
        SPECIAL_VEHICLES("specialVehicles", 10),
        TRAM("tram", 11),
        ROAD_SIDE_UNIT("roadSideUnit", 15);

        private String value;
        private int etsiId;

        ItsStationType(String value, int etsiId) {
            this.value = value;
            this.etsiId = etsiId;
        }

        public String value() {
            return value;
        }

        public int etsiId() {
            return etsiId;
        }

        public static ItsStationType fromValue(String value) {
            for (ItsStationType itsStationType : ItsStationType.values()) {
                if (itsStationType.value().equals(value)) {
                    return itsStationType;
                }
            }
            return UNKNOWN;
        }

        public static ItsStationType fromEtsiId(int etsiId) {
            for (ItsStationType itsStationType : ItsStationType.values()) {
                if (itsStationType.etsiId() == etsiId) {
                    return itsStationType;
                }
            }
            return UNKNOWN;
        }
    }

    public enum PositionType {
        RAW_GNSS("raw_gnss"),
        MAP_MATCHED("map_matched"),
        DETECTED_VEHICLE_CAMERA("detected_vehicle_camera"),
        DETECTED_STATIC_CAMERA("detected_static_camera"),
        INTERPOLATED("interpolated");

        private String value;

        PositionType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static PositionType fromValue(String value) {
            for (PositionType positionType : PositionType.values()) {
                if (positionType.value.equals(value)) {
                    return positionType;
                }
            }
            return RAW_GNSS;
        }
    }

    public enum Connected {
        TRUE(true),
        FALSE(false);

        private boolean value;

        Connected(boolean value) {
            this.value = value;
        }

        public boolean value() {
            return value;
        }
    }

    public enum Feedback {

        ACCEPT("accept"),
        ABORT("abort"),
        CHECKPOINT("checkpoint"),
        REFUSE("refuse");

        private String value;

        Feedback(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Reason {

        REFUSE("refuse"),
        MANEUVER_BEHIND("maneuver_behind"),
        DISTANCE_FRONT("distance_front"),
        DISTANCE_REAR("distance_rear"),
        ACCELERATION("acceleration"),
        DECCELERATION("decceleration"),
        GAP_SIZE("gap_size");

        private String value;

        Reason(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

}
