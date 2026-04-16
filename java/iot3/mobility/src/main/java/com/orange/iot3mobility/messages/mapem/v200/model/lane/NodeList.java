/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import java.util.List;

/**
 * The node list of a lane, either as an explicit node sequence or as a computed reference.
 * Exactly one of {@code nodes} or {@code computed} should be non-null.
 *
 * @param nodes Optional. Ordered list of node points describing the lane centre line. Min 2, max 63.
 * @param computed Optional. Computed lane derived from another lane by translation/rotation/scale.
 */
public record NodeList(List<NodeXY> nodes, ComputedLane computed) {
}

