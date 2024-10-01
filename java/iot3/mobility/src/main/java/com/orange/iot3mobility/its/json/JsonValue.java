/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

public class JsonValue {

    public enum Type {

        CAM("cam"),
        DENM("denm"),
        CPM("cpm");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Origin {

        SELF("self"),
        ROAD_USER("road_user"),
        GLOBAL_APPLICATION("global_application"),
        MEC_APPLICATION("mec_application"),
        ON_BOARD_APPLICATION("on_board_application"),
        ROADSIDE_CAMERA("roadside_camera"),
        ON_BOARD_CAMERA("onboard_camera"),
        TRAFFIC_ORCHESTRATOR("traffic_orchestrator"),
        DATA_FUSION("data_fusion");

        private final String value;

        Origin(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Version {

        CURRENT("1.0.0");

        private final String value;

        Version(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

}
