/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.defs;

/**
 * Circular area.
 *
 * @param nodeCenterPoint Optional center offset.
 * @param radius Radius. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 */
public record AreaCircular(
        Offset nodeCenterPoint,
        int radius) {}

