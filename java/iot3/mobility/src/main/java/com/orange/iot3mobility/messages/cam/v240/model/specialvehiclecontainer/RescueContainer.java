/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer;

/**
 * RescueContainer v2.4.0
 * <p>
 * If the vehicleRole component is set to rescue(5) this container shall be present.
 *
 * @param lightBarSirenInUse {@link LightBarSiren}
 */
public record RescueContainer(
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
