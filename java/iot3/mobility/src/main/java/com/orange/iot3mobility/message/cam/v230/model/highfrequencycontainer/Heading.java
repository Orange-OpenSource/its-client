package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * Heading v2.3.0
 *
 * @param value Heading value in a WGS84 co-ordinates system. Unit: 0.1 degree. wgs84North(0), wgs84East(900),
 *              wgs84South(1800), wgs84West(2700), unavailable(3601)
 * @param confidence Confidence of the heading value with a predefined confidence level.
 *                   equalOrWithinZeroPointOneDegree (1), equalOrWithinOneDegree (10), outOfRange(126), unavailable(127)
 */
public record Heading(int value, int confidence) {}
