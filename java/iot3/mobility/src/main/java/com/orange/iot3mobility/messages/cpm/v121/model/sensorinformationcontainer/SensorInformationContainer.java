package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import java.util.List;

/**
 * Sensor information container.
 *
 * @param sensorInformation List of sensor information. Size: [1..128].
 */
public record SensorInformationContainer(List<SensorInformation> sensorInformation) {}

