/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer;

/**
 * AccelerationComponent v2.3.0
 *
 * @param value Acceleration value. Unit: 0.1 m/s2. pointOneMeterPerSecSquaredForward(1),
 *              pointOneMeterPerSecSquaredBackward(-1), unavailable(161)
 * @param confidence Confidence of acceleration value. pointOneMeterPerSecSquared(1), outOfRange(101),
 *                   unavailable(102)
 */
public record AccelerationComponent(int value, int confidence) {}
