package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Radial shape definition.
 */
public record Radial(
        int range,
        int stationaryHorizontalOpeningAngleStart,
        int stationaryHorizontalOpeningAngleEnd,
        CartesianPosition3d shapeReferencePoint,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd) {}

