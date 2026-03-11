package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Components included in the lower triangular correlation matrix.
 */
public record LowerTriangularMatrixComponents(
        boolean xPosition,
        boolean yPosition,
        boolean zPosition,
        boolean xVelocityOrVelocityMagnitude,
        boolean yVelocityOrVelocityDirection,
        boolean zSpeed,
        boolean xAccelOrAccelMagnitude,
        boolean yAccelOrAccelDirection,
        boolean zAcceleration,
        boolean zAngle,
        boolean yAngle,
        boolean xAngle,
        boolean zAngularVelocity) {}

