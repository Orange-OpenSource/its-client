package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Coordinate along with a confidence value in a cartesian reference system.
 */
public record CartesianCoordinateWithConfidence(int value, int confidence) {}
