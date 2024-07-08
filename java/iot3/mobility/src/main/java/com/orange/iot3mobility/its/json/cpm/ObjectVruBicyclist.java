package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectVruBicyclist {

    private final JSONObject json = new JSONObject();

    /**
     * Describes the subclass of a detected object for single VRU class bicyclist.
     *
     * unavailable(0), bicyclist(1), wheelchair-user(2), horse-and-rider(3), rollerskater(4),
     * e-scooter(5), personal-transporter(6), pedelec(7), speed-pedelec(8), max(15).
     */
    private final int subclass;

    public ObjectVruBicyclist(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 15 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Bicyclist Object subclass should be in the range of [0 - 15]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.BICYCLIST.key(), subclass);
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

    public static ObjectVruBicyclist jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.BICYCLIST.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectVruBicyclist(subclass);
        else return null;
    }

}
