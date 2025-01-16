/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathHistory {

    private static final Logger LOGGER = Logger.getLogger(PathHistory.class.getName());

    private final JSONArray jsonPathHistory = new JSONArray();
    private final List<PathPoint> pathPoints;

    public PathHistory(List<PathPoint> pathPoints)
    {
        if(pathPoints == null) this.pathPoints = new ArrayList<>();
        else this.pathPoints = new ArrayList<>(pathPoints);

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

    public List<PathPoint> getPathPoints() {
        return pathPoints;
    }

    public static PathHistory jsonParser(JSONArray jsonPathHistory) {
        if(JsonUtil.isNullOrEmpty(jsonPathHistory)) return new PathHistory(null);
        ArrayList<PathPoint> pathPoints = new ArrayList<>();
        try {
            for(int i = 0; i < jsonPathHistory.length(); i++) {
                PathPoint pathPoint = PathPoint.jsonParser(jsonPathHistory.getJSONObject(i));
                pathPoints.add(pathPoint);
            }
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "PathHistory JSON parsing error", "Error: " + e);
        }
        return new PathHistory(pathPoints);
    }

}
