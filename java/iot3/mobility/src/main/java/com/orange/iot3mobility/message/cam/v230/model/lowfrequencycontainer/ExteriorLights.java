package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;


public record ExteriorLights(
        boolean lowBeamHeadlightsOn,
        boolean highBeamHeadlightsOn,
        boolean leftTurnSignalOn,
        boolean rightTurnSignalOn,
        boolean daytimeRunningLightsOn,
        boolean reverseLightOn,
        boolean fogLightOn,
        boolean parkingLightsOn) {}
