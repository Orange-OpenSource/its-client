package com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer;

/**
 * Management container.
 *
 * @param stationType Station type. Value: unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4),
 *                    passengerCar(5), bus(6), lightTruck(7), heavyTruck(8), trailer(9), specialVehicles(10), tram(11),
 *                    roadSideUnit(15).
 * @param referencePosition {@link ReferencePosition}
 * @param confidence {@link ManagementConfidence}
 */
public record ManagementContainer(
        int stationType,
        ReferencePosition referencePosition,
        ManagementConfidence confidence) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer stationType;
        private ReferencePosition referencePosition;
        private ManagementConfidence confidence;

        public Builder stationType(int stationType) {
            this.stationType = stationType;
            return this;
        }

        public Builder referencePosition(ReferencePosition referencePosition) {
            this.referencePosition = referencePosition;
            return this;
        }

        public Builder confidence(ManagementConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public ManagementContainer build() {
            return new ManagementContainer(
                    requireNonNull(stationType, "station_type"),
                    requireNonNull(referencePosition, "reference_position"),
                    requireNonNull(confidence, "confidence"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
