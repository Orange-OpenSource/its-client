package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record DangerousGoodsContainer(
        int dangerousGoodsBasic) implements SpecialVehiclePayload {}
