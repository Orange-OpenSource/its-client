package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Euler angles of the object bounding box.
 */
public record EulerAngles(Angle zAngle, Angle yAngle, Angle xAngle) {}

