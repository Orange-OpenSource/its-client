/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Message rate in Hz expressed as mantissa * 10^exponent.
 *
 * @param mantissa Mantissa of the rate (1..100).
 * @param exponent Exponent of the rate (-5..2).
 */
public record MessageRateHz(int mantissa, int exponent) {}
