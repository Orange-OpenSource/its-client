/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * Curvature v2.3.0
 *
 * @param value Curvature of the vehicle trajectory. Unit: 1 / turn radius in meters * 10000.
 *              outOfRangeNegative (-1023), straight (0), outOfRangePositive (1022), unavailable (1023)
 * @param confidence Confidence of the curvature value with a predefined confidence level. onePerMeter-0-00002 (0),
 *                   onePerMeter-0-0001 (1), onePerMeter-0-0005 (2), onePerMeter-0-002 (3), onePerMeter-0-01 (4),
 *                   onePerMeter-0-1 (5), outOfRange (6), unavailable (7)
 */
public record Curvature(int value, int confidence) {}
