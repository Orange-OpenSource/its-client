/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

/**
 * Conveys the combination of an optional region and an IntersectionID.
 *
 * @param region Optional. Road regulator ID [0..65535].
 * @param id     Required. Intersection ID [0..65535].
 */
public record IntersectionReferenceId(Integer region, int id) {}

