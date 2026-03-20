package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Message rate in Hz expressed as mantissa * 10^exponent.
 *
 * @param mantissa Mantissa of the rate (1..100).
 * @param exponent Exponent of the rate (-5..2).
 */
public record MessageRateHz(int mantissa, int exponent) {}
