/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.locationcontainer;

import java.util.List;

/**
 * PathHistory - list of path points.
 *
 * @param pathHistory {@link PathPoint} list (max 40)
 */
public record PathHistory(
        List<PathPoint> pathHistory) {
}

