/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Longitudinal lane position.
 *
 * @param value Longitudinal offset along the matched lane. Unit: 0,1 m. outOfRange (32766), unavailable (32767).
 * @param confidence Longitudinal position confidence. Unit: 0,1 m. outOfRange (1022), unavailable (1023).
 */
public record LongitudinalLanePosition(int value, int confidence) {}

