/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.locationcontainer;

import java.util.List;

/**
 * DetectionZone - detection zone approaching event position.
 *
 * @param path {@link PathPoint} list (max 40)
 */
public record DetectionZone(
        List<PathPoint> path) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<PathPoint> path;

        public Builder path(List<PathPoint> path) {
            this.path = path;
            return this;
        }

        public DetectionZone build() {
            return new DetectionZone(requireNonNull(path, "path"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
