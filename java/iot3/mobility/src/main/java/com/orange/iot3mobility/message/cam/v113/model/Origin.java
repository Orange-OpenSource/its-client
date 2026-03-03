package com.orange.iot3mobility.message.cam.v113.model;

public enum Origin {
    SELF("self"),
    GLOBAL_APPLICATION("global_application"),
    MEC_APPLICATION("mec_application"),
    ON_BOARD_APPLICATION("on_board_application");

    public final String value;

    Origin(String value) {
        this.value = value;
    }
}
