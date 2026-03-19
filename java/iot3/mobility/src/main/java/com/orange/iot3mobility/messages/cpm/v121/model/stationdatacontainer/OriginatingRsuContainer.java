/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Originating RSU container.
 *
 * @param region Road regulator id. Value: [0..65535].
 * @param intersectionReferenceId Intersection id. Value: [0..65535].
 * @param roadSegmentReferenceId Road segment id. Value: [0..65535].
 */
public record OriginatingRsuContainer(
        Integer region,
        Integer intersectionReferenceId,
        Integer roadSegmentReferenceId) {}
