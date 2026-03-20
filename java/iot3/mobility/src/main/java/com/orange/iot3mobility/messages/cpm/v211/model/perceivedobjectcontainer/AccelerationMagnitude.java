package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Acceleration magnitude with confidence.
 *
 * @param value Magnitude of the acceleration vector. Unit: 0,1 m/s2. noAcceleration (0), positiveOutOfRange (160),
 *              unavailable (161).
 * @param confidence Acceleration confidence. Unit: 0,1 m/s2. outOfRange (101), unavailable (102).
 */
public record AccelerationMagnitude(int value, int confidence) {}
