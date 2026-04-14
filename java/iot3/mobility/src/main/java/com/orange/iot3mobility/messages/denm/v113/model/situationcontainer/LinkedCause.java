/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.situationcontainer;

/**
 * LinkedCause - linked cause definition.
 *
 * @param cause cause code. Range: 0-255
 * @param subcause Optional. Subcause code. Range: 0-255 (unavailable=0)
 */
public record LinkedCause(
        int cause,
        Integer subcause) {
}
