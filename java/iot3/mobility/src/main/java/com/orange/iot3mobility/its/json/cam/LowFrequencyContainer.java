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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CAM LowFrequencyContainer.
 * <p>
 * Optional, provides additional information about a vehicle.
 */
public class LowFrequencyContainer {

    private static final Logger LOGGER = Logger.getLogger(LowFrequencyContainer.class.getName());

    private final JSONObject jsonLowFrequencyContainer = new JSONObject();

    /**
     * The role of the vehicle ITS-S that originates the CAM.
     * <p>
     * default(0), publicTransport(1), specialTransport(2), dangerousGoods(3), roadWork(4), rescue(5), emergency(6),
     * safetyCar(7), agriculture(8),commercial(9),military(10),roadOperator(11),taxi(12), reserved1(13), reserved2(14),
     * reserved3(15)
     */
    private final int vehicleRole;

    /**
     * Status of the exterior light switches represented as a bit string.
     * <p>
     * lowBeamHeadlightsOn (0), highBeamHeadlightsOn (1), leftTurnSignalOn (2), rightTurnSignalOn (3),
     * daytimeRunningLightsOn (4), reverseLightOn (5), fogLightOn (6), parkingLightsOn (7)
     * <p>
     * Examples: "00000000", "10011010", "00000110"
     */
    private final String exteriorLights;

    /**
     * The path history of the originating ITS-S, a path with a set of path points.
     */
    private final PathHistory pathHistory;

    /**
     * Build a CAM LowFrequencyContainer.
     *
     * @param vehicleRole {@link #vehicleRole}
     */
    public LowFrequencyContainer(
            final int vehicleRole
    )
    {
        this(vehicleRole, "");
    }

    /**
     * Build a CAM LowFrequencyContainer.
     *
     * @param vehicleRole {@link #vehicleRole}
     * @param exteriorLights {@link #exteriorLights}
     */
    public LowFrequencyContainer(
            final int vehicleRole,
            final String exteriorLights
    )
    {
        this(vehicleRole, exteriorLights, new PathHistory(null));
    }

    /**
     * Build a CAM LowFrequencyContainer.
     *
     * @param vehicleRole {@link #vehicleRole}
     * @param exteriorLights {@link #exteriorLights}
     * @param pathHistory {@link #pathHistory}
     */
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
            if(!exteriorLights.isEmpty())
                jsonLowFrequencyContainer.put(JsonKey.LowFrequencyContainer.EXTERIOR_LIGHTS.key(), exteriorLights);
            jsonLowFrequencyContainer.put(JsonKey.LowFrequencyContainer.PATH_HISTORY.key(), pathHistory.getJsonPathHistory());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "LowFrequencyContainer JSON build error", "Error: " + e);
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
        if(jsonLowFreqContainer == null || jsonLowFreqContainer.isEmpty()) return null;
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
            LOGGER.log(Level.WARNING, "HighFrequencyContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
