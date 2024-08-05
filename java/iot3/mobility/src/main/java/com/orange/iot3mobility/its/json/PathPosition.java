/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import com.orange.iot3mobility.its.EtsiUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PathPosition {

    private final JSONObject jsonPathPosition = new JSONObject();
    private final int deltaLatitude;
    private final int deltaLongitude;
    private final int deltaAltitude;

    public PathPosition(
            final int deltaLatitude,
            final int deltaLongitude,
            final int deltaAltitude)
    {
        if(deltaLatitude > 131072 || deltaLatitude < -131071) {
            throw new IllegalArgumentException("PathPosition DeltaLatitude should be in the range of [-131071 - 131072]."
                    + " Value: " + deltaLatitude);
        }
        this.deltaLatitude = deltaLatitude;
        if(deltaLongitude > 131072 || deltaLongitude < -131071) {
            throw new IllegalArgumentException("PathPosition DeltaLongitude should be in the range of [-131071 - 131072]."
                    + " Value: " + deltaLongitude);
        }
        this.deltaLongitude = deltaLongitude;
        if(deltaAltitude > 12800 || deltaAltitude < -12700) {
            throw new IllegalArgumentException("PathPosition DeltaAltitude should be in the range of [-12700 - 12800]."
                    + " Value: " + deltaAltitude);
        }
        this.deltaAltitude = deltaAltitude;

        createJson();
    }

    private void createJson() {
        try {
            jsonPathPosition.put(JsonKey.PathPosition.DELTA_LATITUDE.key(), deltaLatitude);
            jsonPathPosition.put(JsonKey.PathPosition.DELTA_LONGITUDE.key(), deltaLongitude);
            jsonPathPosition.put(JsonKey.PathPosition.DELTA_ALTITUDE.key(), deltaAltitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonPathPosition() {
        return jsonPathPosition;
    }

    public int getDeltaLatitude() {
        return deltaLatitude;
    }

    public double getDeltaLatitudeDegree() {
        return (double) deltaLatitude / EtsiUtils.ETSI_COORDINATES_FACTOR;
    }

    public int getDeltaLongitude() {
        return deltaLongitude;
    }

    public double getDeltaLongitudeDegree() {
        return (double) deltaLongitude /EtsiUtils.ETSI_COORDINATES_FACTOR;
    }

    public int getDeltaAltitude() {
        return deltaAltitude;
    }

    public static PathPosition jsonParser(JSONObject jsonPathPosition) {
        if(jsonPathPosition == null || jsonPathPosition.length() == 0) return null;
        try {
            int deltaLatitude = jsonPathPosition.getInt(JsonKey.PathPosition.DELTA_LATITUDE.key());
            int deltaLongitude = jsonPathPosition.getInt(JsonKey.PathPosition.DELTA_LONGITUDE.key());
            int deltaAltitude = jsonPathPosition.getInt(JsonKey.PathPosition.DELTA_ALTITUDE.key());

            return new PathPosition(
                    deltaLatitude,
                    deltaLongitude,
                    deltaAltitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
