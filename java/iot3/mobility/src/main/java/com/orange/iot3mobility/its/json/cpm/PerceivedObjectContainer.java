package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PerceivedObjectContainer {

    private final JSONArray jsonArray = new JSONArray();

    /**
     * List of perceived objects.
     */
    private final ArrayList<PerceivedObject> perceivedObjects;

    public PerceivedObjectContainer(
            final ArrayList<PerceivedObject> perceivedObjects
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

    public ArrayList<PerceivedObject> getPerceivedObjects() {
        return perceivedObjects;
    }

    public static PerceivedObjectContainer jsonParser(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.length() == 0) return null;
        try {
            ArrayList<PerceivedObject> perceivedObjects = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                PerceivedObject perceivedObject = PerceivedObject.jsonParser(jsonArray.getJSONObject(i));
                perceivedObjects.add(perceivedObject);
            }

            return new PerceivedObjectContainer(perceivedObjects);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
