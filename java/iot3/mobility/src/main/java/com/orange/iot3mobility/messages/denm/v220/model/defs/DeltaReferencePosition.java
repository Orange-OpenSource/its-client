/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.defs;

/**
 * DeltaReferencePosition - delta reference position.
 *
 * @param deltaLatitude Unit: 0.1 microdegree. Default: 131072
 * @param deltaLongitude Unit: 0.1 microdegree. Default: 131072
 * @param deltaAltitude Unit: centimeter. Default: 12800
 */
public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {
}
