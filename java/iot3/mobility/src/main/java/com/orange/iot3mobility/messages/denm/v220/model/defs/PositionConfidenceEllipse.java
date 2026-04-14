/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.defs;

/**
 * PositionConfidenceEllipse - confidence ellipse.
 *
 * @param semiMajor Half of length of the major axis. Default: 4095
 * @param semiMinor Half of length of the minor axis. Default: 4095
 * @param semiMajorOrientation Orientation of the ellipse major axis. Default: 3601
 */
public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {
}
