package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record SafetyCarContainer(
        LightBarSiren lightBarSirenInUse,
        IncidentIndication incidentIndication,
        Integer trafficRule,
        Integer speedLimit) implements SpecialVehiclePayload {}
