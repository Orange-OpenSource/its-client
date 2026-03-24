/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Circular shape definition.
 *
 * @param radius Radius of the circular area (0..4095).
 * @param shapeReferencePoint Optional {@link CartesianPosition3d} reference point.
 * @param height Optional height for a right circular cylinder (0..4095).
 */
public record Circular(
        int radius,
        CartesianPosition3d shapeReferencePoint,
        Integer height) {}

