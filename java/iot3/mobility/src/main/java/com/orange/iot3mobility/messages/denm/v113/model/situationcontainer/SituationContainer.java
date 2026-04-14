/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.situationcontainer;

/**
 * SituationContainer - DENM v1.1.3 situation container.
 *
 * @param informationQuality Optional. Quality of information. Range: 0-7 (unavailable=0)
 * @param eventType event type
 * @param linkedCause Optional. Linked cause
 */
public record SituationContainer(
        Integer informationQuality,
        EventType eventType,
        LinkedCause linkedCause) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer informationQuality;
        private EventType eventType;
        private LinkedCause linkedCause;

        public Builder informationQuality(Integer informationQuality) {
            this.informationQuality = informationQuality;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder linkedCause(LinkedCause linkedCause) {
            this.linkedCause = linkedCause;
            return this;
        }

        public SituationContainer build() {
            return new SituationContainer(
                    informationQuality,
                    requireNonNull(eventType, "event_type"),
                    linkedCause);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
