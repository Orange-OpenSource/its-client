/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import com.orange.iot3mobility.its.json.JsonKey;

import org.json.JSONException;
import org.json.JSONObject;

public class LinkedCause {

    private final JSONObject jsonLinkedCause = new JSONObject();
    private final int cause;
    private final int subcause;

    public LinkedCause(
            final int cause,
            final int subcause)
    {
        if(cause > 255 || cause < 0) {
            throw new IllegalArgumentException("DENM LinkedCause Cause should be in the range of [0 - 255]."
                    + " Value: " + cause);
        }
        this.cause = cause;
        if(subcause > 255 || subcause < 0) {
            throw new IllegalArgumentException("DENM LinkedCause SubCause should be in the range of [0 - 255]."
                    + " Value: " + subcause);
        }
        this.subcause = subcause;

        createJson();
    }

    private void createJson() {
        try {
            jsonLinkedCause.put(JsonKey.LinkedCause.CAUSE.key(), cause);
            jsonLinkedCause.put(JsonKey.LinkedCause.SUBCAUSE.key(), subcause);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonLinkedCause() {
        return jsonLinkedCause;
    }

    public int getCause() {
        return cause;
    }

    public int getSubcause() {
        return subcause;
    }

    public static LinkedCause jsonParser(JSONObject jsonLinkedCause) {
        if(jsonLinkedCause == null || jsonLinkedCause.length() == 0) return null;
        try {
            int cause = jsonLinkedCause.getInt(JsonKey.EventType.CAUSE.key());
            int subcause = jsonLinkedCause.getInt(JsonKey.EventType.SUBCAUSE.key());

            return new LinkedCause(cause, subcause);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
