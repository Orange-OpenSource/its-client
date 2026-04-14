/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.path;

/**
 * DenmPathElement220 - path element.
 *
 * @param position {@link PathPosition}
 * @param messageType message type (denm, cam, cpm, po)
 */
public record PathElement(
        PathPosition position,
        String messageType) {
}
