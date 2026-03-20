/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Components included in the lower triangular correlation matrix.
 *
 * @param xPosition Include x coordinate of position.
 * @param yPosition Include y coordinate of position.
 * @param zPosition Include z coordinate of position.
 * @param xVelocityOrVelocityMagnitude Include x velocity or velocity magnitude.
 * @param yVelocityOrVelocityDirection Include y velocity or velocity direction.
 * @param zSpeed Include z speed component.
 * @param xAccelOrAccelMagnitude Include x acceleration or acceleration magnitude.
 * @param yAccelOrAccelDirection Include y acceleration or acceleration direction.
 * @param zAcceleration Include z acceleration component.
 * @param zAngle Include yaw angle (z axis).
 * @param yAngle Include pitch angle (y axis).
 * @param xAngle Include roll angle (x axis).
 * @param zAngularVelocity Include angular velocity around z axis.
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

