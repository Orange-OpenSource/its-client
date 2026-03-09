/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * ClosedLanes v2.3.0
 * <p>
 * Provides information about the opening/closure status of the lanes ahead.
 *
 * @param innerHardShoulderStatus Optional and shall be included if an inner hard shoulder is present and the
 *                                information is known. availableForStopping(0), closed(1), availableForDriving(2)
 * @param outerHardShoulderStatus Optional and shall be included if an outer hard shoulder is present and the
 *                                information is known. availableForStopping(0), closed(1), availableForDriving(2)
 * @param drivingLaneStatus Optional {@link DrivingLaneStatus} and shall be included if the information is known
 */
public record ClosedLanes(
        Integer innerHardShoulderStatus,
        Integer outerHardShoulderStatus,
        DrivingLaneStatus drivingLaneStatus) {}
