/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationContainer {

    private static final Logger LOGGER = Logger.getLogger(LocationContainer.class.getName());

    private final JSONObject jsonLocationContainer = new JSONObject();
    private final int eventSpeed;
    private final int eventPositionHeading;
    private final Traces traces;
    private final int roadType;
    private final int eventSpeedConfidence;
    private final int eventPositionHeadingConfidence;

    public LocationContainer(
            final int roadType)
    {
        this(UNKNOWN, UNKNOWN, null, roadType, UNKNOWN, UNKNOWN);
    }

    public LocationContainer(
            final int eventSpeed,
            final int eventPositionHeading)
    {
        this(eventSpeed, eventPositionHeading, null, UNKNOWN, UNKNOWN, UNKNOWN);
    }

    public LocationContainer(
            final int eventSpeed,
            final int eventPositionHeading,
            final Traces traces,
            final int roadType,
            final int eventSpeedConfidence,
            final int eventPositionHeadingConfidence)
    {
        if(eventSpeed != UNKNOWN && (eventSpeed > 16383 || eventSpeed < 0)) {
            throw new IllegalArgumentException("DENM LocationContainer EventSpeed should be in the range of [0 - 16383]."
                    + " Value: " + eventSpeed);
        }
        this.eventSpeed = eventSpeed;
        if(eventPositionHeading != UNKNOWN && (eventPositionHeading > 3601 || eventPositionHeading < 0)) {
            throw new IllegalArgumentException("DENM LocationContainer EventPositionHeading should be in the range of [0 - 3601]."
                    + " Value: " + eventPositionHeading);
        }
        this.eventPositionHeading = eventPositionHeading;
        if(traces == null) this.traces = new Traces(null);
        else this.traces = traces;
        if(roadType != UNKNOWN && (roadType > 3 || roadType < 0)) {
            throw new IllegalArgumentException("DENM LocationContainer RoadType should be in the range of [0 - 3]."
                    + " Value: " + roadType);
        }
        this.roadType = roadType;
        if(eventSpeedConfidence != UNKNOWN && (eventSpeedConfidence > 127 || eventSpeedConfidence < 1)) {
            throw new IllegalArgumentException("DENM LocationContainer EventSpeedConfidence should be in the range of [1 - 127]."
                    + " Value: " + eventSpeedConfidence);
        }
        this.eventSpeedConfidence = eventSpeedConfidence;
        if(eventPositionHeadingConfidence != UNKNOWN && (eventPositionHeadingConfidence > 127 || eventPositionHeadingConfidence < 1)) {
            throw new IllegalArgumentException("DENM LocationContainer EventPositionHeadingConfidence should be in the range of [1 - 127]."
                    + " Value: " + eventPositionHeadingConfidence);
        }
        this.eventPositionHeadingConfidence = eventPositionHeadingConfidence;

        createJson();
    }

    private void createJson() {
        try {
            JSONObject confidence = new JSONObject();
            if(eventSpeedConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.EVENT_SPEED.key(), eventSpeedConfidence);
            if(eventPositionHeadingConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.EVENT_POSITION_HEADING.key(), eventPositionHeadingConfidence);

            if(eventSpeed != UNKNOWN)
                jsonLocationContainer.put(JsonKey.LocationContainer.EVENT_SPEED.key(), eventSpeed);
            if(eventPositionHeading != UNKNOWN)
                jsonLocationContainer.put(JsonKey.LocationContainer.EVENT_POSITION_HEADING.key(), eventPositionHeading);
            jsonLocationContainer.put(JsonKey.LocationContainer.TRACES.key(), traces.getJsonTraces());
            if(roadType != UNKNOWN)
                jsonLocationContainer.put(JsonKey.LocationContainer.ROAD_TYPE.key(), roadType);
            if(!confidence.isEmpty())
                jsonLocationContainer.put(JsonKey.LocationContainer.CONFIDENCE.key(), confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "LocationContainer JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJsonLocationContainer() {
        return jsonLocationContainer;
    }

    public int getEventSpeed() {
        return eventSpeed;
    }

    public int getEventPositionHeading() {
        return eventPositionHeading;
    }

    public Traces getTraces() {
        return traces;
    }

    public int getRoadType() {
        return roadType;
    }

    public int getEventSpeedConfidence() {
        return eventSpeedConfidence;
    }

    public int getEventPositionHeadingConfidence() {
        return eventPositionHeadingConfidence;
    }

    public static LocationContainer jsonParser(JSONObject jsonLocationContainer) {
        if(jsonLocationContainer == null || jsonLocationContainer.isEmpty()) return null;
        try {
            int eventSpeed = jsonLocationContainer.optInt(JsonKey.LocationContainer.EVENT_SPEED.key(), UNKNOWN);
            int eventPositionHeading = jsonLocationContainer.optInt(JsonKey.LocationContainer.EVENT_POSITION_HEADING.key(), UNKNOWN);
            JSONArray jsonTraces = jsonLocationContainer.getJSONArray(JsonKey.LocationContainer.TRACES.key());
            Traces traces = Traces.jsonParser(jsonTraces);
            int roadType = jsonLocationContainer.optInt(JsonKey.LocationContainer.ROAD_TYPE.key(), UNKNOWN);

            JSONObject confidence = jsonLocationContainer.optJSONObject(JsonKey.LocationContainer.CONFIDENCE.key());
            int eventSpeedConfidence = UNKNOWN;
            int eventPositionHeadingConfidence = UNKNOWN;
            if(confidence != null) {
                eventSpeedConfidence = confidence.optInt(JsonKey.Confidence.EVENT_SPEED.key(), UNKNOWN);
                eventPositionHeadingConfidence = confidence.optInt(JsonKey.Confidence.EVENT_POSITION_HEADING.key(), UNKNOWN);
            }

            return new LocationContainer(
                    eventSpeed,
                    eventPositionHeading,
                    traces,
                    roadType,
                    eventSpeedConfidence,
                    eventPositionHeadingConfidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "LocationContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
