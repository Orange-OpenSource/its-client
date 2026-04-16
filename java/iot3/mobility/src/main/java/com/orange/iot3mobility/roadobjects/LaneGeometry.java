/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneDirection;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.LaneTypeFlag;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

/**
 * Version-agnostic representation of a single lane extracted from a MAPEM message.
 * <p>
 * All MAPEM-version-specific structures are resolved by {@code MapemLaneConverter} before
 * constructing this record. SDK implementers should use this type exclusively and never
 * need to import any MAPEM v2xx model class.
 *
 * @param laneId          Intersection-unique lane identifier.
 * @param centerLine      Ordered list of absolute WGS-84 positions (lat/lon in degrees)
 *                        describing the lane centre line from the stop line outward.
 *                        Always contains at least 2 points.
 * @param directionalUse  Allowed travel directions ({@link LaneDirection#INGRESS_PATH}
 *                        and/or {@link LaneDirection#EGRESS_PATH}).
 * @param laneTypeFlags   Typed attribute flags for the active lane type
 *                        (e.g. {@link com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.VehicleLaneAttribute#IS_VEHICLE_REVOCABLE_LANE}).
 *                        Empty when no flags are defined.
 * @param signalGroups    Signal-group IDs from {@code connects_to} entries, linking this lane
 *                        to SPATEM phase data. Empty when the lane has no signalized connections.
 */
public record LaneGeometry(
        int laneId,
        List<LatLng> centerLine,
        List<LaneDirection> directionalUse,
        List<LaneTypeFlag> laneTypeFlags,
        List<Integer> signalGroups) {
}
