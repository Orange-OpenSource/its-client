/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * YawRate v2.3.0
 *
 * @param value Yaw rate represents the vehicle rotation around z-axis of the coordinate system. Negative for clockwise
 *              rotation (i.e. to the right), positive for anti-clockwise rotation (i.e. to the left). Unit: 0,01 degree
 *              per second. negativeOutOfRange (-32766), positiveOutOfRange (32766), unavailable (32767)
 * @param confidence Confidence for the yaw rate value. degSec-000-01 (0), degSec-000-05 (1), degSec-000-10 (2),
 *                   degSec-001-00 (3), degSec-005-00 (4), degSec-010-00 (5), degSec-100-00 (6), outOfRange (7),
 *                   unavailable (8)
 */
public record YawRate(int value, int confidence) {}
