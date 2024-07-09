/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectVruAnimal {

    private final JSONObject json = new JSONObject();

    /**
     * Describes the subclass of a detected object for single VRU class animal.
     *
     * unavailable(0), wild-animal(1), farm-animal(2), service-animal(3), max(15).
     */
    private final int subclass;

    public ObjectVruAnimal(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 15 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Animal Object subclass should be in the range of [0 - 15]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.ANIMAL.key(), subclass);
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

    public static ObjectVruAnimal jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.ANIMAL.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectVruAnimal(subclass);
        else return null;
    }

}
