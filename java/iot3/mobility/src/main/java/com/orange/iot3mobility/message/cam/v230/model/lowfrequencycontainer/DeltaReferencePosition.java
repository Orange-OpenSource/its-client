package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;

public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {}
