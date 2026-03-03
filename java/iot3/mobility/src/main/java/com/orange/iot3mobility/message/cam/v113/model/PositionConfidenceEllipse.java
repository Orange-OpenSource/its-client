package com.orange.iot3mobility.message.cam.v113.model;

/**
 * PositionConfidenceEllipse v1.1.3
 *
 * @param semiMajor oneCentimeter(1), outOfRange(4094), unavailable(4095)
 * @param semiMinor oneCentimeter(1), outOfRange(4094), unavailable(4095)
 * @param semiMajorOrientation wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601)
 */
public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {}
