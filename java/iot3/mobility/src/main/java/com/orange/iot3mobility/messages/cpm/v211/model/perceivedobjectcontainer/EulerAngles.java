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

