package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;

import org.json.JSONException;
import org.json.JSONObject;

public class ActionId {

    private JSONObject jsonActionId = new JSONObject();
    private final long originatingStationId;
    private int sequenceNumber;

    public ActionId(
            final long originatingStationId)
    {
        this(originatingStationId, UNKNOWN);
    }

    public ActionId(
            final long originatingStationId,
            final int sequenceNumber)
    {
        if(originatingStationId > 4294967295L || originatingStationId < 0) {
            throw new IllegalArgumentException("DENM OriginStationID should be in the range of [0 - 4294967295]."
                    + " Value: " + originatingStationId);
        }
        this.originatingStationId = originatingStationId;
        if(sequenceNumber != UNKNOWN && (sequenceNumber > 65535 || sequenceNumber < 0)) {
            throw new IllegalArgumentException("DENM SequenceNumber should be in the range of [0 - 65535]."
                    + " Value: " + sequenceNumber);
        }
        this.sequenceNumber = sequenceNumber;

        createJson();
    }

    private void createJson() {
        try {
            jsonActionId.put(JsonKey.ActionId.ORIGINATING_STATION_ID.key(), originatingStationId);
            if(sequenceNumber != UNKNOWN)
                jsonActionId.put(JsonKey.ActionId.SEQUENCE_NUMBER.key(), sequenceNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonActionId() {
        return jsonActionId;
    }

    public long getOriginatingStationId() {
        return originatingStationId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public static ActionId jsonParser(JSONObject jsonActionId) {
        if(jsonActionId == null || jsonActionId.length() == 0) return null;
        try {
            long originStationId = jsonActionId.getLong(JsonKey.ActionId.ORIGINATING_STATION_ID.key());
            int sequenceNumber = jsonActionId.optInt(JsonKey.ActionId.SEQUENCE_NUMBER.key(), UNKNOWN);

            return new ActionId(originStationId, sequenceNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
