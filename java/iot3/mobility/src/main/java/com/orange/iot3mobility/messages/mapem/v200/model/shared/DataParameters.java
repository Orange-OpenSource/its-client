/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.shared;

/**
 * Static metadata on how a map fragment was produced.
 *
 * @param processMethod Optional. Method used to process or derive the map data.
 * @param processAgency Optional. Agency that processed the map data.
 * @param lastCheckedDate Optional. Date the data was last verified.
 * @param geoidUsed Optional. Geodetic datum / geoid model used.
 */
public record DataParameters(
        String processMethod,
        String processAgency,
        String lastCheckedDate,
        String geoidUsed) {
}

