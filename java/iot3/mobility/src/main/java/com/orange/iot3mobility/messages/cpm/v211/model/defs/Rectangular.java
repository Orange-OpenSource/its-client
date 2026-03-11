package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Rectangular shape definition.
 */
public record Rectangular(
        CartesianPosition3d centerPoint,
        int semiLength,
        int semiBreadth,
        Integer orientation,
        Integer height) {}

