package com.orange.iot3mobility.message.cam.v113.model;

public record PositionConfidenceEllipse(
        int semiMajor,
        int semiMinor,
        int semiMajorOrientation) {}
