package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.LongitudinalLanePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.MapReference;

/**
 * Map-matched position of an object.
 *
 * @param mapReference Optional {@link MapReference} referencing MAPEM topology.
 * @param laneId Optional lane identifier (0..255).
 * @param connectionId Optional connection identifier (0..255).
 * @param longitudinalLanePosition Optional {@link LongitudinalLanePosition} along the lane.
 */
public record MapPosition(
        MapReference mapReference,
        Integer laneId,
        Integer connectionId,
        LongitudinalLanePosition longitudinalLanePosition) {}
