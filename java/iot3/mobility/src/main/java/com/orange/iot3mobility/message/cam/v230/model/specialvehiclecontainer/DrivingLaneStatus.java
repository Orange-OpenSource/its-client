package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record DrivingLaneStatus(
        boolean lane1Closed,
        boolean lane2Closed,
        boolean lane3Closed,
        boolean lane4Closed,
        boolean lane5Closed,
        boolean lane6Closed,
        boolean lane7Closed,
        boolean lane8Closed,
        boolean lane9Closed,
        boolean lane10Closed,
        boolean lane11Closed,
        boolean lane12Closed,
        boolean lane13Closed) {}