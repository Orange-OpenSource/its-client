/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent;

/**
 * Velocity vector in a cartesian coordinate system.
 *
 * @param xVelocity {@link VelocityComponent} along the x-axis.
 * @param yVelocity {@link VelocityComponent} along the y-axis.
 * @param zVelocity Optional. {@link VelocityComponent} along the z-axis.
 */
public record CartesianVelocity(
        VelocityComponent xVelocity,
        VelocityComponent yVelocity,
        VelocityComponent zVelocity) {}
