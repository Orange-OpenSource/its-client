/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.basiccontainer;

/**
 * PositionConfidenceEllipse v2.3.0
 *
 * @param semiMajor Half of length of the major axis, i.e. distance between the centre point and major axis point of
 *                  the position accuracy ellipse. Unit: 0,1 metre
 * @param semiMinor Half of length of the minor axis, i.e. distance between the centre point and minor axis point of
 *                  the position accuracy ellipse. Unit: 0,1 metre
 * @param semiMajorOrientation Angle value in degrees described in the WGS84 reference system with respect to the
 *                             WGS84 north. Unit: 0,1 degree. valueNotUsed (3600), unavailable (3601)
 */
public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {}
