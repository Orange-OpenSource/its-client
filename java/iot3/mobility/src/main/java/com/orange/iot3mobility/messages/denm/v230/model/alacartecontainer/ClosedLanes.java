/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer;

/**
 * ClosedLanes - lanes closed due to road works.
 *
 * @param hardShoulderStatus Optional. Status of the hard shoulder.
 *                           availableForStopping(0), closed(1), availableForDriving(2).
 * @param drivingLaneStatus  Optional. Bit mask indicating which driving lanes are closed
 *                           (bit N = lane N+1 from inside). 0 means all lanes open. Range: 0..16383.
 */
public record ClosedLanes(
        Integer hardShoulderStatus,
        Integer drivingLaneStatus) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer hardShoulderStatus;
        private Integer drivingLaneStatus;

        public Builder hardShoulderStatus(Integer hardShoulderStatus) {
            this.hardShoulderStatus = hardShoulderStatus;
            return this;
        }

        public Builder drivingLaneStatus(Integer drivingLaneStatus) {
            this.drivingLaneStatus = drivingLaneStatus;
            return this;
        }

        public ClosedLanes build() {
            return new ClosedLanes(hardShoulderStatus, drivingLaneStatus);
        }
    }
}

