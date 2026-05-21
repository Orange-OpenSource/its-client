/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.shared.SpeedLimit;

import java.util.List;

/**
 * Node-level attribute data: one entry combining multiple attribute values at a single node.
 *
 * @param pathEndPointAngle Optional. Cant angle of the lane stop line at the last node, in degrees. Range: -150..150.
 * @param laneCrownPointCenter Optional. Road bed camber from centreline (roadway_crown_angle integer).
 * @param laneCrownPointLeft Optional. Road bed camber from left edge.
 * @param laneCrownPointRight Optional. Road bed camber from right edge.
 * @param laneAngle Optional. Angle of a merging/departing lane at this node, in units of 1.5 degrees. Range: -180..180.
 * @param speedLimits Optional. Speed limits applicable from this node onwards.
 */
public record NodeAttributeData(
        Integer pathEndPointAngle,
        Integer laneCrownPointCenter,
        Integer laneCrownPointLeft,
        Integer laneCrownPointRight,
        Integer laneAngle,
        List<SpeedLimit> speedLimits) {
}

