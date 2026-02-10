package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

public record AccelerationControl(
        boolean brakePedalEngaged,
        boolean gasPedalEngaged,
        boolean emergencyBrakeEngaged,
        boolean collisionWarningEngaged,
        boolean accEngaged,
        boolean cruiseControlEngaged,
        boolean speedLimiterEngaged) {}
