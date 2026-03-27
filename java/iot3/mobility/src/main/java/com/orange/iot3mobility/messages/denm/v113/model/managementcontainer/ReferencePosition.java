/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.managementcontainer;

/**
 * ReferencePosition - event position.
 *
 * @param latitude Unit: 0.1 microdegree. oneMicrodegreeNorth (10), oneMicrodegreeSouth (-10), unavailable(900000001)
 * @param longitude Unit: 0.1 microdegree. oneMicrodegreeEast (10), oneMicrodegreeWest (-10), unavailable(1800000001)
 * @param altitude Unit: 0.01 meter. referenceEllipsoidSurface(0), oneCentimeter(1), unavailable(800001)
 */
public record ReferencePosition(
        int latitude,
        int longitude,
        int altitude) {
}
