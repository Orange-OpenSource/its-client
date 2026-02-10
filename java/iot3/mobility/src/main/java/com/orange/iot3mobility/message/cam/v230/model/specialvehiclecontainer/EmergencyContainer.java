package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record EmergencyContainer(
        LightBarSiren lightBarSirenInUse,
        IncidentIndication incidentIndication,
        EmergencyPriority emergencyPriority) implements SpecialVehiclePayload {}
