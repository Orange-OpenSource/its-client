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

public class ObjectVruPedestrian {

    private static final Logger LOGGER = Logger.getLogger(ObjectVruPedestrian.class.getName());

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
            LOGGER.log(Level.WARNING, "CPM ObjectVruPedestrian JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSubclass() {
        return subclass;
    }

    public static ObjectVruPedestrian jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.PEDESTRIAN.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectVruPedestrian(subclass);
        else return null;
    }

}
