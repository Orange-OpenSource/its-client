package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record ClosedLanes(
        Integer innerHardShoulderStatus,
        Integer outerHardShoulderStatus,
        DrivingLaneStatus drivingLaneStatus) {}
