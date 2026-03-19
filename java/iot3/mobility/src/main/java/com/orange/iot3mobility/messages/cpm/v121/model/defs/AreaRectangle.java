/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.defs;

/**
 * Rectangular area.
 *
 * @param nodeCenterPoint Optional center offset.
 * @param semiMajorRangeLength Half length of the rectangle. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param semiMinorRangeLength Half width of the rectangle. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param semiMajorRangeOrientation Orientation of the semi major range length. Unit: 0.1 degree. Value: wgs84North(0),
 *                                  wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param semiHeight Optional semi height. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 */
public record AreaRectangle(
        Offset nodeCenterPoint,
        int semiMajorRangeLength,
        int semiMinorRangeLength,
        int semiMajorRangeOrientation,
        Integer semiHeight) {}
