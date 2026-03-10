/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer;

/**
 * Speed v2.3.0
 *
 * @param value Speed value. Unit 0.01 m/s. standstill(0), oneCentimeterPerSec(1), unavailable(16383)
 * @param confidence Confidence of the speed value. equalOrWithinOneCentimeterPerSec(1),
 *                   equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127)
 */
public record Speed(int value, int confidence) {}
