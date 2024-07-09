/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetectionArea {

    private final JSONObject json = new JSONObject();

    /**
     * If the emitting ITS-station is a vehicle.
     */
    private final VehicleSensor vehicleSensor;

    /**
     * If the emitting ITS-station is a RSU, and the sensor's detection area is radial.
     */
    private final StationarySensorRadial stationarySensorRadial;

    /**
     * If the emitting ITS-station is a RSU, and the sensor's detection area is polygonal.
     */
    private final StationarySensorPolygon stationarySensorPolygon;

    /**
     * If the emitting ITS-station is a RSU, and the sensor's detection area is circular.
     */
    private final StationarySensorCircular stationarySensorCircular;

    /**
     * If the emitting ITS-station is a RSU, and the sensor's detection area is ellipsoidal.
     */
    private final StationarySensorEllipse stationarySensorEllipse;

    /**
     * If the emitting ITS-station is a RSU, and the sensor's detection area is rectangular.
     */
    private final StationarySensorRectangle stationarySensorRectangle;

    public DetectionArea(
            final VehicleSensor vehicleSensor
    ) throws IllegalArgumentException {
        this(vehicleSensor,
                null,
                null,
                null,
                null,
                null);
    }

    public DetectionArea(
            final StationarySensorRadial stationarySensorRadial
    ) throws IllegalArgumentException {
        this(null,
                stationarySensorRadial,
                null,
                null,
                null,
                null);
    }

    public DetectionArea(
            final StationarySensorPolygon stationarySensorPolygon
    ) throws IllegalArgumentException {
        this(null,
                null,
                stationarySensorPolygon,
                null,
                null,
                null);
    }

    public DetectionArea(
            final StationarySensorCircular stationarySensorCircular
    ) throws IllegalArgumentException {
        this(null,
                null,
                null,
                stationarySensorCircular,
                null,
                null);
    }

    public DetectionArea(
            final StationarySensorEllipse stationarySensorEllipse
    ) throws IllegalArgumentException {
        this(null,
                null,
                null,
                null,
                stationarySensorEllipse,
                null);
    }

    public DetectionArea(
            final StationarySensorRectangle stationarySensorRectangle
    ) throws IllegalArgumentException {
        this(null,
                null,
                null,
                null,
                null,
                stationarySensorRectangle);
    }

    public DetectionArea(
            final VehicleSensor vehicleSensor,
            final StationarySensorRadial stationarySensorRadial,
            final StationarySensorPolygon stationarySensorPolygon,
            final StationarySensorCircular stationarySensorCircular,
            final StationarySensorEllipse stationarySensorEllipse,
            final StationarySensorRectangle stationarySensorRectangle
    ) throws IllegalArgumentException {
        this.vehicleSensor = vehicleSensor;
        this.stationarySensorRadial = stationarySensorRadial;
        this.stationarySensorPolygon = stationarySensorPolygon;
        this.stationarySensorCircular = stationarySensorCircular;
        this.stationarySensorEllipse = stationarySensorEllipse;
        this.stationarySensorRectangle = stationarySensorRectangle;

        createJson();
    }

    private void createJson() {
        try {
            if(vehicleSensor != null)
                json.put(JsonCpmKey.DetectionArea.VEHICLE_SENSOR.key(), vehicleSensor.getJson());
            else if(stationarySensorRadial != null)
                json.put(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_RADIAL.key(), stationarySensorRadial.getJson());
            else if(stationarySensorPolygon != null)
                json.put(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_POLYGON.key(), stationarySensorPolygon.getJson());
            else if(stationarySensorCircular != null)
                json.put(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_CIRCULAR.key(), stationarySensorCircular.getJson());
            else if(stationarySensorEllipse != null)
                json.put(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_ELLIPSE.key(), stationarySensorEllipse.getJson());
            else if(stationarySensorRectangle != null)
                json.put(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_RECTANGLE.key(), stationarySensorRectangle.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public VehicleSensor getVehicleSensor() {
        return vehicleSensor;
    }

    public StationarySensorRadial getStationarySensorRadial() {
        return stationarySensorRadial;
    }

    public StationarySensorPolygon getStationarySensorPolygon() {
        return stationarySensorPolygon;
    }

    public StationarySensorCircular getStationarySensorCircular() {
        return stationarySensorCircular;
    }

    public StationarySensorEllipse getStationarySensorEllipse() {
        return stationarySensorEllipse;
    }

    public StationarySensorRectangle getStationarySensorRectangle() {
        return stationarySensorRectangle;
    }

    public static DetectionArea jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        JSONObject jsonVehicleSensor = json.optJSONObject(JsonCpmKey.DetectionArea.VEHICLE_SENSOR.key());
        VehicleSensor vehicleSensor = VehicleSensor.jsonParser(jsonVehicleSensor);
        JSONObject jsonStationarySensorRadial = json.optJSONObject(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_RADIAL.key());
        StationarySensorRadial stationarySensorRadial = StationarySensorRadial.jsonParser(jsonStationarySensorRadial);
        JSONArray jsonStationarySensorPolygon = json.optJSONArray(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_POLYGON.key());
        StationarySensorPolygon stationarySensorPolygon = StationarySensorPolygon.jsonParser(jsonStationarySensorPolygon);
        JSONObject jsonStationarySensorCircular = json.optJSONObject(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_CIRCULAR.key());
        StationarySensorCircular stationarySensorCircular = StationarySensorCircular.jsonParser(jsonStationarySensorCircular);
        JSONObject jsonStationarySensorEllipse = json.optJSONObject(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_ELLIPSE.key());
        StationarySensorEllipse stationarySensorEllipse = StationarySensorEllipse.jsonParser(jsonStationarySensorEllipse);
        JSONObject jsonStationarySensorRectangle = json.optJSONObject(JsonCpmKey.DetectionArea.STATIONARY_SENSOR_RECTANGLE.key());
        StationarySensorRectangle stationarySensorRectangle = StationarySensorRectangle.jsonParser(jsonStationarySensorRectangle);

        return new DetectionArea(
                vehicleSensor,
                stationarySensorRadial,
                stationarySensorPolygon,
                stationarySensorCircular,
                stationarySensorEllipse,
                stationarySensorRectangle);
    }

}
