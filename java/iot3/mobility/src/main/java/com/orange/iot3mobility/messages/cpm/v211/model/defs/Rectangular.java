package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Rectangular shape definition.
 *
 * @param centerPoint Optional {@link CartesianPosition3d} center point.
 * @param semiLength Half the length of the rectangle (0..102).
 * @param semiBreadth Half the breadth of the rectangle (0..102).
 * @param orientation Optional. Orientation in WGS84. Unit: 0,1 degrees (0..3601).
 * @param height Optional height for a right rectangular prism (0..4095).
 */
public record Rectangular(
        CartesianPosition3d centerPoint,
        int semiLength,
        int semiBreadth,
        Integer orientation,
        Integer height) {}
