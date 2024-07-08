package com.orange.iot3mobility.its.json;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.EtsiUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Position {

    private final JSONObject jsonPosition = new JSONObject();
    private final long latitude;
    private final long longitude;
    private final int altitude;

    public Position(
            final long latitude,
            final long longitude)
    {
        this(latitude, longitude, UNKNOWN);
    }

    public Position(
            final long latitude,
            final long longitude,
            final int altitude)
    {
        if(latitude > 900000001 || latitude < -900000000) {
            throw new IllegalArgumentException("Position Latitude should be in the range of [-900000000 - 900000001]."
                    + " Value: " + latitude);
        }
        this.latitude = latitude;
        if(longitude > 1800000001 || longitude < -1800000000) {
            throw new IllegalArgumentException("Position Longitude should be in the range of [-1800000000 - 1800000001]."
                    + " Value: " + longitude);
        }
        this.longitude = longitude;
        if(altitude != UNKNOWN && (altitude > 800001 || altitude < -100000)) {
            throw new IllegalArgumentException("Position Altitude should be in the range of [-100000 - 800001]."
                    + " Value: " + altitude);
        }
        this.altitude = altitude;

        createJson();
    }

    private void createJson() {
        try {
            jsonPosition.put(JsonKey.Position.LATITUDE.key(), latitude);
            jsonPosition.put(JsonKey.Position.LONGITUDE.key(), longitude);
            if(altitude != UNKNOWN)
                jsonPosition.put(JsonKey.Position.ALTITUDE.key(), altitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return jsonPosition;
    }

    public long getLatitude() {
        return latitude;
    }

    public double getLatitudeDegree() {
        return (double) latitude / EtsiUtils.ETSI_COORDINATES_FACTOR;
    }

    public long getLongitude() {
        return longitude;
    }

    public double getLongitudeDegree() {
        return (double) longitude / EtsiUtils.ETSI_COORDINATES_FACTOR;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getAltitudeMeters() {
        return altitude/EtsiUtils.ETSI_ALTITUDE_FACTOR;
    }

    public static Position jsonParser(JSONObject jsonPosition) {
        if(jsonPosition == null || jsonPosition.length() == 0) return null;
        try {
            long latitude = jsonPosition.getLong(JsonKey.Position.LATITUDE.key());
            long longitude = jsonPosition.getLong(JsonKey.Position.LONGITUDE.key());
            int altitude = jsonPosition.optInt(JsonKey.Position.ALTITUDE.key(), UNKNOWN);

            return new Position(
                    latitude,
                    longitude,
                    altitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
