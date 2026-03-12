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
