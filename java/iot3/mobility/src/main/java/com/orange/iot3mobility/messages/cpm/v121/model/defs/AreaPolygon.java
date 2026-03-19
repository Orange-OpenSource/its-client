/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.defs;

import java.util.List;

/**
 * Polygonal area.
 *
 * @param offsets Polygon as a list of {@link Offset} points. Size: [3..16].
 */
public record AreaPolygon(List<Offset> offsets) {}

