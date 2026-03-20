/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Acceleration component along with a confidence value.
 *
 * @param value Acceleration component value. Unit: 0,1 m/s2. negativeOutOfRange (-160), positiveOutOfRange (160),
 *              unavailable (161).
 * @param confidence Acceleration confidence. Unit: 0,1 m/s2. outOfRange (101), unavailable (102).
 */
public record AccelerationComponent(int value, int confidence) {}
