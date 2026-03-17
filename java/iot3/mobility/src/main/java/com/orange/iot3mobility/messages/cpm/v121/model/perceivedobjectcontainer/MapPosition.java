package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Map-matched position.
 *
 * @param laneId Assigned index unique within the intersection. Value: [0..255].
 * @param longitudinalLanePosition Longitudinal offset along the matched lane. Unit: 0.1 meter.
 *                                 Value: zeroPointOneMeter(1).
 */
public record MapPosition(
        Integer laneId,
        Integer longitudinalLanePosition) {}
