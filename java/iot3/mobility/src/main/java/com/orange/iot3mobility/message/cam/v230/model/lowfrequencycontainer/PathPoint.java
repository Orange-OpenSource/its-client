package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;

public record PathPoint(
        DeltaReferencePosition pathPosition,
        Integer pathDeltaTime) {}
