/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.locationcontainer;

import com.orange.iot3mobility.messages.denm.v220.model.defs.DeltaReferencePosition;

/**
 * PathPoint - path point element.
 *
 * @param pathPosition {@link DeltaReferencePosition}
 * @param pathDeltaTime Optional. Unit: 10 millisecond. Range: 1-65535
 */
public record PathPoint(
        DeltaReferencePosition pathPosition,
        Integer pathDeltaTime) {
}
