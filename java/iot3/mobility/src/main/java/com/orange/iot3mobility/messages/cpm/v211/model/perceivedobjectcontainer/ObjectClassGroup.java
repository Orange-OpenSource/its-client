/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

/**
 * Object class group or cluster.
 *
 * @param clusterBoundingBoxShape {@link Shape} defining the cluster bounding box.
 * @param clusterCardinalitySize Estimated number of VRUs in the group. unavailable (0), onlyLeader (1).
 * @param clusterId Optional identifier of a VRU cluster (0..255).
 * @param clusterProfiles Optional {@link ClusterProfiles} describing VRU profile types in the cluster.
 */
public record ObjectClassGroup(
        Shape clusterBoundingBoxShape,
        int clusterCardinalitySize,
        Integer clusterId,
        ClusterProfiles clusterProfiles) {}
