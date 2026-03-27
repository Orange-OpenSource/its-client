/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.managementcontainer;

/**
 * ActionId - identifier for a DENM action.
 *
 * @param originatingStationId originating station identifier
 * @param sequenceNumber sequence number
 */
public record ActionId(
        long originatingStationId,
        int sequenceNumber) {
}
