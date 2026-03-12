package com.orange.iot3mobility.messages.cpm.v121.model.defs;

import java.util.List;

/**
 * Polygonal area.
 *
 * @param offsets Polygon points. Size: [3..16].
 */
public record AreaPolygon(List<Offset> offsets) {}

