package com.orange.iot3mobility.messages.cpm.v121.model.defs;

/**
 * Circular area.
 *
 * @param nodeCenterPoint Optional center offset.
 * @param radius Radius. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 */
public record AreaCircular(
        Offset nodeCenterPoint,
        int radius) {}

