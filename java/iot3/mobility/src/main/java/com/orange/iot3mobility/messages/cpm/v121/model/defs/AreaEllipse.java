package com.orange.iot3mobility.messages.cpm.v121.model.defs;

/**
 * Elliptical area.
 *
 * @param nodeCenterPoint Optional center offset.
 * @param semiMajorRangeLength Major radius of the ellipse. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param semiMinorRangeLength Minor radius of the ellipse. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param semiMajorRangeOrientation Orientation of the semi major range length. Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param semiHeight Semi height. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 */
public record AreaEllipse(
        Offset nodeCenterPoint,
        int semiMajorRangeLength,
        int semiMinorRangeLength,
        int semiMajorRangeOrientation,
        Integer semiHeight) {}
