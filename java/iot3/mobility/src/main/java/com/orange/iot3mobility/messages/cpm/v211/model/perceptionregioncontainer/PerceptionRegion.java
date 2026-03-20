package com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

import java.util.List;

/**
 * Perception region information.
 *
 * @param measurementDeltaTime Difference between the time of estimation of the perception region and the reference
 *                             time. Unit: ms (-2048..2047).
 * @param perceptionRegionConfidence Perception confidence (1..101). 101 indicates unavailable.
 * @param perceptionRegionShape {@link Shape} describing the region.
 * @param shadowingApplies Indicates if the standard shadowing approach applies.
 * @param sensorIdList Optional list of sensor identifiers involved (1..128 items).
 * @param perceivedObjectIds Optional list of perceived object identifiers contained in the region (0..255 items).
 */
public record PerceptionRegion(
        int measurementDeltaTime,
        int perceptionRegionConfidence,
        Shape perceptionRegionShape,
        boolean shadowingApplies,
        List<Integer> sensorIdList,
        List<Integer> perceivedObjectIds) {}
