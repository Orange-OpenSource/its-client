package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record SpecialTransportType(
        boolean heavyLoad,
        boolean excessWidth,
        boolean excessLength,
        boolean excessHeight) {}
