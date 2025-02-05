/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionId {

    private static final Logger LOGGER = Logger.getLogger(ActionId.class.getName());

    private final JSONObject jsonActionId = new JSONObject();
    private final long originatingStationId;
    private final int sequenceNumber;

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
            LOGGER.log(Level.WARNING, "ActionId JSON build error", "Error: " + e);
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
        if(JsonUtil.isNullOrEmpty(jsonActionId)) return null;
        try {
            long originStationId = jsonActionId.getLong(JsonKey.ActionId.ORIGINATING_STATION_ID.key());
            int sequenceNumber = jsonActionId.optInt(JsonKey.ActionId.SEQUENCE_NUMBER.key(), UNKNOWN);

            return new ActionId(originStationId, sequenceNumber);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "ActionId JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
