/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PathHistory {

    private final JSONArray jsonPathHistory = new JSONArray();
    private final ArrayList<PathPoint> pathPoints;

    public PathHistory(
            ArrayList<PathPoint> pathPoints)
    {
        if(pathPoints == null) this.pathPoints = new ArrayList<>();
        else this.pathPoints = pathPoints;

        createJson();
    }

    private void createJson() {
        for(PathPoint pathPoint: pathPoints) {
            jsonPathHistory.put(pathPoint.getJsonPathPoint());
        }
    }

    public JSONArray getJsonPathHistory() {
        return jsonPathHistory;
    }

    public ArrayList<PathPoint> getPathPoints() {
        return pathPoints;
    }

    public static PathHistory jsonParser(JSONArray jsonPathHistory) {
        if(jsonPathHistory == null || jsonPathHistory.length() == 0) return new PathHistory(null);
        ArrayList<PathPoint> pathPoints = new ArrayList<>();
        try {
            for(int i = 0; i < jsonPathHistory.length(); i++) {
                PathPoint pathPoint = PathPoint.jsonParser(jsonPathHistory.getJSONObject(i));
                pathPoints.add(pathPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new PathHistory(pathPoints);
    }

}
