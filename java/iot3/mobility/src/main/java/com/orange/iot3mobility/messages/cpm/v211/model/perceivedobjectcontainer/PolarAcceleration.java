package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.AccelerationComponent;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Acceleration vector in a polar or cylindrical coordinate system.
 */
public record PolarAcceleration(
        AccelerationMagnitude accelerationMagnitude,
        Angle accelerationDirection,
        AccelerationComponent zAcceleration) {}

