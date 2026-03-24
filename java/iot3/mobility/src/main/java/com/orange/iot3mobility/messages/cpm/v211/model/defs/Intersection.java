/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Intersection reference in a MAPEM.
 *
 * @param id Intersection identifier.
 * @param region Optional region identifier of the responsible entity.
 */
public record Intersection(int id, Integer region) {}

