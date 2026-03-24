/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Angle with confidence.
 * <p>
 * Angle value expressed in WGS84 with a confidence level.
 *
 * @param value Angle value in WGS84. Unit: 0,1 degrees. wgs84North (0), wgs84East (900), wgs84South (1800),
 *              wgs84West (2700), doNotUse (3600), unavailable (3601).
 * @param confidence Angle confidence. Unit: 0,1 degrees. outOfRange (126), unavailable (127).
 */
public record Angle(int value, int confidence) {}
