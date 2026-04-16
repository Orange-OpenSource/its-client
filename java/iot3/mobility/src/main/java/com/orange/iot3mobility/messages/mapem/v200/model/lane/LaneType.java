/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import java.util.List;

/**
 * Lane type attributes: a oneOf group where exactly one field should be non-null.
 * Each field is a list of attribute flags specific to that lane type.
 *
 * @param vehicle Optional. Flags for motorized vehicle lanes.
 * @param crosswalk Optional. Flags for crosswalk lanes.
 * @param bikeLane Optional. Flags for bicycle lanes.
 * @param sidewalk Optional. Flags for sidewalk lanes.
 * @param median Optional. Flags for median/barrier lanes.
 * @param striping Optional. Flags for ground striping lanes.
 * @param trackedVehicle Optional. Flags for tracked-vehicle (rail) lanes.
 * @param parking Optional. Flags for parking lanes.
 */
public record LaneType(
        List<String> vehicle,
        List<String> crosswalk,
        List<String> bikeLane,
        List<String> sidewalk,
        List<String> median,
        List<String> striping,
        List<String> trackedVehicle,
        List<String> parking) {
}

