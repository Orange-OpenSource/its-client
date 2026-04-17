/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

import java.util.List;

/**
 * Conveys the signal phase state for a collection of lanes sharing a common signal group.
 *
 * @param signalGroup        Required. Signal group ID [0..255].
 * @param stateTimeSpeed     Required. List of MovementEvent entries [1..16].
 * @param movementName       Optional. Human-readable name for debug use.
 * @param maneuverAssistList Optional. Maneuver assist data.
 */
public record MovementState(
        int signalGroup,
        List<MovementEvent> stateTimeSpeed,
        String movementName,
        List<ManeuverAssist> maneuverAssistList) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer signalGroup;
        private List<MovementEvent> stateTimeSpeed;
        private String movementName;
        private List<ManeuverAssist> maneuverAssistList;

        private Builder() {}

        public Builder signalGroup(int signalGroup) { this.signalGroup = signalGroup; return this; }
        public Builder stateTimeSpeed(List<MovementEvent> stateTimeSpeed) { this.stateTimeSpeed = stateTimeSpeed; return this; }
        public Builder movementName(String movementName) { this.movementName = movementName; return this; }
        public Builder maneuverAssistList(List<ManeuverAssist> maneuverAssistList) { this.maneuverAssistList = maneuverAssistList; return this; }

        public MovementState build() {
            return new MovementState(
                    requireNonNull(signalGroup, "signalGroup"),
                    requireNonNull(stateTimeSpeed, "stateTimeSpeed"),
                    movementName, maneuverAssistList);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

