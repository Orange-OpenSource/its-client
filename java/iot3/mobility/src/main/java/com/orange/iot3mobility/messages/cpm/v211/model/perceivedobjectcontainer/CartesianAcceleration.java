package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.AccelerationComponent;

/**
 * Acceleration vector in a cartesian coordinate system.
 */
public record CartesianAcceleration(
        AccelerationComponent xAcceleration,
        AccelerationComponent yAcceleration,
        AccelerationComponent zAcceleration) {}

