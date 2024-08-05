/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class Offset {

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.01 m.
     *
     * Offset on the x-axis.
     */
    private final int x;

    /**
     * Unit: 0.01 m.
     *
     * Offset on the y-axis.
     */
    private final int y;

    /**
     * Unit: 0.01 m.
     *
     * Offset on the z-axis.
     */
    private final int z;

    public Offset(
            final int x,
            final int y
    ) throws IllegalArgumentException {
        this(x, y, UNKNOWN);
    }

    public Offset(
            final int x,
            final int y,
            final int z
    ) throws IllegalArgumentException {
        if(x == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM Offset X is missing");
        } else if(CPM.isStrictMode() && (x > 32767 || x < -32768)) {
            throw new IllegalArgumentException("CPM Offset X should be in the range of [-32768 - 32767]."
                    + " Value: " + x);
        }
        this.x = x;
        if(y == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM Offset Y is missing");
        } else if(CPM.isStrictMode() && (y > 32767 || y < -32768)) {
            throw new IllegalArgumentException("CPM Offset Y should be in the range of [-32768 - 32767]."
                    + " Value: " + y);
        }
        this.y = y;
        if(z != UNKNOWN && CPM.isStrictMode()
                && (z > 32767 || z < -32768)) {
            throw new IllegalArgumentException("CPM Offset Z should be in the range of [-32768 - 32767]."
                    + " Value: " + z);
        }
        this.z = z;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.Offset.X.key(), x);
            json.put(JsonCpmKey.Offset.Y.key(), y);
            if(z != UNKNOWN)
                json.put(JsonCpmKey.Offset.Z.key(), z);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public static Offset jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            int x = json.getInt(JsonCpmKey.Offset.X.key());
            int y = json.getInt(JsonCpmKey.Offset.Y.key());
            int z = json.optInt(JsonCpmKey.Offset.Z.key(), UNKNOWN);

            return new Offset(x, y, z);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
