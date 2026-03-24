/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer;

/**
 * Position confidence ellipse.
 *
 * @param semiMajorConfidence Semi-major confidence. Value: oneCentimeter(1), outOfRange(4094), unavailable(4095).
 * @param semiMinorConfidence Semi-minor confidence. Value: oneCentimeter(1), outOfRange(4094), unavailable(4095).
 * @param semiMajorOrientation Semi-major orientation. Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900),
 *                             wgs84South(1800), wgs84West(2700), unavailable(3601).
 */
public record PositionConfidenceEllipse(
        int semiMajorConfidence,
        int semiMinorConfidence,
        int semiMajorOrientation) {}
