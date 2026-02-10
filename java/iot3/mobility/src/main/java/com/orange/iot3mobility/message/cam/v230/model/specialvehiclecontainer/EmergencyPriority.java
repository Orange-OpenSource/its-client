package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

public record EmergencyPriority(
        boolean requestForRightOfWay,
        boolean requestForFreeCrossingAtTrafficLight) {}
