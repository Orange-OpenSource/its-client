package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * VRU cluster profiles.
 *
 * @param pedestrian Indicates that the cluster contains at least one pedestrian VRU.
 * @param bicyclist Indicates that the cluster contains at least one bicycle VRU.
 * @param motorcyclist Indicates that the cluster contains at least one motorcycle VRU.
 * @param animal Indicates that the cluster contains at least one animal VRU.
 */
public record ClusterProfiles(
        boolean pedestrian,
        boolean bicyclist,
        boolean motorcyclist,
        boolean animal) {}

