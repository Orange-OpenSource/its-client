package com.orange.iot3mobility.message.cam.v113.model;

import com.orange.iot3mobility.message.StationType;

/**
 * BasicContainer v1.1.3
 *
 * @param stationType {@link StationType} Integer value
 * @param referencePosition {@link ReferencePosition}
 * @param confidence {@link PositionConfidence}
 */
public record BasicContainer(
        int stationType,
        ReferencePosition referencePosition,
        PositionConfidence confidence) {

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
        private PositionConfidence confidence;

        private Builder() {}

        public Builder stationType(int stationType) {
            this.stationType = stationType;
            return this;
        }

        public Builder referencePosition(ReferencePosition referencePosition) {
            this.referencePosition = referencePosition;
            return this;
        }

        public Builder positionConfidence(PositionConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public BasicContainer build() {
            return new BasicContainer(
                    requireNonNull(stationType, "station_type"),
                    requireNonNull(referencePosition, "reference_position"),
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
