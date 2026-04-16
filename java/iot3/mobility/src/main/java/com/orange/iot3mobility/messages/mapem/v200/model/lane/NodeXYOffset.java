/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

/**
 * X/Y offset (in centimetres) from the previous node point in a lane path.
 *
 * @param x X-axis offset in cm. Range: -32767..32767 (offset_b16).
 * @param y Y-axis offset in cm. Range: -32767..32767 (offset_b16).
 */
public record NodeXYOffset(int x, int y) {
}

