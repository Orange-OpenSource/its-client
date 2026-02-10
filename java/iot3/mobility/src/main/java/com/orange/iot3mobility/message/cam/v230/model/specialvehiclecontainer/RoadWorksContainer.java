package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record RoadWorksContainer(
        Integer roadWorksSubCauseCode,
        LightBarSiren lightBarSirenInUse,
        ClosedLanes closedLanes) implements SpecialVehiclePayload {}
