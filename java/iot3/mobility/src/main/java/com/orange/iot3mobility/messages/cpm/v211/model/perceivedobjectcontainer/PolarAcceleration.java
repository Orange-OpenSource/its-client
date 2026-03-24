/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.AccelerationComponent;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Acceleration vector in a polar or cylindrical coordinate system.
 *
 * @param accelerationMagnitude {@link AccelerationMagnitude} projected onto the reference plane.
 * @param accelerationDirection {@link Angle} of the projected acceleration vector.
 * @param zAcceleration Optional. {@link AccelerationComponent} along the reference axis.
 */
public record PolarAcceleration(
        AccelerationMagnitude accelerationMagnitude,
        Angle accelerationDirection,
        AccelerationComponent zAcceleration) {}
