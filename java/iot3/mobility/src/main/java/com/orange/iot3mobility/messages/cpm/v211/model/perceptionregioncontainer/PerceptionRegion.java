package com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

import java.util.List;

/**
 * Perception region information.
 */
public record PerceptionRegion(
        int measurementDeltaTime,
        int perceptionRegionConfidence,
        Shape perceptionRegionShape,
        boolean shadowingApplies,
        List<Integer> sensorIdList,
        List<Integer> perceivedObjectIds) {}

