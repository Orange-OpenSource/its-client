/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.AllowedManeuver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Connecting lane descriptor: links this lane to an outbound lane beyond the stop line.
 *
 * @param lane     ID of the connecting (outbound) lane.
 * @param maneuver Optional. Allowed manoeuvres from this lane to the connecting lane.
 *                 Use {@link AllowedManeuver} values.
 */
public record ConnectingLane(int lane, List<String> maneuver) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer lane;
        private List<String> maneuver;

        private Builder() {}

        public Builder lane(int lane) { this.lane = lane; return this; }

        /** Sets the allowed manoeuvres using raw JSON strings. */
        public Builder maneuver(List<String> maneuver) { this.maneuver = maneuver; return this; }

        /**
         * Sets the allowed manoeuvres using typed enum constants.
         * Pass no arguments for an empty list.
         * <p>Example: {@code .maneuver(AllowedManeuver.MANEUVER_LEFT_ALLOWED)}
         *
         * @param maneuvers zero or more {@link AllowedManeuver} values
         * @return this builder
         */
        public Builder maneuver(AllowedManeuver... maneuvers) {
            this.maneuver = maneuvers.length == 0
                    ? Collections.emptyList()
                    : Arrays.stream(maneuvers).map(AllowedManeuver::value).toList();
            return this;
        }

        public ConnectingLane build() {
            return new ConnectingLane(
                    requireNonNull(lane, "lane"),
                    maneuver);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
