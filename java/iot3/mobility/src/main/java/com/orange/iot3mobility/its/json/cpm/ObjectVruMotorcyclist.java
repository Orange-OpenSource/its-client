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

public class ObjectVruMotorcyclist {

    private static final Logger LOGGER = Logger.getLogger(ObjectVruMotorcyclist.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Describes the subclass of a detected object for single VRU class motorcyclist.
     *
     * unavailable(0), moped(1), motorcycle(2), motorcycle-and-sidecar-right(3),
     * motorcycle-and-sidecar-left(4), max(15).
     */
    private final int subclass;

    public ObjectVruMotorcyclist(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 15 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Motorcyclist Object subclass should be in the range of [0 - 15]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.MOTORCYLCIST.key(), subclass);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ObjectVruMotorcyclist JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSubclass() {
        return subclass;
    }

    public static ObjectVruMotorcyclist jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.MOTORCYLCIST.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectVruMotorcyclist(subclass);
        else return null;
    }

}
