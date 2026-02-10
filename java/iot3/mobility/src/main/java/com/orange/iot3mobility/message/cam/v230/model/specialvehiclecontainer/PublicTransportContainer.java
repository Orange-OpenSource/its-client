package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record PublicTransportContainer(
        boolean embarkationStatus,
        PtActivation ptActivation) implements SpecialVehiclePayload {}
