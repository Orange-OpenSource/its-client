/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorInformation {

    private final JSONObject json = new JSONObject();

    /**
     * Sensor identifier.
     */
    private final int sensorId;

    /**
     * Type of attached sensor.
     *
     * undefined(0), radar(1), lidar(2), monovideo(3), stereovision(4), nightvision(5),
     * ultrasonic(6), pmd(7), fusion(8), inductionloop(9), sphericalCamera(10),
     * itssaggregation(11).
     */
    private final int type;

    /**
     * Area covered by the sensor.
     */
    private final DetectionArea detectionArea;

    public SensorInformation(
        final int sensorId,
        final int type,
        final DetectionArea detectionArea
    ) throws IllegalArgumentException {
        if(sensorId == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM SensorInformation sensorId is missing");
        } else if(CPM.isStrictMode() && (sensorId > 255 || sensorId < 0)) {
            throw new IllegalArgumentException("CPM SensorInformation sensorId should be in the range of [0 - 255]."
                    + " Value: " + sensorId);
        }
        this.sensorId = sensorId;
        if(type == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM SensorInformation type is missing");
        } else if(CPM.isStrictMode() && (type > 15 || type < 0)) {
            throw new IllegalArgumentException("CPM SensorInformation type should be in the range of [0 - 15]."
                    + " Value: " + type);
        }
        this.type = type;
        if(detectionArea == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM SensorInformation detectionArea is missing");
        }
        this.detectionArea = detectionArea;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.SensorInformationContainer.SENSOR_ID.key(), sensorId);
            json.put(JsonCpmKey.SensorInformationContainer.TYPE.key(), type);
            json.put(JsonCpmKey.SensorInformationContainer.DETECTION_AREA.key(), detectionArea.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSensorId() {
        return sensorId;
    }

    public int getType() {
        return type;
    }

    public String getSensorType() {
        switch (type) {
            default:
            case 0:
                return "undefined";
            case 1:
                return "radar";
            case 2:
                return "lidar";
            case 3:
                return "mono video";
            case 4:
                return "stereo vision";
            case 5:
                return "night vision";
            case 6:
                return "ultrasonic";
            case 7:
                return "pmd";
            case 8:
                return "fusion";
            case 9:
                return "induction loop";
            case 10:
                return "spherical camera";
            case 11:
                return "its aggregation";

        }
    }

    public DetectionArea getDetectionArea() {
        return detectionArea;
    }

    public static SensorInformation jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            int sensorId = json.getInt(JsonCpmKey.SensorInformationContainer.SENSOR_ID.key());
            int type = json.getInt(JsonCpmKey.SensorInformationContainer.TYPE.key());
            JSONObject jsonDetectionArea = json.getJSONObject(JsonCpmKey.SensorInformationContainer.DETECTION_AREA.key());
            DetectionArea detectionArea = DetectionArea.jsonParser(jsonDetectionArea);

            return new SensorInformation(
                    sensorId,
                    type,
                    detectionArea);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
