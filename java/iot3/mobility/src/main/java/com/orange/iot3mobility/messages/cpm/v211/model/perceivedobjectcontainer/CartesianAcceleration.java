package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.AccelerationComponent;

/**
 * Acceleration vector in a cartesian coordinate system.
 *
 * @param xAcceleration {@link AccelerationComponent} along the x-axis.
 * @param yAcceleration {@link AccelerationComponent} along the y-axis.
 * @param zAcceleration Optional. {@link AccelerationComponent} along the z-axis.
 */
public record CartesianAcceleration(
        AccelerationComponent xAcceleration,
        AccelerationComponent yAcceleration,
        AccelerationComponent zAcceleration) {}
