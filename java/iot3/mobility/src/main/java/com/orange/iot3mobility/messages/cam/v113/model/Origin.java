/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.model;

/**
 * Origin v1.1.3
 * <p>
 * The entity responsible for this message.
 */
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
