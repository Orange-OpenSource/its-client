/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.locationcontainer;

/**
 * LocationConfidence - confidence values.
 *
 * @param eventSpeed Optional. Unit: 0.01 m/s. equalOrWithinOneCentimeterPerSec(1),
 *                   equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127)
 * @param eventPositionHeading Optional. Unit: 0.1 degree. equalOrWithinZeroPointOneDegree (1),
 *                            equalOrWithinOneDegree (10), outOfRange(126), unavailable(127)
 */
public record LocationConfidence(
        Integer eventSpeed,
        Integer eventPositionHeading) {
}
