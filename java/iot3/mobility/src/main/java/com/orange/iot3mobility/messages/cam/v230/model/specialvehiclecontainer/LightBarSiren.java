/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.specialvehiclecontainer;

/**
 * LightBarSiren v2.3.0
 * <p>
 * Status of light bar and any sort of audible alarm system besides the horn.
 *
 * @param lightBarActivated When the light bar is activated
 * @param sirenActivated When the siren is activated
 */
public record LightBarSiren(
        boolean lightBarActivated,
        boolean sirenActivated) {}
