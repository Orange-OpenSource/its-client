/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

/**
 * Timing information for a signal phase change.
 * <p>
 * All time values are in tenths of a second in the current or next hour (0–36001;
 * 36001 = unknown/indefinite future).
 *
 * @param minEndTime  Required. Earliest time the phase could change.
 * @param startTime   Optional. Time the phase started or is expected to start.
 * @param maxEndTime  Optional. Latest time the phase could change.
 * @param likelyTime  Optional. Most likely time the phase changes (adaptive control).
 * @param confidence  Optional. Confidence in likelyTime [0..15].
 * @param nextTime    Optional. Next time this phase will occur (approximate).
 */
public record TimeChangeDetail(
        int minEndTime,
        Integer startTime,
        Integer maxEndTime,
        Integer likelyTime,
        Integer confidence,
        Integer nextTime) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer minEndTime;
        private Integer startTime;
        private Integer maxEndTime;
        private Integer likelyTime;
        private Integer confidence;
        private Integer nextTime;

        private Builder() {}

        public Builder minEndTime(int minEndTime) { this.minEndTime = minEndTime; return this; }
        public Builder startTime(Integer startTime) { this.startTime = startTime; return this; }
        public Builder maxEndTime(Integer maxEndTime) { this.maxEndTime = maxEndTime; return this; }
        public Builder likelyTime(Integer likelyTime) { this.likelyTime = likelyTime; return this; }
        public Builder confidence(Integer confidence) { this.confidence = confidence; return this; }
        public Builder nextTime(Integer nextTime) { this.nextTime = nextTime; return this; }

        public TimeChangeDetail build() {
            return new TimeChangeDetail(
                    requireNonNull(minEndTime, "minEndTime"),
                    startTime, maxEndTime, likelyTime, confidence, nextTime);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

