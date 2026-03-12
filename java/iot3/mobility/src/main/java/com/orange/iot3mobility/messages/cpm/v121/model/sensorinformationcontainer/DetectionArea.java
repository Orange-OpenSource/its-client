package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaPolygon;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaRectangle;

/**
 * Detection area for a sensor.
 *
 * Exactly one of the detection area options should be provided.
 *
 * @param vehicleSensor {@link VehicleSensor}
 * @param stationarySensorRadial {@link StationarySensorRadial}
 * @param stationarySensorPolygon {@link AreaPolygon}
 * @param stationarySensorCircular {@link AreaCircular}
 * @param stationarySensorEllipse {@link AreaEllipse}
 * @param stationarySensorRectangle {@link AreaRectangle}
 */
public record DetectionArea(
        VehicleSensor vehicleSensor,
        StationarySensorRadial stationarySensorRadial,
        AreaPolygon stationarySensorPolygon,
        AreaCircular stationarySensorCircular,
        AreaEllipse stationarySensorEllipse,
        AreaRectangle stationarySensorRectangle) {}
