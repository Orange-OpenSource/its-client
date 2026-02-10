package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;

import java.util.List;
import java.util.Objects;

public record BasicVehicleContainerLowFrequency(
        int vehicleRole,
        ExteriorLights exteriorLights,
        List<PathPoint> pathHistory) {
    public BasicVehicleContainerLowFrequency {
        pathHistory = List.copyOf(Objects.requireNonNull(pathHistory));
    }
}
