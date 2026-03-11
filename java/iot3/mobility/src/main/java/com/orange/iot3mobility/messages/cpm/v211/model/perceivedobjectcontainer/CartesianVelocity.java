package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent;

/**
 * Velocity vector in a cartesian coordinate system.
 */
public record CartesianVelocity(
        VelocityComponent xVelocity,
        VelocityComponent yVelocity,
        VelocityComponent zVelocity) {}

