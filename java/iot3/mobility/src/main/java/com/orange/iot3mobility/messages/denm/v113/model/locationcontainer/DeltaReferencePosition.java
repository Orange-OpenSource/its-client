/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.locationcontainer;

/**
 * DeltaReferencePosition - delta reference position.
 *
 * @param deltaLatitude Unit: 0.1 microdegree. oneMicrodegreeNorth (10), oneMicrodegreeSouth (-10),
 *                     unavailable(131072)
 * @param deltaLongitude Unit: 0.1 microdegree. oneMicrodegreeEast (10), oneMicrodegreeWest (-10),
 *                      unavailable(131072)
 * @param deltaAltitude Unit: centimeter. oneCentimeterUp (1), oneCentimeterDown (-1), unavailable(12800)
 */
public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {
}
