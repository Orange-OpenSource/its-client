/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.managementcontainer;

/**
 * PositionConfidence - confidence object.
 *
 * @param positionConfidenceEllipse Optional. {@link PositionConfidenceEllipse}
 * @param altitude Optional. Confidence level for altitude. Range: 0-15 (unavailable=15)
 */
public record PositionConfidence(
        PositionConfidenceEllipse positionConfidenceEllipse,
        Integer altitude) {
}
