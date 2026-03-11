package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Position in a two or three-dimensional cartesian coordinate system with confidence.
 */
public record CartesianPosition3dWithConfidence(
        CartesianCoordinateWithConfidence xCoordinate,
        CartesianCoordinateWithConfidence yCoordinate,
        CartesianCoordinateWithConfidence zCoordinate) {}
