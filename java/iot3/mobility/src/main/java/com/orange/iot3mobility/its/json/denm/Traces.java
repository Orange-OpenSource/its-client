/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import com.orange.iot3mobility.its.json.PathHistory;

import org.json.JSONArray;

import java.util.ArrayList;

public class Traces {

    private final JSONArray jsonTraces = new JSONArray();
    private final ArrayList<PathHistory> pathHistories;

    public Traces(
            ArrayList<PathHistory> pathHistories)
    {
        if(pathHistories == null) this.pathHistories = new ArrayList<>();
        else this.pathHistories = pathHistories;

        createJson();
    }

    private void createJson() {
        for(PathHistory pathHistory: pathHistories) {
            jsonTraces.put(pathHistory.getJsonPathHistory());
        }
    }

    public JSONArray getJsonTraces() {
        return jsonTraces;
    }

    public ArrayList<PathHistory> getPathHistories() {
        return pathHistories;
    }

    public static Traces jsonParser(JSONArray jsonTraces) {
        if(jsonTraces == null || jsonTraces.length() == 0) return new Traces(null);
        ArrayList<PathHistory> pathHistories = new ArrayList<>();
        for(int i = 0; i < jsonTraces.length(); i++) {
            PathHistory pathHistory = PathHistory.jsonParser(jsonTraces.optJSONArray(i));
            pathHistories.add(pathHistory);
        }
        return new Traces(pathHistories);
    }

}
