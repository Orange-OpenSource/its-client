/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

/**
 * Delta position of a single node point: either an XY offset or an absolute lat/lon.
 * Exactly one of {@code nodeXy} or {@code nodeLatLon} should be non-null.
 *
 * @param nodeXy Optional. XY offset from the previous node.
 * @param nodeLatLon Optional. Absolute lat/lon node position.
 */
public record NodeDelta(NodeXYOffset nodeXy, NodeLatLon nodeLatLon) {
}

