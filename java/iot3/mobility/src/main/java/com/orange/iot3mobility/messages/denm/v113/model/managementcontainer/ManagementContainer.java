/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.managementcontainer;

/**
 * ManagementContainer - DENM v1.1.3 management container.
 *
 * @param actionId {@link ActionId}
 * @param detectionTime Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000)
 * @param referenceTime Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000)
 * @param termination Optional. isCancellation(0), isNegation(1)
 * @param eventPosition {@link ReferencePosition}
 * @param relevanceDistance Optional. lessThan50m(0), lessThan100m(1), lessThan200m(2), lessThan500m(3),
 *                          lessThan1000m(4), lessThan5km(5), lessThan10km(6), over10km(7)
 * @param relevanceTrafficDirection Optional. allTrafficDirections(0), upstreamTraffic(1), downstreamTraffic(2),
 *                                  oppositeTraffic(3)
 * @param validityDuration Optional. Unit: second. timeOfDetection(0), oneSecondAfterDetection(1)
 * @param transmissionInterval Optional. Unit: millisecond. oneMilliSecond(1), tenSeconds(10000)
 * @param stationType Optional. Station type (unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4),
 *                    passengerCar(5), bus(6), lightTruck(7), heavyTruck(8), trailer(9), specialVehicles(10),
 *                    tram(11), roadSideUnit(15))
 * @param confidence Optional. {@link PositionConfidence}
 */
public record ManagementContainer(
        ActionId actionId,
        long detectionTime,
        long referenceTime,
        Integer termination,
        ReferencePosition eventPosition,
        Integer relevanceDistance,
        Integer relevanceTrafficDirection,
        Integer validityDuration,
        Integer transmissionInterval,
        Integer stationType,
        PositionConfidence confidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ActionId actionId;
        private Long detectionTime;
        private Long referenceTime;
        private Integer termination;
        private ReferencePosition eventPosition;
        private Integer relevanceDistance;
        private Integer relevanceTrafficDirection;
        private Integer validityDuration;
        private Integer transmissionInterval;
        private Integer stationType;
        private PositionConfidence confidence;

        public Builder actionId(ActionId actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder detectionTime(long detectionTime) {
            this.detectionTime = detectionTime;
            return this;
        }

        public Builder referenceTime(long referenceTime) {
            this.referenceTime = referenceTime;
            return this;
        }

        public Builder termination(Integer termination) {
            this.termination = termination;
            return this;
        }

        public Builder eventPosition(ReferencePosition eventPosition) {
            this.eventPosition = eventPosition;
            return this;
        }

        public Builder relevanceDistance(Integer relevanceDistance) {
            this.relevanceDistance = relevanceDistance;
            return this;
        }

        public Builder relevanceTrafficDirection(Integer relevanceTrafficDirection) {
            this.relevanceTrafficDirection = relevanceTrafficDirection;
            return this;
        }

        public Builder validityDuration(Integer validityDuration) {
            this.validityDuration = validityDuration;
            return this;
        }

        public Builder transmissionInterval(Integer transmissionInterval) {
            this.transmissionInterval = transmissionInterval;
            return this;
        }

        public Builder stationType(Integer stationType) {
            this.stationType = stationType;
            return this;
        }

        public Builder confidence(PositionConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public ManagementContainer build() {
            return new ManagementContainer(
                    requireNonNull(actionId, "action_id"),
                    requireNonNull(detectionTime, "detection_time"),
                    requireNonNull(referenceTime, "reference_time"),
                    termination,
                    requireNonNull(eventPosition, "event_position"),
                    relevanceDistance,
                    relevanceTrafficDirection,
                    validityDuration,
                    transmissionInterval,
                    stationType,
                    confidence);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
