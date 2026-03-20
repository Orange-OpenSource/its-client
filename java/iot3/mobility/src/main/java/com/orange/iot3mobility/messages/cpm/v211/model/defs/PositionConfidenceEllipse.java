/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * PositionConfidenceEllipse
 *
 * @param semiMajor Half of length of the major axis (semi-major axis). Unit: 0,01 m. doNotUse (0), outOfRange (4094),
 *                  unavailable (4095).
 * @param semiMinor Half of length of the minor axis (semi-minor axis). Unit: 0,01 m. doNotUse (0), outOfRange (4094),
 *                  unavailable (4095).
 * @param semiMajorOrientation Orientation of the ellipse major axis with respect to WGS84 North. Unit: 0,1 degrees.
 *                             wgs84North (0), wgs84East (900), wgs84South (1800), wgs84West (2700), doNotUse (3600),
 *                             unavailable (3601).
 */
public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {}
