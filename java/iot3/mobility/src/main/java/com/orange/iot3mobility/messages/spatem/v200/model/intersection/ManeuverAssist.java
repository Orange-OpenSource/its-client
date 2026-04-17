/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

/**
 * Dynamic flow data for a lane-to-lane connection.
 *
 * @param connectionId           Required. Connection index [0..255].
 * @param queueLength            Optional. Distance from stop line to rear of queue in meters [0..10000].
 * @param availableStorageLength Optional. Available downstream storage length in meters [0..10000].
 * @param waitOnStop             Optional. If true, vehicles must stop at the stop line.
 * @param pedBicycleDetect       Optional. If true, pedestrians/bicyclists detected in the crossing lane.
 */
public record ManeuverAssist(
        int connectionId,
        Integer queueLength,
        Integer availableStorageLength,
        Boolean waitOnStop,
        Boolean pedBicycleDetect) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer connectionId;
        private Integer queueLength;
        private Integer availableStorageLength;
        private Boolean waitOnStop;
        private Boolean pedBicycleDetect;

        private Builder() {}

        public Builder connectionId(int connectionId) { this.connectionId = connectionId; return this; }
        public Builder queueLength(Integer queueLength) { this.queueLength = queueLength; return this; }
        public Builder availableStorageLength(Integer availableStorageLength) { this.availableStorageLength = availableStorageLength; return this; }
        public Builder waitOnStop(Boolean waitOnStop) { this.waitOnStop = waitOnStop; return this; }
        public Builder pedBicycleDetect(Boolean pedBicycleDetect) { this.pedBicycleDetect = pedBicycleDetect; return this; }

        public ManeuverAssist build() {
            return new ManeuverAssist(
                    requireNonNull(connectionId, "connectionId"),
                    queueLength, availableStorageLength, waitOnStop, pedBicycleDetect);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
