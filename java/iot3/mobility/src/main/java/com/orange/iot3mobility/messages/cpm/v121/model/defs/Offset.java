/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.defs;

/**
 * Offset position.
 *
 * @param x X offset. Value: [-32768..32767].
 * @param y Y offset. Value: [-32768..32767].
 * @param z Optional Z offset. Value: [-32768..32767].
 */
public record Offset(
        int x,
        int y,
        Integer z) {}

