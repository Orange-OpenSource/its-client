package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * Speed v2.3.0
 *
 * @param value Speed value. Unit 0.01 m/s. standstill(0), oneCentimeterPerSec(1), unavailable(16383)
 * @param confidence Confidence of the speed value. equalOrWithinOneCentimeterPerSec(1),
 *                   equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127)
 */
public record Speed(int value, int confidence) {}
