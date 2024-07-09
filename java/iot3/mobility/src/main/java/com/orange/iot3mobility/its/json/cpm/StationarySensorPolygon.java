/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONArray;

import java.util.ArrayList;

public class StationarySensorPolygon {

    private final JSONArray jsonArray = new JSONArray();

    /**
     * List of offset points forming the polygon, with respect to the reference position.
     */
    private final ArrayList<Offset> offsetPoints;

    public StationarySensorPolygon(
            final ArrayList<Offset> offsetPoints
    ) {
        this.offsetPoints = offsetPoints;

        createJson();
    }

    private void createJson() {
        for(Offset offsetPoint: offsetPoints) {
            jsonArray.put(offsetPoint.getJson());
        }
    }

    public JSONArray getJson() {
        return jsonArray;
    }

    public ArrayList<Offset> getOffsetPoints() {
        return offsetPoints;
    }

    public static StationarySensorPolygon jsonParser(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.length() == 0) return null;
        ArrayList<Offset> offsetPoints = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Offset offsetPoint = Offset.jsonParser(jsonArray.optJSONObject(i));
            offsetPoints.add(offsetPoint);
        }

        return new StationarySensorPolygon(offsetPoints);
    }

}
