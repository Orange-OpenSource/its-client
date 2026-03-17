package com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer;

import java.util.List;

/**
 * Free space addendum.
 *
 * @param freeSpaceArea Free space area for which the confidence applies. {@link FreeSpaceArea}
 * @param freeSpaceConfidence Isotropic free space confidence for the area. Value: unknown(0), onePercent(1),
 *                            oneHundredPercent(100), unavailable(101).
 * @param sensorIdList List of sensor IDs which performed the measurement. Value: [0..255].
 * @param shadowingApplies Optional. True if simple shadowing mechanism applies within the area.
 */
public record FreeSpaceAddendum(
        FreeSpaceArea freeSpaceArea,
        int freeSpaceConfidence,
        List<Integer> sensorIdList,
        Boolean shadowingApplies) {}
