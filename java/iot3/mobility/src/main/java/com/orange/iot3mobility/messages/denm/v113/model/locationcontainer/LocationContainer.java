/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model.locationcontainer;

import java.util.List;

/**
 * LocationContainer - DENM v1.1.3 location container.
 *
 * @param eventSpeed Optional. Unit: 0.01 m/s. standstill(0), oneCentimeterPerSec(1), unavailable(16383)
 * @param eventPositionHeading Optional. Unit: 0.1 degree. wgs84North(0), wgs84East(900), wgs84South(1800),
 *                             wgs84West(2700), unavailable(3601)
 * @param traces {@link PathHistory} (1 to 7 path histories)
 * @param roadType Optional. Type of road segment. Range: 0-3
 * @param confidence Optional. {@link LocationConfidence}
 */
public record LocationContainer(
        Integer eventSpeed,
        Integer eventPositionHeading,
        List<PathHistory> traces,
        Integer roadType,
        LocationConfidence confidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer eventSpeed;
        private Integer eventPositionHeading;
        private List<PathHistory> traces;
        private Integer roadType;
        private LocationConfidence confidence;

        public Builder eventSpeed(Integer eventSpeed) {
            this.eventSpeed = eventSpeed;
            return this;
        }

        public Builder eventPositionHeading(Integer eventPositionHeading) {
            this.eventPositionHeading = eventPositionHeading;
            return this;
        }

        public Builder traces(List<PathHistory> traces) {
            this.traces = traces;
            return this;
        }

        public Builder roadType(Integer roadType) {
            this.roadType = roadType;
            return this;
        }

        public Builder confidence(LocationConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public LocationContainer build() {
            return new LocationContainer(
                    eventSpeed,
                    eventPositionHeading,
                    requireNonNull(traces, "traces"),
                    roadType,
                    confidence);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
