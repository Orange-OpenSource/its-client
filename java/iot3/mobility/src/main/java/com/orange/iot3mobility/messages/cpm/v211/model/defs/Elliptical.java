/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Elliptical shape definition.
 *
 * @param semiMajorAxisLength Half length of the major axis (0..4095).
 * @param semiMinorAxisLength Half length of the minor axis (0..4095).
 * @param shapeReferencePoint Optional {@link CartesianPosition3d} reference point.
 * @param orientation Optional. Orientation in WGS84. Unit: 0,1 degrees (0..3601).
 * @param height Optional height for a right elliptical cylinder (0..4095).
 */
public record Elliptical(
        int semiMajorAxisLength,
        int semiMinorAxisLength,
        CartesianPosition3d shapeReferencePoint,
        Integer orientation,
        Integer height) {}
