/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.locationcontainer;

/**
 * EventSpeed - event speed with confidence.
 *
 * @param value Unit: 0.01 m/s. standstill(0), oneCentimeterPerSec(1), unavailable(16383)
 * @param confidence Unit: 0.01 m/s. equalOrWithinOneCentimeterPerSec(1), equalOrWithinOneMeterPerSec(100),
 *                   outOfRange(126), unavailable(127)
 */
public record EventSpeed(
        int value,
        int confidence) {
}
