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

public class VehicleSensorProperty {

    private static final Logger LOGGER = Logger.getLogger(VehicleSensorProperty.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.1 meter. Range of sensor within the indicated azimuth angle defined by the start
     * and end opening angle.
     *
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int range;

    /**
     * Unit: 0.1 degrees. Start of the sensor's horizontal opening angle extension relative to the
     * body of the vehicle.
     *
     * The value is provided with respect to a body-fixed coordinate system according to the
     * ISO 8855 [i.2] specification with angles counted positive in the counter-clockwise direction
     * starting from the X-axis.
     *
     * The opening angle always extends from the horizontal opening angle start to horizontal
     * opening angle end in counter-clockwise direction.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int horizontalOpeningAngleStart;

    /**
     * Unit: 0.1 degrees. End of the sensor's horizontal opening angle extension relative to the
     * body of the vehicle.
     *
     * The value is provided with respect to a body-fixed coordinate system according to the
     * ISO 8855 [i.2] specification with angles counted positive in the counter-clockwise direction
     * starting from the X-axis.
     *
     * The opening angle always extends from the horizontal opening angle start to horizontal
     * opening angle end in counter-clockwise direction.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int horizontalOpeningAngleEnd;

    /**
     * Unit: 0.1 degrees. Start of the sensor's vertical opening angle extension.
     *
     * The angle refers to a rotation about the y-axis of a sensor-specific coordinate system with
     * its origin located at the location defined by the offset.
     *
     * The x-axis of the sensor's coordinate system points in the direction of half of the
     * horizontal opening angle.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int verticalOpeningAngleStart;

    /**
     * Unit: 0.1 degrees. End of the sensor's vertical opening angle extension.
     *
     * The angle refers to a rotation about the y-axis of a sensor-specific coordinate system with
     * its origin located at the location defined by the offset.
     *
     * The X-axis of the sensor's coordinate system points in the direction of half of the
     * horizontal opening angle.
     *
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int verticalOpeningAngleEnd;

    public VehicleSensorProperty(
            final int range,
            final int horizontalOpeningAngleStart,
            final int horizontalOpeningAngleEnd,
            final int verticalOpeningAngleStart,
            final int verticalOpeningAngleEnd
    ) throws IllegalArgumentException {
        if(range == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty range is missing");
        } else if(CPM.isStrictMode() && (range > 10000 || range < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty range should be in the range of [0 - 10000]."
                    + " Value: " + range);
        }
        this.range = range;
        if(horizontalOpeningAngleStart == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty horizontalOpeningAngleStart is missing");
        } else if(CPM.isStrictMode() && (horizontalOpeningAngleStart > 3601 || horizontalOpeningAngleStart < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty horizontalOpeningAngleStart should be in the range of [0 - 3601]."
                    + " Value: " + horizontalOpeningAngleStart);
        }
        this.horizontalOpeningAngleStart = horizontalOpeningAngleStart;
        if(horizontalOpeningAngleEnd == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty horizontalOpeningAngleEnd is missing");
        } else if(CPM.isStrictMode() && (horizontalOpeningAngleEnd > 3601 || horizontalOpeningAngleEnd < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty horizontalOpeningAngleEnd should be in the range of [0 - 3601]."
                    + " Value: " + horizontalOpeningAngleEnd);
        }
        this.horizontalOpeningAngleEnd = horizontalOpeningAngleEnd;
        if(verticalOpeningAngleStart != UNKNOWN && CPM.isStrictMode()
                && (verticalOpeningAngleStart > 3601 || verticalOpeningAngleStart < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty verticalOpeningAngleStart should be in the range of [0 - 3601]."
                    + " Value: " + verticalOpeningAngleStart);
        }
        this.verticalOpeningAngleStart = verticalOpeningAngleStart;
        if(verticalOpeningAngleEnd != UNKNOWN && CPM.isStrictMode()
                && (verticalOpeningAngleEnd > 3601 || verticalOpeningAngleEnd < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensorProperty verticalOpeningAngleEnd should be in the range of [0 - 3601]."
                    + " Value: " + verticalOpeningAngleEnd);
        }
        this.verticalOpeningAngleEnd = verticalOpeningAngleEnd;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.VehicleSensorProperty.RANGE.key(), range);
            json.put(JsonCpmKey.VehicleSensorProperty.HORIZONTAL_OPENING_ANGLE_START.key(), horizontalOpeningAngleStart);
            json.put(JsonCpmKey.VehicleSensorProperty.HORIZONTAL_OPENING_ANGLE_END.key(), horizontalOpeningAngleEnd);
            if(verticalOpeningAngleStart != UNKNOWN)
                json.put(JsonCpmKey.VehicleSensorProperty.VERTICAL_OPENING_ANGLE_START.key(), verticalOpeningAngleStart);
            if(verticalOpeningAngleEnd != UNKNOWN)
                json.put(JsonCpmKey.VehicleSensorProperty.VERTICAL_OPENING_ANGLE_END.key(), verticalOpeningAngleEnd);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM VehicleSensorProperty JSON build error", "Error: " + e);
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

    public static VehicleSensorProperty jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        try {
            int range = json.getInt(JsonCpmKey.VehicleSensorProperty.RANGE.key());
            int horizontalOpeningAngleStart = json.getInt(JsonCpmKey.VehicleSensorProperty.HORIZONTAL_OPENING_ANGLE_START.key());
            int horizontalOpeningAngleEnd = json.getInt(JsonCpmKey.VehicleSensorProperty.HORIZONTAL_OPENING_ANGLE_END.key());
            int verticalOpeningAngleStart = json.optInt(JsonCpmKey.VehicleSensorProperty.VERTICAL_OPENING_ANGLE_START.key(), UNKNOWN);
            int verticalOpeningAngleEnd = json.optInt(JsonCpmKey.VehicleSensorProperty.VERTICAL_OPENING_ANGLE_END.key(), UNKNOWN);

            return new VehicleSensorProperty(
                    range,
                    horizontalOpeningAngleStart,
                    horizontalOpeningAngleEnd,
                    verticalOpeningAngleStart,
                    verticalOpeningAngleEnd);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM VehicleSensorProperty JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
