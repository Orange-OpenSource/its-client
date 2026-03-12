/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model;

/**
 * Origin v1.2.1
 * <p>
 * The entity responsible for this message.
 * Value: self, global_application, mec_application, on_board_application.
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
