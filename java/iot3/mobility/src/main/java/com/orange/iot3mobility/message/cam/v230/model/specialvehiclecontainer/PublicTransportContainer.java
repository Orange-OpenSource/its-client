package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * PublicTransportContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to publicTransport(1) this container shall be present.
 *
 * @param embarkationStatus Passenger embarkation is currently ongoing
 * @param ptActivation Optional {@link PtActivation} used for controlling traffic lights, barriers, bollards, etc.
 */
public record PublicTransportContainer(
        boolean embarkationStatus,
        PtActivation ptActivation) implements SpecialVehiclePayload {}
