/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.defs;

/**
 * Altitude - value with confidence.
 *
 * @param value altitude value (Unit: 0.01 meter). Default: 800001
 * @param confidence altitude confidence. Default: 15
 */
public record Altitude(
        int value,
        int confidence) {
}
