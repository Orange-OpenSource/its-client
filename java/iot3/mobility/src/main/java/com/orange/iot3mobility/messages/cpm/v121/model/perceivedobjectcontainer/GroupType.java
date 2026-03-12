package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Group type flags for VRU groups.
 *
 * @param pedestrian True if pedestrian group.
 * @param bicyclist True if bicyclist group.
 * @param motorcyclist True if motorcyclist group.
 * @param animal True if animal group.
 */
public record GroupType(
        boolean pedestrian,
        boolean bicyclist,
        boolean motorcyclist,
        boolean animal) {}
