/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectVruPedestrian {

    private final JSONObject json = new JSONObject();

    /**
     * Describes the subclass of a detected object for single VRU class pedestrian.
     *
     * unavailable(0), ordinary-pedestrian(1), road-worker(2), first-responder(3), max(15).
     */
    private final int subclass;

    public ObjectVruPedestrian(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 15 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Pedestrian Object subclass should be in the range of [0 - 15]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.PEDESTRIAN.key(), subclass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSubclass() {
        return subclass;
    }

    public static ObjectVruPedestrian jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.PEDESTRIAN.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectVruPedestrian(subclass);
        else return null;
    }

}
