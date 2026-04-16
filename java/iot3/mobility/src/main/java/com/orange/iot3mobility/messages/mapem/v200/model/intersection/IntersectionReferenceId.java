/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.intersection;

/**
 * Globally unique intersection identifier combining an optional region ID and a local intersection ID.
 *
 * @param region Optional. Regional authority identifier. Range: 0..65535.
 * @param id Local intersection identifier. Range: 0..65535.
 */
public record IntersectionReferenceId(Integer region, int id) {
}

