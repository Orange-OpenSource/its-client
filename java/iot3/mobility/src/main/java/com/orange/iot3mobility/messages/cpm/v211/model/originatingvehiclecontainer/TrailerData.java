package com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Trailer data entry.
 */
public record TrailerData(
        int refPointId,
        int hitchPointOffset,
        Angle hitchAngle,
        Integer frontOverhang,
        Integer rearOverhang,
        Integer trailerWidth) {}

