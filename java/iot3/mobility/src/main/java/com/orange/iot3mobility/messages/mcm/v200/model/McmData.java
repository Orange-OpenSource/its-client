/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model;

import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;

import java.util.List;

/**
 * MCM data payload. Exactly one of {@link #vehicleManoeuvreContainer} or
 * {@link #advisedManoeuvreContainer} must be non-null.
 *
 * @param vehicleManoeuvreContainer  Optional. Present when the originating station is a vehicle sending its own state.
 * @param advisedManoeuvreContainer  Optional. Present when the message consists solely of advice entries.
 */
public record McmData(
        VehicleManoeuvreContainer vehicleManoeuvreContainer,
        List<ManoeuvreAdvice> advisedManoeuvreContainer) {

    /**
     * Factory for a vehicle-originating MCM.
     *
     * @param vehicleManoeuvreContainer the vehicle manoeuvre container, must not be null.
     * @return McmData carrying a vehicle_manoeuvre_container.
     */
    public static McmData ofVehicle(VehicleManoeuvreContainer vehicleManoeuvreContainer) {
        if (vehicleManoeuvreContainer == null) {
            throw new IllegalStateException("Missing field: vehicle_manoeuvre_container");
        }
        return new McmData(vehicleManoeuvreContainer, null);
    }

    /**
     * Factory for an infrastructure/RSU-originating MCM carrying only advice.
     *
     * @param advisedManoeuvreContainer the list of manoeuvre advice entries, must not be null or empty.
     * @return McmData carrying an advised_manoeuvre_container.
     */
    public static McmData ofAdvised(List<ManoeuvreAdvice> advisedManoeuvreContainer) {
        if (advisedManoeuvreContainer == null) {
            throw new IllegalStateException("Missing field: advised_manoeuvre_container");
        }
        return new McmData(null, advisedManoeuvreContainer);
    }
}

