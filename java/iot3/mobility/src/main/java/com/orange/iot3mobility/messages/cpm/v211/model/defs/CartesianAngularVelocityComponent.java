package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Angular velocity component along with a confidence value in a cartesian coordinate system.
 *
 * @param value Angular velocity component. Unit: degree/s. negativeOutOfRange (-255), positiveOutOfRange (255),
 *              unavailable (256).
 * @param confidence Angular speed confidence. degSec-01 (0), degSec-02 (1), degSec-05 (2), degSec-10 (3),
 *                   degSec-20 (4), degSec-50 (5), outOfRange (6), unavailable (7).
 */
public record CartesianAngularVelocityComponent(int value, int confidence) {}
