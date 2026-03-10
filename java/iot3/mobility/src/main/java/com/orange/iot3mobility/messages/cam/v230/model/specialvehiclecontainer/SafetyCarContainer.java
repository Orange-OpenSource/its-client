/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.specialvehiclecontainer;

/**
 * SafetyCarContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to safetyCar(7) this container shall be present.
 *
 * @param lightBarSirenInUse {@link LightBarSiren}
 * @param incidentIndication Optional {@link IncidentIndication}
 * @param trafficRule Optional rule to indicate whether vehicles are allowed to overtake a safety car that is
 *                    originating this CAM. noPassing(0), noPassingForTrucks(1), passToRight(2), passToLeft(3),
 *                    passToLeftOrRight (4)
 * @param speedLimit Optional speed to indicate whether a speed limit is applied to vehicles following the safety car.
 *                   Unit: km/h
 */
public record SafetyCarContainer(
        LightBarSiren lightBarSirenInUse,
        IncidentIndication incidentIndication,
        Integer trafficRule,
        Integer speedLimit) implements SpecialVehiclePayload {}
