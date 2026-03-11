package com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

/**
 * Information for an individual sensor.
 */
public record SensorInformation(
        int sensorId,
        int sensorType,
        Shape perceptionRegionShape,
        Integer perceptionRegionConfidence,
        boolean shadowingApplies) {}

