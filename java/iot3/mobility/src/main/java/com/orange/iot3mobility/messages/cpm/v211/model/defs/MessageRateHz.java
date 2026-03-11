package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Message rate. The specified message rate is: mantissa*(10^exponent).
 */
public record MessageRateHz(int mantissa, int exponent) {}
