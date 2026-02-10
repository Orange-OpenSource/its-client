package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record RescueContainer(
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
