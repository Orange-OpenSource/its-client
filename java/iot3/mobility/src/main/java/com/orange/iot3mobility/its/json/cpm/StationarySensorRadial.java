/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StationarySensorRadial {

    private static final Logger LOGGER = Logger.getLogger(StationarySensorRadial.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.1 meter. The radial range of the sensor from the reference point or sensor point
     * offset.
     *
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int range;

    /**
     * Unit: 0.1 degrees. The orientation indicating the start of the stationary sensor’s
     * horizontal opening angle in positive angular direction with respect to the WGS84 coordinate
     * system.
     *
     * wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable (3601).
     */
    private final int horizontalOpeningAngleStart;

    /**
     * Unit: 0.1 degrees. The orientation indicating the end of the stationary sensor’s
     * horizontal opening angle in positive angular direction with respect to the WGS84 coordinate
     * system.
     *
     * wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable (3601).
     */
    private final int horizontalOpeningAngleEnd;

    /**
     * Unit: 0.1 degrees. The orientation indicating the start of the stationary sensor’s
     * vertical opening angle in positive angular direction of a Cartesian coordinate system
     * with its x-axis located in the north-east plane of the WGS84 coordinate system.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int verticalOpeningAngleStart;

    /**
     * Unit: 0.1 degrees. The orientation indicating the end of the stationary sensor’s
     * vertical opening angle in positive angular direction of a Cartesian coordinate system
     * with its x-axis located in the north-east plane of the WGS84 coordinate system.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int verticalOpeningAngleEnd;

    /**
     * The offset of the mounting point of this sensor from the station's reference position.
     */
    private final Offset sensorPositionOffset;

    public StationarySensorRadial(
            final int range,
            final int horizontalOpeningAngleStart,
            final int horizontalOpeningAngleEnd,
            final int verticalOpeningAngleStart,
            final int verticalOpeningAngleEnd,
            final Offset sensorPositionOffset
    ) throws IllegalArgumentException {
        if(range == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorRadial range is missing");
        } else if(CPM.isStrictMode() && (range > 10000 || range < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial range should be in the range of [0 - 10000]."
                    + " Value: " + range);
        }
        this.range = range;
        if(horizontalOpeningAngleStart == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorRadial horizontalOpeningAngleStart is missing");
        } else if(CPM.isStrictMode() && (horizontalOpeningAngleStart > 3601 || horizontalOpeningAngleStart < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial horizontalOpeningAngleStart should be in the range of [0 - 3601]."
                    + " Value: " + horizontalOpeningAngleStart);
        }
        this.horizontalOpeningAngleStart = horizontalOpeningAngleStart;
        if(horizontalOpeningAngleEnd == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorRadial horizontalOpeningAngleEnd is missing");
        } else if(CPM.isStrictMode() && (horizontalOpeningAngleEnd > 3601 || horizontalOpeningAngleEnd < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial horizontalOpeningAngleEnd should be in the range of [0 - 3601]."
                    + " Value: " + horizontalOpeningAngleEnd);
        }
        this.horizontalOpeningAngleEnd = horizontalOpeningAngleEnd;
        if(verticalOpeningAngleStart != UNKNOWN && CPM.isStrictMode()
                && (verticalOpeningAngleStart > 3601 || verticalOpeningAngleStart < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial verticalOpeningAngleStart should be in the range of [0 - 3601]."
                    + " Value: " + verticalOpeningAngleStart);
        }
        this.verticalOpeningAngleStart = verticalOpeningAngleStart;
        if(verticalOpeningAngleEnd != UNKNOWN && CPM.isStrictMode()
                && (verticalOpeningAngleEnd > 3601 || verticalOpeningAngleEnd < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial verticalOpeningAngleEnd should be in the range of [0 - 3601]."
                    + " Value: " + verticalOpeningAngleEnd);
        }
        this.verticalOpeningAngleEnd = verticalOpeningAngleEnd;
        this.sensorPositionOffset = sensorPositionOffset;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.StationarySensorRadial.RANGE.key(), range);
            json.put(JsonCpmKey.StationarySensorRadial.HORIZONTAL_OPENING_ANGLE_START.key(), horizontalOpeningAngleStart);
            json.put(JsonCpmKey.StationarySensorRadial.HORIZONTAL_OPENING_ANGLE_END.key(), horizontalOpeningAngleEnd);
            if(verticalOpeningAngleStart != UNKNOWN)
                json.put(JsonCpmKey.StationarySensorRadial.VERTICAL_OPENING_ANGLE_START.key(), verticalOpeningAngleStart);
            if(verticalOpeningAngleEnd != UNKNOWN)
                json.put(JsonCpmKey.StationarySensorRadial.VERTICAL_OPENING_ANGLE_END.key(), verticalOpeningAngleEnd);
            if(sensorPositionOffset != null)
                json.put(JsonCpmKey.StationarySensorRadial.SENSOR_POSITION_OFFSET.key(), sensorPositionOffset.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM StationarySensorRadial JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getRange() {
        return range;
    }

    public int getHorizontalOpeningAngleStart() {
        return horizontalOpeningAngleStart;
    }

    public int getHorizontalOpeningAngleEnd() {
        return horizontalOpeningAngleEnd;
    }

    public int getVerticalOpeningAngleStart() {
        return verticalOpeningAngleStart;
    }

    public int getVerticalOpeningAngleEnd() {
        return verticalOpeningAngleEnd;
    }

    public Offset getSensorPositionOffset() {
        return sensorPositionOffset;
    }

    public static StationarySensorRadial jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        try {
            int range = json.getInt(JsonCpmKey.StationarySensorRadial.RANGE.key());
            int horizontalOpeningAngleStart = json.getInt(JsonCpmKey.StationarySensorRadial.HORIZONTAL_OPENING_ANGLE_START.key());
            int horizontalOpeningAngleEnd = json.getInt(JsonCpmKey.StationarySensorRadial.HORIZONTAL_OPENING_ANGLE_END.key());
            int verticalOpeningAngleStart = json.optInt(JsonCpmKey.StationarySensorRadial.VERTICAL_OPENING_ANGLE_START.key(), UNKNOWN);
            int verticalOpeningAngleEnd = json.optInt(JsonCpmKey.StationarySensorRadial.VERTICAL_OPENING_ANGLE_END.key(), UNKNOWN);
            JSONObject jsonSensorPositionOffset = json.optJSONObject(JsonCpmKey.StationarySensorRadial.SENSOR_POSITION_OFFSET.key());
            Offset offset = Offset.jsonParser(jsonSensorPositionOffset);

            return new StationarySensorRadial(
                    range,
                    horizontalOpeningAngleStart,
                    horizontalOpeningAngleEnd,
                    verticalOpeningAngleStart,
                    verticalOpeningAngleEnd,
                    offset);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM StationarySensorRadial JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
