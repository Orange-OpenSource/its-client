package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Station data container.
 *
 * Exactly one of originating_vehicle_container or originating_rsu_container must be provided.
 *
 * @param originatingVehicleContainer Optional {@link OriginatingVehicleContainer}
 * @param originatingRsuContainer Optional {@link OriginatingRsuContainer}
 */
public record StationDataContainer(
        OriginatingVehicleContainer originatingVehicleContainer,
        OriginatingRsuContainer originatingRsuContainer) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private OriginatingVehicleContainer originatingVehicleContainer;
        private OriginatingRsuContainer originatingRsuContainer;

        public Builder originatingVehicleContainer(OriginatingVehicleContainer originatingVehicleContainer) {
            this.originatingVehicleContainer = originatingVehicleContainer;
            return this;
        }

        public Builder originatingRsuContainer(OriginatingRsuContainer originatingRsuContainer) {
            this.originatingRsuContainer = originatingRsuContainer;
            return this;
        }

        public StationDataContainer build() {
            return new StationDataContainer(originatingVehicleContainer, originatingRsuContainer);
        }

    }
}

