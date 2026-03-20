package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Coordinate along with a confidence value in a cartesian reference system.
 *
 * @param value Coordinate value (mean of the distribution). Unit: 0,01 m. negativeOutOfRange (-131072),
 *              positiveOutOfRange (131071).
 * @param confidence Coordinate confidence. Unit: 0,01 m. outOfRange (4095), unavailable (4096).
 */
public record CartesianCoordinateWithConfidence(int value, int confidence) {}
