/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer;

/**
 * SteeringWheelAngle v2.3.0
 *
 * @param value Steering wheel angle value. Negative when the wheel is turned clockwise (i.e. to the right), positive
 *              when turned anti-clockwise (i.e. to the left). Unit: 1,5 degree. negativeOutOfRange (-511),
 *              positiveOutOfRange (511), unavailable (512)
 * @param confidence Confidence of steering wheel angle value. Unit: 1,5 degree. outOfRange (126), unavailable (127)
 */
public record SteeringWheelAngle(int value, int confidence) {}
