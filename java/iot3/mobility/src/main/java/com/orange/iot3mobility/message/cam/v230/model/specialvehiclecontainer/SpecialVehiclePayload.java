package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public sealed interface SpecialVehiclePayload
        permits EmergencyContainer,
        DangerousGoodsContainer,
        PublicTransportContainer,
        RescueContainer,
        RoadWorksContainer,
        SafetyCarContainer,
        SpecialTransportContainer {}
