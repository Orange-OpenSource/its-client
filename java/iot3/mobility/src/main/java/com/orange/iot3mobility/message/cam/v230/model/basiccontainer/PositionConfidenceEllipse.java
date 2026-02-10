package com.orange.iot3mobility.message.cam.v230.model.basiccontainer;

public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {}
