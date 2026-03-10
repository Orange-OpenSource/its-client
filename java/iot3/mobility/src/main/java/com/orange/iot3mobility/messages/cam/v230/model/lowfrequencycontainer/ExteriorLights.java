/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.lowfrequencycontainer;

/**
 * ExteriorLights v2.3.0
 * <p>
 * Status of the most important exterior lights switches of the vehicle ITS-S that originates the CAM.
 *
 * @param lowBeamHeadlightsOn When the low beam headlight switch is on
 * @param highBeamHeadlightsOn When the high beam headlight switch is on
 * @param leftTurnSignalOn When the left turn signal switch is on
 * @param rightTurnSignalOn When the right turn signal switch is on
 * @param daytimeRunningLightsOn When the daytime running light switch is on
 * @param reverseLightOn When the reverse light switch is on
 * @param fogLightOn When the tail fog light switch is on
 * @param parkingLightsOn When the parking light switch is on
 */
public record ExteriorLights(
        boolean lowBeamHeadlightsOn,
        boolean highBeamHeadlightsOn,
        boolean leftTurnSignalOn,
        boolean rightTurnSignalOn,
        boolean daytimeRunningLightsOn,
        boolean reverseLightOn,
        boolean fogLightOn,
        boolean parkingLightsOn) {}
