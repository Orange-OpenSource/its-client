package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Elliptical shape definition.
 */
public record Elliptical(
        int semiMajorAxisLength,
        int semiMinorAxisLength,
        CartesianPosition3d shapeReferencePoint,
        Integer orientation,
        Integer height) {}

