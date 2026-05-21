/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

/**
 * Absolute lat/lon node point (0.1 micro-degree resolution).
 *
 * @param lat Latitude in units of 0.1 microdegree. Range: -900000000..900000001.
 * @param lon Longitude in units of 0.1 microdegree. Range: -1800000000..1800000001.
 */
public record NodeLatLon(int lat, int lon) {
}

