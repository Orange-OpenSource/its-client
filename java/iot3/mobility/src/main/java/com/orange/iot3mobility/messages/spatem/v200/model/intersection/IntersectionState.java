/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

import com.orange.iot3mobility.messages.spatem.v200.model.intersection.enums.IntersectionStatusFlag;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the signal phase and timing state for one intersection.
 *
 * @param id                  Required. Globally unique intersection ID.
 * @param revision            Required. Current revision of the intersection description [0..127].
 * @param status              Required. General status flags of the controller(s).
 *                            Use {@link IntersectionStatusFlag} values.
 * @param states              Required. List of MovementState entries [1..255].
 * @param name                Optional. Human-readable name for debug use.
 * @param moy                 Optional. Minute of current UTC year [0..527040].
 * @param timestamp           Optional. Millisecond in the current UTC minute [0..65535].
 * @param enabledLanes        Optional. List of enabled revocable lane IDs [1..16].
 * @param maneuverAssistList  Optional. Maneuver assist data.
 */
public record IntersectionState(
        IntersectionReferenceId id,
        int revision,
        List<String> status,
        List<MovementState> states,
        String name,
        Integer moy,
        Integer timestamp,
        List<Integer> enabledLanes,
        List<ManeuverAssist> maneuverAssistList) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private IntersectionReferenceId id;
        private Integer revision;
        private List<String> status;
        private List<MovementState> states;
        private String name;
        private Integer moy;
        private Integer timestamp;
        private List<Integer> enabledLanes;
        private List<ManeuverAssist> maneuverAssistList;

        private Builder() {}

        public Builder id(IntersectionReferenceId id) { this.id = id; return this; }
        public Builder revision(int revision) { this.revision = revision; return this; }
        public Builder status(List<String> status) { this.status = status; return this; }

        /**
         * Sets the intersection status flags using typed enum constants.
         *
         * @param flags zero or more {@link IntersectionStatusFlag} values
         * @return this builder
         */
        public Builder status(IntersectionStatusFlag... flags) {
            this.status = flags.length == 0
                    ? java.util.Collections.emptyList()
                    : Arrays.stream(flags).map(IntersectionStatusFlag::value).toList();
            return this;
        }
        public Builder states(List<MovementState> states) { this.states = states; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder moy(Integer moy) { this.moy = moy; return this; }
        public Builder timestamp(Integer timestamp) { this.timestamp = timestamp; return this; }
        public Builder enabledLanes(List<Integer> enabledLanes) { this.enabledLanes = enabledLanes; return this; }
        public Builder maneuverAssistList(List<ManeuverAssist> maneuverAssistList) { this.maneuverAssistList = maneuverAssistList; return this; }

        public IntersectionState build() {
            return new IntersectionState(
                    requireNonNull(id, "id"),
                    requireNonNull(revision, "revision"),
                    requireNonNull(status, "status"),
                    requireNonNull(states, "states"),
                    name, moy, timestamp, enabledLanes, maneuverAssistList);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

