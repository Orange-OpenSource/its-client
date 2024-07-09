/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cam;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.PathHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LowFrequencyContainer {

    private final JSONObject jsonLowFrequencyContainer = new JSONObject();
    private final int vehicleRole;
    private final String exteriorLights;
    private final PathHistory pathHistory;

    public LowFrequencyContainer(
            final int vehicleRole
    )
    {
        this(vehicleRole, "");
    }

    public LowFrequencyContainer(
            final int vehicleRole,
            final String exteriorLights
    )
    {
        this(vehicleRole, exteriorLights, new PathHistory(null));
    }

    public LowFrequencyContainer(
            final int vehicleRole,
            final String exteriorLights,
            final PathHistory pathHistory
    )
    {
        if(vehicleRole > 15 || vehicleRole < 0) {
            throw new IllegalArgumentException("CAM LowFrequencyContainer VehicleRole should be in the range of [0 - 15]."
                    + " Value: " + vehicleRole);
        }
        this.vehicleRole = vehicleRole;
        this.exteriorLights = exteriorLights;
        this.pathHistory = pathHistory;

        createJson();
    }

    private void createJson() {
        try {
            jsonLowFrequencyContainer.put(JsonKey.LowFrequencyContainer.VEHICLE_ROLE.key(), vehicleRole);
            if(!exteriorLights.equals(""))
                jsonLowFrequencyContainer.put(JsonKey.LowFrequencyContainer.EXTERIOR_LIGHTS.key(), exteriorLights);
            jsonLowFrequencyContainer.put(JsonKey.LowFrequencyContainer.PATH_HISTORY.key(), pathHistory.getJsonPathHistory());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonLowFrequencyContainer() {
        return jsonLowFrequencyContainer;
    }

    public int getVehicleRole() {
        return vehicleRole;
    }

    public String getExteriorLights() {
        return exteriorLights;
    }

    public PathHistory getPathHistory() {
        return pathHistory;
    }

    public static LowFrequencyContainer jsonParser(JSONObject jsonLowFreqContainer) {
        if(jsonLowFreqContainer == null || jsonLowFreqContainer.length() == 0) return null;
        try {
            int vehicleRole = jsonLowFreqContainer.getInt(JsonKey.LowFrequencyContainer.VEHICLE_ROLE.key());
            String exteriorLights = jsonLowFreqContainer.optString(JsonKey.LowFrequencyContainer.EXTERIOR_LIGHTS.key());
            JSONArray jsonPathHistory = jsonLowFreqContainer.optJSONArray(JsonKey.LowFrequencyContainer.PATH_HISTORY.key());
            PathHistory pathHistory = PathHistory.jsonParser(jsonPathHistory);

            return new LowFrequencyContainer(
                    vehicleRole,
                    exteriorLights,
                    pathHistory);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
