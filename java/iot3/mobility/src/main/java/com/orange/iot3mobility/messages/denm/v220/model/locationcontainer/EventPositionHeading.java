/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.locationcontainer;

/**
 * EventPositionHeading - event position heading with confidence.
 *
 * @param value Unit: 0.1 degree. wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700),
 *              doNotUse(3600), unavailable(3601)
 * @param confidence Unit: 0.1 degree. equalOrWithinZeroPointOneDegree (1), equalOrWithinOneDegree (10),
 *                   outOfRange(126), unavailable(127)
 */
public record EventPositionHeading(
        int value,
        int confidence) {
}
