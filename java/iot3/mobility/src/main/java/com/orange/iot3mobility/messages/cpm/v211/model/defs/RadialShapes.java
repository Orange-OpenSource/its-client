/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

import java.util.List;

/**
 * Radial shapes list.
 *
 * @param refPointId Reference point identifier (0..255).
 * @param xCoordinate X offset of the reference point (-3094..1001).
 * @param yCoordinate Y offset of the reference point (-3094..1001).
 * @param zCoordinate Optional Z offset of the reference point (-3094..1001).
 * @param radialShapesList List of {@link Radial} shapes.
 */
public record RadialShapes(
        int refPointId,
        int xCoordinate,
        int yCoordinate,
        Integer zCoordinate,
        List<Radial> radialShapesList) {}
