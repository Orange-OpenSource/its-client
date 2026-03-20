package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Speed and associated confidence value.
 *
 * @param value Speed value (magnitude of velocity). Unit: 0,01 m/s. standstill (0), outOfRange (16382),
 *              unavailable (16383).
 * @param confidence Speed confidence. Unit: 0,01 m/s. outOfRange (126), unavailable (127).
 */
public record Speed(int value, int confidence) {}
