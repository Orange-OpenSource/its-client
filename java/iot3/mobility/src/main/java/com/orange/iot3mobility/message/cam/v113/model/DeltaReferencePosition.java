/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v113.model;

/**
 * DeltaReferencePosition v1.1.3
 * <p>
 * Offset position of a detected event point with regards to the previous detected
 * event point {@link ReferencePosition}.
 *
 * @param deltaLatitude oneMicrodegreeNorth (10), oneMicrodegreeSouth (-10) , unavailable(131072)
 * @param deltaLongitude oneMicrodegreeEast (10), oneMicrodegreeWest (-10), unavailable(131072)
 * @param deltaAltitude oneCentimeterUp (1), oneCentimeterDown (-1), unavailable(12800)
 */
public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {}
