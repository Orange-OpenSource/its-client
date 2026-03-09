/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.basiccontainer;

import com.orange.iot3mobility.message.StationType;

/**
 * BasicContainer v2.3.0
 *
 * @param stationType {@link StationType} Integer value
 * @param referencePosition {@link ReferencePosition}
 */
public record BasicContainer(
        int stationType,
        ReferencePosition referencePosition) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for BasicContainer.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>stationType</li>
     * <li>referencePosition</li>
     * </ul>
     */
    public static final class Builder {
        private Integer stationType;
        private ReferencePosition referencePosition;

        private Builder() {}

        public Builder stationType(int stationType) {
            this.stationType = stationType;
            return this;
        }

        public Builder referencePosition(ReferencePosition referencePosition) {
            this.referencePosition = referencePosition;
            return this;
        }

        public BasicContainer build() {
            return new BasicContainer(
                    requireNonNull(stationType, "station_type"),
                    requireNonNull(referencePosition, "reference_position"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
