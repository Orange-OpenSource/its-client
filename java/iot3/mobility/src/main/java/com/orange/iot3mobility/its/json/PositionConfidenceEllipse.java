/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class PositionConfidenceEllipse {

    private final JSONObject jsonPositionConfidenceEllipse = new JSONObject();
    private final int semiMajorConfidence;
    private final int semiMinorConfidence;
    private final int semiMajorOrientation;

    public PositionConfidenceEllipse(
            final int semiMajorConfidence,
            final int semiMinorConfidence,
            final int semiMajorOrientation)
    {
        if(semiMajorConfidence != UNKNOWN && (semiMajorConfidence > 4095 || semiMajorConfidence < 0)) {
            throw new IllegalArgumentException("Position SemiMajorConfidence should be in the range of [0 - 4095]."
                    + " Value: " + semiMajorConfidence);
        }
        this.semiMajorConfidence = semiMajorConfidence;
        if(semiMinorConfidence != UNKNOWN && (semiMinorConfidence > 4095 || semiMinorConfidence < 0)) {
            throw new IllegalArgumentException("Position SemiMinorConfidence should be in the range of [0 - 4095]."
                    + " Value: " + semiMinorConfidence);
        }
        this.semiMinorConfidence = semiMinorConfidence;
        if(semiMajorOrientation != UNKNOWN && (semiMajorOrientation > 3601 || semiMajorOrientation < 0)) {
            throw new IllegalArgumentException("Position SemiMajorOrientation should be in the range of [0 - 3601]."
                    + " Value: " + semiMajorOrientation);
        }
        this.semiMajorOrientation = semiMajorOrientation;

        createJson();
    }

    private void createJson() {
        try {
            if (semiMajorConfidence != UNKNOWN)
                jsonPositionConfidenceEllipse.put(JsonKey.Confidence.POSITION_SEMI_MAJOR_CONFIDENCE.key(), semiMajorConfidence);
            if (semiMinorConfidence != UNKNOWN)
                jsonPositionConfidenceEllipse.put(JsonKey.Confidence.POSITION_SEMI_MINOR_CONFIDENCE.key(), semiMinorConfidence);
            if (semiMajorOrientation != UNKNOWN)
                jsonPositionConfidenceEllipse.put(JsonKey.Confidence.POSITION_SEMI_MAJOR_ORIENTATION.key(), semiMajorOrientation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonPositionConfidenceEllipse() {
        return jsonPositionConfidenceEllipse;
    }

    public int getSemiMajorConfidence() {
        return semiMajorConfidence;
    }

    public int getSemiMinorConfidence() {
        return semiMinorConfidence;
    }

    public int getSemiMajorOrientation() {
        return semiMajorOrientation;
    }

    public static PositionConfidenceEllipse jsonParser(JSONObject jsonPositionConfidenceEllipse) {
        if(jsonPositionConfidenceEllipse == null || jsonPositionConfidenceEllipse.length() == 0) return null;
        int semiMajorConfidence = jsonPositionConfidenceEllipse.optInt(JsonKey.Confidence.POSITION_SEMI_MAJOR_CONFIDENCE.key(), UNKNOWN);;
        int semiMinorConfidence = jsonPositionConfidenceEllipse.optInt(JsonKey.Confidence.POSITION_SEMI_MINOR_CONFIDENCE.key(), UNKNOWN);;
        int semiMajorOrientation = jsonPositionConfidenceEllipse.optInt(JsonKey.Confidence.POSITION_SEMI_MAJOR_ORIENTATION.key(), UNKNOWN);

        return new PositionConfidenceEllipse(
                semiMajorConfidence,
                semiMinorConfidence,
                semiMajorOrientation);
    }

}
