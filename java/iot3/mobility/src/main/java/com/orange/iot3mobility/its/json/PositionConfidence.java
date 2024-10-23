/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Confidence of the position.
 * <p>
 * The positionConfidenceEllipse provides the accuracy of the measured position
 * with the 95 % confidence level. Otherwise, the positionConfidenceEllipse shall be
 * set to unavailable.
 * <p>
 * If semiMajorOrientation is set to 0° North, then the semiMajorConfidence
 * corresponds to the position accuracy in the North/South direction, while the
 * semiMinorConfidence corresponds to the position accuracy in the East/West
 * direction. This definition implies that the semiMajorConfidence might be smaller
 * than the semiMinorConfidence.
 */
public class PositionConfidence {

    private static final Logger LOGGER = Logger.getLogger(PositionConfidence.class.getName());

    private final JSONObject jsonPositionConfidence = new JSONObject();
    private final PositionConfidenceEllipse positionConfidenceEllipse;
    private final int altitudeConfidence;

    public PositionConfidence(
            PositionConfidenceEllipse positionConfidenceEllipse,
            int altitudeConfidence)
    {
        this.positionConfidenceEllipse = positionConfidenceEllipse;
        if(altitudeConfidence != UNKNOWN && (altitudeConfidence > 15 || altitudeConfidence < 0)) {
            throw new IllegalArgumentException("Position Confidence Altitude should be in the range of [0 - 15]."
                    + " Value: " + altitudeConfidence);
        }
        this.altitudeConfidence = altitudeConfidence;

        createJson();
    }

    private void createJson() {
        try {
            if(positionConfidenceEllipse != null)
                jsonPositionConfidence.put(JsonKey.Confidence.POSITION_CONFIDENCE_ELLIPSE.key(), positionConfidenceEllipse);
            if(altitudeConfidence != UNKNOWN)
                jsonPositionConfidence.put(JsonKey.Confidence.ALTITUDE.key(), altitudeConfidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "PositionConfidence JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return jsonPositionConfidence;
    }

    public PositionConfidenceEllipse getPositionConfidenceEllipse() {
        return positionConfidenceEllipse;
    }

    public int getAltitudeConfidence() {
        return altitudeConfidence;
    }

    public static PositionConfidence jsonParser(JSONObject jsonPositionConfidence) {
        if(jsonPositionConfidence == null || jsonPositionConfidence.isEmpty()) return null;
        JSONObject jsonPositionConfidenceEllipse = jsonPositionConfidence.optJSONObject(JsonKey.Confidence.POSITION_CONFIDENCE_ELLIPSE.key());
        PositionConfidenceEllipse positionConfidenceEllipse = PositionConfidenceEllipse.jsonParser(jsonPositionConfidenceEllipse);
        int altitudeConfidence = jsonPositionConfidence.optInt(JsonKey.Confidence.ALTITUDE.key(), UNKNOWN);

        return new PositionConfidence(
                positionConfidenceEllipse,
                altitudeConfidence);
    }

}
