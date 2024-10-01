/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class StationarySensorCircular {

    private final JSONObject json = new JSONObject();

    /**
     * Offset point about which the circle is centred with respect to the reference position.
     */
    private final Offset nodeCenterPoint;

    /**
     * Unit: 0.1 meter. The radius of the circular area.
     *
     * zeroPointZeroOneMeter(1), oneMeter(10).
     */
    private final int radius;

    public StationarySensorCircular(
            final int radius
    ) throws IllegalArgumentException {
        this(null, radius);
    }

    public StationarySensorCircular(
            final Offset nodeCenterPoint,
            final int radius
    ) throws IllegalArgumentException {
        this.nodeCenterPoint = nodeCenterPoint;
        if(radius == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM StationarySensorCircular radius is missing");
        } else if(CPM.isStrictMode() && (radius > 10000 || radius < 0)) {
            throw new IllegalArgumentException("CPM StationarySensorRadial radius should be in the range of [0 - 10000]."
                    + " Value: " + radius);
        }
        this.radius = radius;

        createJson();
    }

    private void createJson() {
        try {
            if(nodeCenterPoint != null)
                json.put(JsonCpmKey.StationarySensorCircular.NODE_CENTER_POINT.key(), nodeCenterPoint.getJson());
            json.put(JsonCpmKey.StationarySensorCircular.RADIUS.key(), radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public Offset getNodeCenterPoint() {
        return nodeCenterPoint;
    }

    public int getRadius() {
        return radius;
    }

    public static StationarySensorCircular jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            JSONObject jsonNodeCenterPoint = json.optJSONObject(JsonCpmKey.StationarySensorCircular.NODE_CENTER_POINT.key());
            Offset nodeCenterPoint = Offset.jsonParser(jsonNodeCenterPoint);
            int radius = json.getInt(JsonCpmKey.StationarySensorCircular.RADIUS.key());

            return new StationarySensorCircular(nodeCenterPoint, radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
