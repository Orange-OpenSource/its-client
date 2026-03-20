/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
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
