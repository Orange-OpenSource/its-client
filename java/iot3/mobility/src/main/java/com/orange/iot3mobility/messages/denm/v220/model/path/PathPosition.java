/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.path;

/**
 * DenmPathPosition220 - path position.
 *
 * @param latitude Latitude of the geographical point. Range: -900000000 to 900000001
 * @param longitude Longitude of the geographical point. Range: -1800000000 to 1800000001
 * @param altitude Unit: 0.01 meter. referenceEllipsoidSurface(0), oneCentimeter(1), unavailable(800001)
 */
public record PathPosition(
        int latitude,
        int longitude,
        int altitude) {
}
