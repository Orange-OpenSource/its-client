package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.LongitudinalLanePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.MapReference;

/**
 * Map-matched position of an object.
 */
public record MapPosition(
        MapReference mapReference,
        Integer laneId,
        Integer connectionId,
        LongitudinalLanePosition longitudinalLanePosition) {}

