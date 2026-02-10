package com.orange.iot3mobility.message.cam.v113.model;

public record PositionConfidence(
        PositionConfidenceEllipse ellipse,
        int altitude) {}
