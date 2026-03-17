package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * VRU group class.
 *
 * @param groupType {@link GroupType}
 * @param groupSize Estimation of the number of VRUs in the group. Value: unavailable(0), onlyLeader(1).
 * @param clusterId Optional ID of the associated cluster. Value: [0..255].
 */
public record ObjectClassGroup(
        GroupType groupType,
        int groupSize,
        Integer clusterId) {}
