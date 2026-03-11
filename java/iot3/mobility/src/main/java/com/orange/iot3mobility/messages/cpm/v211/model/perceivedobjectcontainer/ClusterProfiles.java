package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * VRU cluster profiles.
 */
public record ClusterProfiles(
        boolean pedestrian,
        boolean bicyclist,
        boolean motorcyclist,
        boolean animal) {}

