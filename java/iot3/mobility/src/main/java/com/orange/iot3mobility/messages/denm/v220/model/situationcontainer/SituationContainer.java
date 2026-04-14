/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.situationcontainer;

import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId;

import java.util.List;

/**
 * SituationContainer - DENM v2.2.0 situation container.
 *
 * @param informationQuality information quality (0-7)
 * @param eventType {@link CauseCode}
 * @param linkedCause Optional. {@link CauseCode}
 * @param eventZone Optional. {@link EventZone} list
 * @param linkedDenms Optional. {@link com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId} list (1 to 8)
 * @param eventEnd Optional. Unit: meter. outOfRange(8190), unavailable(8191)
 */
public record SituationContainer(
        int informationQuality,
        CauseCode eventType,
        CauseCode linkedCause,
        List<EventZone> eventZone,
        List<ActionId> linkedDenms,
        Integer eventEnd) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer informationQuality;
        private CauseCode eventType;
        private CauseCode linkedCause;
        private List<EventZone> eventZone;
        private List<ActionId> linkedDenms;
        private Integer eventEnd;

        public Builder informationQuality(int informationQuality) {
            this.informationQuality = informationQuality;
            return this;
        }

        public Builder eventType(CauseCode eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder linkedCause(CauseCode linkedCause) {
            this.linkedCause = linkedCause;
            return this;
        }

        public Builder eventZone(List<EventZone> eventZone) {
            this.eventZone = eventZone;
            return this;
        }

        public Builder linkedDenms(List<ActionId> linkedDenms) {
            this.linkedDenms = linkedDenms;
            return this;
        }

        public Builder eventEnd(Integer eventEnd) {
            this.eventEnd = eventEnd;
            return this;
        }

        public SituationContainer build() {
            return new SituationContainer(
                    requireNonNull(informationQuality, "information_quality"),
                    requireNonNull(eventType, "event_type"),
                    linkedCause,
                    eventZone,
                    linkedDenms,
                    eventEnd);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
