/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.managementcontainer;

/**
 * PositionConfidenceEllipse - confidence ellipse.
 *
 * @param semiMajorConfidence oneCentimeter(1), outOfRange(4094), unavailable(4095)
 * @param semiMinorConfidence oneCentimeter(1), outOfRange(4094), unavailable(4095)
 * @param semiMajorOrientation wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601)
 */
public record PositionConfidenceEllipse(
        int semiMajorConfidence,
        int semiMinorConfidence,
        int semiMajorOrientation) {
}
