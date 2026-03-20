package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Speed;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent;

/**
 * Velocity vector in a polar or cylindrical coordinate system.
 *
 * @param velocityMagnitude {@link Speed} of the velocity vector projected onto the reference plane.
 * @param velocityDirection {@link Angle} of the velocity vector projected onto the reference plane.
 * @param zVelocity Optional. {@link VelocityComponent} along the reference axis of the cylindrical system.
 */
public record PolarVelocity(
        Speed velocityMagnitude,
        Angle velocityDirection,
        VelocityComponent zVelocity) {}
