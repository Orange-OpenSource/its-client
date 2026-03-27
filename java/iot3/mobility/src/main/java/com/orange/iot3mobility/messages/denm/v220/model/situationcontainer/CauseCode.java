/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.situationcontainer;

/**
 * CauseCode - cause code definition.
 *
 * @param cause main cause code. Range: 0-255
 * @param subcause Optional. Subcause code. Range: 0-255 (unavailable=0)
 */
public record CauseCode(
        int cause,
        Integer subcause) {
}
