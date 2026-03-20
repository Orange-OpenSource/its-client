package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Position in a two or three-dimensional cartesian coordinate system with confidence.
 *
 * @param xCoordinate {@link CartesianCoordinateWithConfidence} for the x-axis.
 * @param yCoordinate {@link CartesianCoordinateWithConfidence} for the y-axis.
 * @param zCoordinate Optional. {@link CartesianCoordinateWithConfidence} for the z-axis.
 */
public record CartesianPosition3dWithConfidence(
        CartesianCoordinateWithConfidence xCoordinate,
        CartesianCoordinateWithConfidence yCoordinate,
        CartesianCoordinateWithConfidence zCoordinate) {}
