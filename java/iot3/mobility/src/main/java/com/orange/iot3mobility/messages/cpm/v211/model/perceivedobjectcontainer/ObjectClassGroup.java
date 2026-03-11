package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

/**
 * Object class group or cluster.
 */
public record ObjectClassGroup(
        Shape clusterBoundingBoxShape,
        int clusterCardinalitySize,
        Integer clusterId,
        ClusterProfiles clusterProfiles) {}

