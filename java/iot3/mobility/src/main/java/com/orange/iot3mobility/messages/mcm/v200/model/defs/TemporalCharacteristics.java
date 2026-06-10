/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * Temporal occupancy characteristics of a Target Road Resource (TRR).
 *
 * @param trrOccupancyStartTime Start time delta since TimestampIts in ms [0..65535].
 * @param trrOccupancyEndTime   End time delta since TimestampIts in ms [0..65535].
 */
public record TemporalCharacteristics(int trrOccupancyStartTime, int trrOccupancyEndTime) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer trrOccupancyStartTime;
        private Integer trrOccupancyEndTime;

        private Builder() {}

        public Builder trrOccupancyStartTime(int trrOccupancyStartTime) {
            this.trrOccupancyStartTime = trrOccupancyStartTime;
            return this;
        }

        public Builder trrOccupancyEndTime(int trrOccupancyEndTime) {
            this.trrOccupancyEndTime = trrOccupancyEndTime;
            return this;
        }

        public TemporalCharacteristics build() {
            return new TemporalCharacteristics(
                    requireNonNull(trrOccupancyStartTime, "trr_occupancy_start_time"),
                    requireNonNull(trrOccupancyEndTime, "trr_occupancy_end_time"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

