/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerceivedObjectContainer {

    private static final Logger LOGGER = Logger.getLogger(PerceivedObjectContainer.class.getName());

    private final JSONArray jsonArray = new JSONArray();

    /**
     * List of perceived objects.
     */
    private final List<PerceivedObject> perceivedObjects;

    public PerceivedObjectContainer(
            final List<PerceivedObject> perceivedObjects
    ) {
        this.perceivedObjects = perceivedObjects;

        createJson();
    }

    private void createJson() {
        for(PerceivedObject perceivedObject: perceivedObjects) {
            jsonArray.put(perceivedObject.getJson());
        }
    }

    public JSONArray getJson() {
        return jsonArray;
    }

    public List<PerceivedObject> getPerceivedObjects() {
        return perceivedObjects;
    }

    public static PerceivedObjectContainer jsonParser(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.isEmpty()) return null;
        try {
            ArrayList<PerceivedObject> perceivedObjects = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                PerceivedObject perceivedObject = PerceivedObject.jsonParser(jsonArray.getJSONObject(i));
                perceivedObjects.add(perceivedObject);
            }

            return new PerceivedObjectContainer(perceivedObjects);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM PerceivedObjectContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
