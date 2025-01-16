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

public class ObjectClassOther {

    private static final Logger LOGGER = Logger.getLogger(ObjectClassOther.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Detected object for class other. unknown(0), roadSideUnit(1).
     */
    private final int subclass;

    public ObjectClassOther(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 255 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Other Object subclass should be in the range of [0 - 255]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.OTHER.key(), subclass);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ObjectClassOther JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSubclass() {
        return subclass;
    }

    public static ObjectClassOther jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.OTHER.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectClassOther(subclass);
        else return null;
    }

}
