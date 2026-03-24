/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Euler angles of the object bounding box.
 *
 * @param zAngle {@link Angle} yaw (rotation around z-axis) at measurement time.
 * @param yAngle Optional {@link Angle} pitch (rotation around y-axis) at measurement time.
 * @param xAngle Optional {@link Angle} roll (rotation around x-axis) at measurement time.
 */
public record EulerAngles(Angle zAngle, Angle yAngle, Angle xAngle) {}

