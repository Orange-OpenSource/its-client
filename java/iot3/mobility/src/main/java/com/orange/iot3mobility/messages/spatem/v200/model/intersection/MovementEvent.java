/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

import java.util.List;

/**
 * Contains details about a single movement event within a signal phase sequence.
 *
 * @param eventState Required. Signal phase state [0..9].
 * @param timing     Optional. Timing details for this phase.
 * @param speeds     Optional. Advisory speed recommendations.
 */
public record MovementEvent(
        int eventState,
        TimeChangeDetail timing,
        List<AdvisorySpeed> speeds) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer eventState;
        private TimeChangeDetail timing;
        private List<AdvisorySpeed> speeds;

        private Builder() {}

        public Builder eventState(int eventState) { this.eventState = eventState; return this; }
        public Builder timing(TimeChangeDetail timing) { this.timing = timing; return this; }
        public Builder speeds(List<AdvisorySpeed> speeds) { this.speeds = speeds; return this; }

        public MovementEvent build() {
            return new MovementEvent(
                    requireNonNull(eventState, "eventState"),
                    timing, speeds);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

