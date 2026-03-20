package com.orange.iot3mobility.messages.cpm.v211.model.defs;

import java.util.List;

/**
 * Polygonal shape definition.
 *
 * @param polygon List of {@link CartesianPosition3d} relative positions defining the polygon.
 * @param shapeReferencePoint Optional {@link CartesianPosition3d} reference point.
 * @param height Optional height for a right prism (0..4095).
 */
public record Polygonal(
        List<CartesianPosition3d> polygon,
        CartesianPosition3d shapeReferencePoint,
        Integer height) {}

