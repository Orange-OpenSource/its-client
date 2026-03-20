package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Dimension of an object together with a confidence value.
 *
 * @param value Object dimension value. Unit: 0,1 m. outOfRange (255), unavailable (256).
 * @param confidence Dimension confidence. Unit: 0,1 m. outOfRange (31), unavailable (32).
 */
public record ObjectDimension(int value, int confidence) {}
