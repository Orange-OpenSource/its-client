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
        OriginatingRsuContainer originatingRsuContainer) {}

