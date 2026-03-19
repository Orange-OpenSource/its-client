/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
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
