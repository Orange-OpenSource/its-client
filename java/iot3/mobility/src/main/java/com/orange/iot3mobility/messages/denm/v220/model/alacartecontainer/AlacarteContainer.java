/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.alacartecontainer;

/**
 * AlacarteContainer - DENM v2.2.0 alacarte container.
 *
 * @param lanePosition Optional. offTheRoad(-1), innerHardShoulder(0), innermostDrivingLane(1),
 *                     secondLaneFromInside(2), outterHardShoulder(14)
 * @param positioningSolution Optional. noPositioningSolution(0), sGNSS(1), dGNSS(2), sGNSSplusDR(3),
 *                            dGNSSplusDR(4), dR(5), manuallyByOperator(6)
 */
public record AlacarteContainer(
        Integer lanePosition,
        Integer positioningSolution) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer lanePosition;
        private Integer positioningSolution;

        public Builder lanePosition(Integer lanePosition) {
            this.lanePosition = lanePosition;
            return this;
        }

        public Builder positioningSolution(Integer positioningSolution) {
            this.positioningSolution = positioningSolution;
            return this;
        }

        public AlacarteContainer build() {
            return new AlacarteContainer(lanePosition, positioningSolution);
        }
    }
}
