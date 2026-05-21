/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model.intersection;

/**
 * Recommended traveling approach speed for a movement event.
 *
 * @param type       Required. Advisory speed type: none(0), greenwave(1), ecoDrive(2), transit(3).
 * @param speed      Optional. Speed advice in 0.1 m/s steps [0..500]. 500 = unavailable.
 * @param confidence Optional. Speed confidence [0..7].
 * @param distance   Optional. Zone length in meters [0..10000].
 * @param classId    Optional. Restriction class ID [0..255].
 */
public record AdvisorySpeed(
        int type,
        Integer speed,
        Integer confidence,
        Integer distance,
        Integer classId) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer type;
        private Integer speed;
        private Integer confidence;
        private Integer distance;
        private Integer classId;

        private Builder() {}

        public Builder type(int type) { this.type = type; return this; }
        public Builder speed(Integer speed) { this.speed = speed; return this; }
        public Builder confidence(Integer confidence) { this.confidence = confidence; return this; }
        public Builder distance(Integer distance) { this.distance = distance; return this; }
        public Builder classId(Integer classId) { this.classId = classId; return this; }

        public AdvisorySpeed build() {
            return new AdvisorySpeed(
                    requireNonNull(type, "type"),
                    speed, confidence, distance, classId);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

