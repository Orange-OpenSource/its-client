package com.orange.iot3mobility.message.cam.v113.model;

public record PathPoint(
        DeltaReferencePosition deltaPosition,
        Integer deltaTime) {}
