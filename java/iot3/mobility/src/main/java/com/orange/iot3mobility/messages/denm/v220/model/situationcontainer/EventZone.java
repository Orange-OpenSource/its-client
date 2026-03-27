/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.situationcontainer;

import com.orange.iot3mobility.messages.denm.v220.model.defs.DeltaReferencePosition;

/**
 * EventZone - event zone element.
 *
 * @param eventPosition {@link DeltaReferencePosition}
 * @param eventDeltaTime Optional. Unit: 10 millisecond. Range: 0-65535
 * @param informationQuality information quality (0-7)
 */
public record EventZone(
        DeltaReferencePosition eventPosition,
        Integer eventDeltaTime,
        int informationQuality) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DeltaReferencePosition eventPosition;
        private Integer eventDeltaTime;
        private Integer informationQuality;

        public Builder eventPosition(DeltaReferencePosition eventPosition) {
            this.eventPosition = eventPosition;
            return this;
        }

        public Builder eventDeltaTime(Integer eventDeltaTime) {
            this.eventDeltaTime = eventDeltaTime;
            return this;
        }

        public Builder informationQuality(int informationQuality) {
            this.informationQuality = informationQuality;
            return this;
        }

        public EventZone build() {
            return new EventZone(
                    requireNonNull(eventPosition, "event_position"),
                    eventDeltaTime,
                    requireNonNull(informationQuality, "information_quality"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
