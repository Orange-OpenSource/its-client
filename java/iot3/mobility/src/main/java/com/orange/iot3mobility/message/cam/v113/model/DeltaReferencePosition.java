package com.orange.iot3mobility.message.cam.v113.model;

public record DeltaReferencePosition(
        int deltaLatitude,
        int deltaLongitude,
        int deltaAltitude) {}
