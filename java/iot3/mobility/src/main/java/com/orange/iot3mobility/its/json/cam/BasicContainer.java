/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cam;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.PositionConfidence;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicContainer {

    private static final Logger LOGGER = Logger.getLogger(CAM.class.getName());

    private final JSONObject jsonBasicContainer = new JSONObject();
    private final int stationType;
    private final Position position;
    private final PositionConfidence positionConfidence;

    public BasicContainer(
            final int stationType,
            final Position position)
    {
        this(stationType, position, null);
    }

    public BasicContainer(
            final int stationType,
            final Position position,
            final PositionConfidence positionConfidence)
    {
        if(stationType > 255 || stationType < 0) {
            throw new IllegalArgumentException("CAM BasicContainer StationType should be in the range of [0 - 255]."
                    + " Value: " + stationType);
        }
        this.stationType = stationType;
        if(position == null) {
            throw new IllegalArgumentException("CAM BasicContainer Position missing.");
        }
        this.position = position;
        this.positionConfidence = positionConfidence;

        createJson();
    }

    private void createJson() {
        try {
            jsonBasicContainer.put(JsonKey.BasicContainer.STATION_TYPE.key(), stationType);
            jsonBasicContainer.put(JsonKey.BasicContainer.POSITION.key(), position.getJson());
            if(positionConfidence != null)
                jsonBasicContainer.put(JsonKey.Position.CONFIDENCE.key(), positionConfidence.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "BasicContainer JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJsonBasicContainer() {
        return jsonBasicContainer;
    }

    public int getStationType() {
        return stationType;
    }

    public Position getPosition() {
        return position;
    }

    public PositionConfidence getPositionConfidence() {
        return positionConfidence;
    }

    public static BasicContainer jsonParser(JSONObject jsonBasicContainer) {
        if(jsonBasicContainer == null || jsonBasicContainer.isEmpty()) return null;
        try {
            int stationType = jsonBasicContainer.getInt(JsonKey.BasicContainer.STATION_TYPE.key());
            JSONObject jsonPosition = jsonBasicContainer.getJSONObject(JsonKey.BasicContainer.POSITION.key());
            Position position = Position.jsonParser(jsonPosition);
            JSONObject jsonPositionConfidence = jsonBasicContainer.optJSONObject(JsonKey.Position.CONFIDENCE.key());
            PositionConfidence positionConfidence = PositionConfidence.jsonParser(jsonPositionConfidence);

            return new BasicContainer(stationType, position, positionConfidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "BasicContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
