package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Circular shape definition.
 */
public record Circular(
        int radius,
        CartesianPosition3d shapeReferencePoint,
        Integer height) {}

