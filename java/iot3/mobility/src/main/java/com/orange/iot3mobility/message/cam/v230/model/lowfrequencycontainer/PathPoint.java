/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;

/**
 *
 * @param pathPosition {@link DeltaReferencePosition}
 * @param pathDeltaTime Optional travel time separated from a waypoint to the predefined reference position.
 *                      Unit: 0,01 second
 */
public record PathPoint(
        DeltaReferencePosition pathPosition,
        Integer pathDeltaTime) {}
