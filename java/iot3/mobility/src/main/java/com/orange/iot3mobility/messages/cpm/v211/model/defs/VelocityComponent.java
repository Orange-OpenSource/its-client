/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Component of the velocity vector and the associated confidence value.
 *
 * @param value Velocity component value. Unit: 0,01 m/s. negativeOutOfRange (-16383), outOfRange (16382),
 *              unavailable (16383).
 * @param confidence Speed confidence for this component. Unit: 0,01 m/s. outOfRange (126), unavailable (127).
 */
public record VelocityComponent(int value, int confidence) {}
