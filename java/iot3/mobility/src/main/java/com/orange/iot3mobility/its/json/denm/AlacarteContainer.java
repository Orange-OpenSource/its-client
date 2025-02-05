/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AlacarteContainer {

    private static final Logger LOGGER = Logger.getLogger(AlacarteContainer.class.getName());

    private final JSONObject jsonAlacarteContainer = new JSONObject();
    private final int lanePosition;
    private final int impactReduction;
    private final int externalTemperature;
    private final int roadWorks;
    private final int positionSolutionType;

    public AlacarteContainer(
            final int lanePosition)
    {
        this(lanePosition, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);
    }

    public AlacarteContainer(
            final int lanePosition,
            final int positionSolutionType)
    {
        this(lanePosition, UNKNOWN, UNKNOWN, UNKNOWN, positionSolutionType);
    }

    public AlacarteContainer(
            final int lanePosition,
            final int impactReduction,
            final int externalTemperature,
            final int roadWorks,
            final int positionSolutionType)
    {
        if(lanePosition != UNKNOWN && (lanePosition > 14 || lanePosition < -1)) {
            throw new IllegalArgumentException("DENM AlacarteContainer LanePosition should be in the range of [-1 - 14]."
                    + " Value: " + lanePosition);
        }
        this.lanePosition = lanePosition;
        this.impactReduction = impactReduction;
        this.externalTemperature = externalTemperature;
        this.roadWorks = roadWorks;
        if(positionSolutionType != UNKNOWN && (positionSolutionType > 5 || positionSolutionType < 0)) {
            throw new IllegalArgumentException("DENM AlacarteContainer PositionSolutionType should be in the range of [0 - 5]."
                    + " Value: " + lanePosition);
        }
        this.positionSolutionType = positionSolutionType;

        createJson();
    }

    private void createJson() {
        try {
            if(lanePosition != UNKNOWN)
                jsonAlacarteContainer.put(JsonKey.AlacarteContainer.LANE_POSITION.key(), lanePosition);
            if(impactReduction != UNKNOWN)
                jsonAlacarteContainer.put(JsonKey.AlacarteContainer.IMPACT_REDUCTION.key(), impactReduction);
            if(externalTemperature != UNKNOWN)
                jsonAlacarteContainer.put(JsonKey.AlacarteContainer.EXTERNAL_TEMPERATURE.key(), externalTemperature);
            if(roadWorks != UNKNOWN)
                jsonAlacarteContainer.put(JsonKey.AlacarteContainer.ROAD_WORKS.key(), roadWorks);
            if(positionSolutionType != UNKNOWN)
                jsonAlacarteContainer.put(JsonKey.AlacarteContainer.POSITION_SOLUTION_TYPE.key(), positionSolutionType);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "AlaCarteContainer JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJsonAlacarteContainer() {
        return jsonAlacarteContainer;
    }

    public int getLanePosition() {
        return lanePosition;
    }

    public int getImpactReduction() {
        return impactReduction;
    }

    public int getExternalTemperature() {
        return externalTemperature;
    }

    public int getRoadWorks() {
        return roadWorks;
    }

    public int getPositionSolutionType() {
        return positionSolutionType;
    }

    public static AlacarteContainer jsonParser(JSONObject jsonAlacarteContainer) {
        if(JsonUtil.isNullOrEmpty(jsonAlacarteContainer)) return null;
        int lanePosition = jsonAlacarteContainer.optInt(JsonKey.AlacarteContainer.LANE_POSITION.key(), UNKNOWN);
        int impactReduction = jsonAlacarteContainer.optInt(JsonKey.AlacarteContainer.IMPACT_REDUCTION.key(), UNKNOWN);
        final int externalTemperature = jsonAlacarteContainer.optInt(JsonKey.AlacarteContainer.EXTERNAL_TEMPERATURE.key(), UNKNOWN);
        final int roadWorks = jsonAlacarteContainer.optInt(JsonKey.AlacarteContainer.ROAD_WORKS.key(), UNKNOWN);
        final int positioningSolution = jsonAlacarteContainer.optInt(JsonKey.AlacarteContainer.POSITION_SOLUTION_TYPE.key(), UNKNOWN);

        return new AlacarteContainer(
                lanePosition,
                impactReduction,
                externalTemperature,
                roadWorks,
                positioningSolution);
    }

}
