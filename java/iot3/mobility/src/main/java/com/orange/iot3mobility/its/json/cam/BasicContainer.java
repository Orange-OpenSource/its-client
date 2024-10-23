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

/**
 * CAM BasicContainer.
 * <p>
 * Provides the type and position of a vehicle.
 */
public class BasicContainer {

    private static final Logger LOGGER = Logger.getLogger(BasicContainer.class.getName());

    private final JSONObject jsonBasicContainer = new JSONObject();

    /**
     * Type of the emitting ITS-station.
     * <p>
     * unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4), passengerCar(5), bus(6),
     * lightTruck(7), heavyTruck(8), trailer(9), specialVehicles(10), tram(11), roadSideUnit(15).
     */
    private final int stationType;

    /**
     * Position measured at the reference point of the originating ITS-S.
     * The measurement time shall correspond to generationDeltaTime.
     * <p>
     * If the station type of the originating ITS-S is set to one out of the values 3 to 11
     * the reference point shall be the ground position of the centre of the front side of
     * the bounding box of the vehicle.
     * <p>
     * See {@link Position}
     */
    private final Position position;

    /**
     * Confidence of the originating ITS-S position.
     * <p>
     * {@link PositionConfidence}
     */
    private final PositionConfidence positionConfidence;

    /**
     * Build a CAM BasicContainer.
     * <p>
     * These fields are mandatory.
     *
     * @param stationType {@link #stationType}
     * @param position {@link #position}
     */
    public BasicContainer(
            final int stationType,
            final Position position)
    {
        this(stationType, position, null);
    }

    /**
     * Build a CAM BasicContainer.
     * <p>
     * These fields are mandatory, except positionConfidence - use {@link #BasicContainer(int, Position)} if not known.
     *
     * @param stationType {@link #stationType}
     * @param position {@link #position}
     * @param positionConfidence {@link #positionConfidence}
     */
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
