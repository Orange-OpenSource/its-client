package com.orange.iot3mobility.messages.cpm.v211.model.defs;

import java.util.List;

/**
 * Radial shapes list.
 */
public record RadialShapes(
        int refPointId,
        int xCoordinate,
        int yCoordinate,
        Integer zCoordinate,
        List<Radial> radialShapesList) {}

