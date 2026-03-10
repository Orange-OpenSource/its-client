/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.model;

/**
 * PathPoint v1.1.3
 *
 * @param deltaPosition {@link DeltaReferencePosition}
 * @param deltaTime time traveled by the detecting ITS-S since the previous detected event point
 *                  (generation_delta_time). tenMilliSecondsInPast(1)
 */
public record PathPoint(
        DeltaReferencePosition deltaPosition,
        Integer deltaTime) {}
