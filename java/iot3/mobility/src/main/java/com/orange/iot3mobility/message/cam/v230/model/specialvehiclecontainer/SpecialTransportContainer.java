package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record SpecialTransportContainer(
        SpecialTransportType specialTransportType,
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
