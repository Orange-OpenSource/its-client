/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorInformationContainer {

    private static final Logger LOGGER = Logger.getLogger(SensorInformationContainer.class.getName());

    private final JSONArray jsonArray = new JSONArray();

    /**
     * List of sensors and their information.
     */
    private final List<SensorInformation> sensorInformationList;

    public SensorInformationContainer(
            final List<SensorInformation> sensorInformationList
    ) {
        this.sensorInformationList = sensorInformationList;

        createJson();
    }

    private void createJson() {
        for(SensorInformation sensorInformation: sensorInformationList) {
            jsonArray.put(sensorInformation.getJson());
        }
    }

    public JSONArray getJson() {
        return jsonArray;
    }

    public List<SensorInformation> getSensorInformationList() {
        return sensorInformationList;
    }

    public List<String> getSensorTypeList() {
        ArrayList<String> sensorTypeList = new ArrayList<>();
        for(SensorInformation sensorInformation: sensorInformationList) {
            sensorTypeList.add(sensorInformation.getSensorType());
        }
        return sensorTypeList;
    }

    public static SensorInformationContainer jsonParser(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.isEmpty()) return null;
        try {
            ArrayList<SensorInformation> sensorInformationList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                SensorInformation sensorInformation = SensorInformation.jsonParser(jsonArray.getJSONObject(i));
                sensorInformationList.add(sensorInformation);
            }

            return new SensorInformationContainer(sensorInformationList);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM SensorInformationContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
