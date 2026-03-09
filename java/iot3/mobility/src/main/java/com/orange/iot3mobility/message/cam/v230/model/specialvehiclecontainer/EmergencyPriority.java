/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * EmergencyPriority v2.3.0
 * <p>
 * Represents right of way indicator of the vehicle ITS-S that originates the CAM PDU.
 *
 * @param requestForRightOfWay When the vehicle is requesting/assuming the right of way
 * @param requestForFreeCrossingAtTrafficLight When the vehicle is requesting/assuming the right to pass at a (red)
 *                                             traffic light
 */
public record EmergencyPriority(
        boolean requestForRightOfWay,
        boolean requestForFreeCrossingAtTrafficLight) {}
