package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Speed;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent;

/**
 * Velocity vector in a polar or cylindrical coordinate system.
 */
public record PolarVelocity(
        Speed velocityMagnitude,
        Angle velocityDirection,
        VelocityComponent zVelocity) {}

