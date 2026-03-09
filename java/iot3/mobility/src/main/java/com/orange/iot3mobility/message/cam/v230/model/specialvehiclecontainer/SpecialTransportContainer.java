/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * SpecialTransportContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to specialTransport(2) this container shall be present.
 *
 * @param specialTransportType {@link SpecialTransportType}
 * @param lightBarSirenInUse {@link LightBarSiren}
 */
public record SpecialTransportContainer(
        SpecialTransportType specialTransportType,
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
