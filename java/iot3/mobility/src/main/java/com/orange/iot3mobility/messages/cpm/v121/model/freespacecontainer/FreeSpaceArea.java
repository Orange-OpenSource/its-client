package com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaPolygon;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaRectangle;

/**
 * Free space area definition.
 *
 * Exactly one of the free space area options should be provided.
 *
 * @param freeSpacePolygon {@link AreaPolygon}
 * @param freeSpaceCircular {@link AreaCircular}
 * @param freeSpaceEllipse {@link AreaEllipse}
 * @param freeSpaceRectangle {@link AreaRectangle}
 */
public record FreeSpaceArea(
        AreaPolygon freeSpacePolygon,
        AreaCircular freeSpaceCircular,
        AreaEllipse freeSpaceEllipse,
        AreaRectangle freeSpaceRectangle) {}
