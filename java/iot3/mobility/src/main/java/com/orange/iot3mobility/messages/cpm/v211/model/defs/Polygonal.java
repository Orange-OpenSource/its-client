package com.orange.iot3mobility.messages.cpm.v211.model.defs;

import java.util.List;

/**
 * Polygonal shape definition.
 */
public record Polygonal(
        List<CartesianPosition3d> polygon,
        CartesianPosition3d shapeReferencePoint,
        Integer height) {}

