/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.specialvehiclecontainer;

/**
 * DrivingLaneStatus v2.3.0
 *
 * @param lane1Closed Indicates whether lane 1 is closed or not
 * @param lane2Closed Indicates whether lane 2 is closed or not
 * @param lane3Closed Indicates whether lane 3 is closed or not
 * @param lane4Closed Indicates whether lane 4 is closed or not
 * @param lane5Closed Indicates whether lane 5 is closed or not
 * @param lane6Closed Indicates whether lane 6 is closed or not
 * @param lane7Closed Indicates whether lane 7 is closed or not
 * @param lane8Closed Indicates whether lane 8 is closed or not
 * @param lane9Closed Indicates whether lane 9 is closed or not
 * @param lane10Closed Indicates whether lane 10 is closed or not
 * @param lane11Closed Indicates whether lane 11 is closed or not
 * @param lane12Closed Indicates whether lane 12 is closed or not
 * @param lane13Closed Indicates whether lane 13 is closed or not
 */
public record DrivingLaneStatus(
        boolean lane1Closed,
        boolean lane2Closed,
        boolean lane3Closed,
        boolean lane4Closed,
        boolean lane5Closed,
        boolean lane6Closed,
        boolean lane7Closed,
        boolean lane8Closed,
        boolean lane9Closed,
        boolean lane10Closed,
        boolean lane11Closed,
        boolean lane12Closed,
        boolean lane13Closed) {}