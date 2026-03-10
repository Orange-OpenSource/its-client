/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.lowfrequencycontainer;

/**
 * DeltaReferencePosition v2.3.0
 * <p>
 * Geographical point position as a 3-dimensional offset position to a geographical reference point.
 *
 * @param deltaLatitude Delta latitude offset with regard to the latitude value of the reference position.
 *                      Unit 0,1 microdegree. negativeOutOfRange (-131071), positiveOutOfRange (131071),
 *                      unavailable (131072)
 * @param deltaLongitude Delta longitude offset with regard to the longitude value of the reference position.
 *                       Unit 0,1 microdegree. negativeOutOfRange (-131071), positiveOutOfRange (131071),
 *                       unavailable (131072)
 * @param deltaAltitude Delta altitude offset with regard to the altitude value of the reference position.
 *                      Unit: 0,01 metre. negativeOutOfRange (-12700), positiveOutOfRange (12799), unavailable (12800)
 */
public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {}
