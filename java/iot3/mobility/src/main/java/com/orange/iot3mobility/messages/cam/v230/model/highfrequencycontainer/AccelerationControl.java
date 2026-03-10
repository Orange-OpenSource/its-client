/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer;

/**
 * AccelerationControl v2.3.0
 *
 * @param brakePedalEngaged Driver is stepping on the brake pedal
 * @param gasPedalEngaged Driver is stepping on the gas pedal
 * @param emergencyBrakeEngaged Emergency brake system is engaged
 * @param collisionWarningEngaged Collision warning system is engaged
 * @param accEngaged Adaptative cruise control is engaged
 * @param cruiseControlEngaged Cruise control is engaged
 * @param speedLimiterEngaged Speed limiter is engaged
 */
public record AccelerationControl(
        boolean brakePedalEngaged,
        boolean gasPedalEngaged,
        boolean emergencyBrakeEngaged,
        boolean collisionWarningEngaged,
        boolean accEngaged,
        boolean cruiseControlEngaged,
        boolean speedLimiterEngaged) {}
