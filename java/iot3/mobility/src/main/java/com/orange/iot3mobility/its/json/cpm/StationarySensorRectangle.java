/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StationarySensorRectangle {

    private static final Logger LOGGER = Logger.getLogger(StationarySensorRectangle.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Offset point about which the rectangle is centred with respect to the reference position.
     */
    private final Offset nodeCenterPoint;

    /**
     * Unit: 0.1 meter. Half length of the rectangle.
     *
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int semiMajorRangeLength;

    /**
     * Unit: 0.1 meter. Half width of the rectangle.
     *
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int semiMinorRangeLength;

    /**
     * Unit: 0.1 degrees.Orientation of the semi major range length of the rectangle in the WGS84
     * coordinate system.
     *
     * wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
     */
    private final int semiMajorRangeOrientation;

    /**
     * Unit: 0.1 meter.
     *
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int semiHeight;

    public StationarySensorRectangle(
            final Offset nodeCenterPoint,
            final int semiMajorRangeLength,
            final int semiMinorRangeLength,
            final int semiMajorRangeOrientation,
            final int semiHeight
    ) throws IllegalArgumentException {
        this.nodeCenterPoint = nodeCenterPoint;
        if(semiMajorRangeLength == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMajorRangeLength is missing");
        } else if(CPM.isStrictMode() && (semiMajorRangeLength > 10000 || semiMajorRangeLength < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMajorRangeLength should be in the range of [0 - 10000]."
                    + " Value: " + semiMajorRangeLength);
        }
        this.semiMajorRangeLength = semiMajorRangeLength;
        if(semiMinorRangeLength == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMinorRangeLength is missing");
        } else if(CPM.isStrictMode() && (semiMinorRangeLength > 10000 || semiMinorRangeLength < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMinorRangeLength should be in the range of [0 - 10000]."
                    + " Value: " + semiMinorRangeLength);
        }
        this.semiMinorRangeLength = semiMinorRangeLength;
        if(semiMajorRangeOrientation == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMajorRangeOrientation is missing");
        } else if(CPM.isStrictMode() && (semiMajorRangeOrientation > 3601 || semiMajorRangeOrientation < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiMajorRangeOrientation should be in the range of [0 - 3601]."
                    + " Value: " + semiMajorRangeOrientation);
        }
        this.semiMajorRangeOrientation = semiMajorRangeOrientation;
        if(semiHeight != UNKNOWN && CPM.isStrictMode()
                && (semiHeight > 10000 || semiHeight < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorEllipse semiHeight should be in the range of [0 - 10000]."
                    + " Value: " + semiHeight);
        }
        this.semiHeight = semiHeight;

        createJson();
    }

    private void createJson() {
        try {
            if(nodeCenterPoint != null)
                json.put(JsonCpmKey.StationarySensorEllipseRect.NODE_CENTER_POINT.key(), nodeCenterPoint.getJson());
            json.put(JsonCpmKey.StationarySensorEllipseRect.SEMI_MAJOR_RANGE_LENGTH.key(), semiMajorRangeLength);
            json.put(JsonCpmKey.StationarySensorEllipseRect.SEMI_MINOR_RANGE_LENGTH.key(), semiMinorRangeLength);
            json.put(JsonCpmKey.StationarySensorEllipseRect.SEMI_MAJOR_RANGE_ORIENTATION.key(), semiMajorRangeOrientation);
            if(semiHeight != UNKNOWN)
                json.put(JsonCpmKey.StationarySensorEllipseRect.SEMI_HEIGHT.key(), semiHeight);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM StationarySensorRectangle JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public Offset getNodeCenterPoint() {
        return nodeCenterPoint;
    }

    public int getSemiMajorRangeLength() {
        return semiMajorRangeLength;
    }

    public int getSemiMinorRangeLength() {
        return semiMinorRangeLength;
    }

    public int getSemiMajorRangeOrientation() {
        return semiMajorRangeOrientation;
    }

    public int getSemiHeight() {
        return semiHeight;
    }

    public static StationarySensorRectangle jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        try {
            JSONObject jsonNodeCenterPoint = json.optJSONObject(JsonCpmKey.StationarySensorEllipseRect.NODE_CENTER_POINT.key());
            Offset nodeCenterPoint = Offset.jsonParser(jsonNodeCenterPoint);
            int semiMajorRangeLength = json.getInt(JsonCpmKey.StationarySensorEllipseRect.SEMI_MAJOR_RANGE_LENGTH.key());
            int semiMinorRangeLength = json.getInt(JsonCpmKey.StationarySensorEllipseRect.SEMI_MINOR_RANGE_LENGTH.key());
            int semiMajorRangeOrientation = json.getInt(JsonCpmKey.StationarySensorEllipseRect.SEMI_MAJOR_RANGE_ORIENTATION.key());
            int semiHeight = json.optInt(JsonCpmKey.StationarySensorEllipseRect.NODE_CENTER_POINT.key(), UNKNOWN);

            return new StationarySensorRectangle(
                    nodeCenterPoint,
                    semiMajorRangeLength,
                    semiMinorRangeLength,
                    semiMajorRangeOrientation,
                    semiHeight);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM StationarySensorRectangle JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
