/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Dimension of an object together with a confidence value.
 *
 * @param value Object dimension value. Unit: 0,1 m. outOfRange (255), unavailable (256).
 * @param confidence Dimension confidence. Unit: 0,1 m. outOfRange (31), unavailable (32).
 */
public record ObjectDimension(int value, int confidence) {}
