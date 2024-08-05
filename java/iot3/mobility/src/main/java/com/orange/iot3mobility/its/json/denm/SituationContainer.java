/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SituationContainer {

    private final JSONObject jsonSituationContainer = new JSONObject();
    private final int infoQuality;
    private final EventType eventType;
    private final LinkedCause linkedCause;
    private final JSONArray eventHistory;

    public SituationContainer(
            final EventType eventType)
    {
        this(UNKNOWN, eventType, null, null);
    }

    public SituationContainer(
            final int infoQuality,
            final EventType eventType)
    {
        this(infoQuality, eventType, null, null);
    }

    public SituationContainer(
            final int infoQuality,
            final EventType eventType,
            final LinkedCause linkedCause,
            final JSONArray eventHistory)
    {
        if(infoQuality != UNKNOWN && (infoQuality > 7 || infoQuality < 0)) {
            throw new IllegalArgumentException("DENM SituationContainer InfoQuality should be in the range of [0 - 7]."
                    + " Value: " + infoQuality);
        }
        this.infoQuality = infoQuality;
        if(eventType == null) {
            throw new IllegalArgumentException("DENM SituationContainer EventType missing.");
        }
        this.eventType = eventType;
        this.linkedCause = linkedCause;
        this.eventHistory = eventHistory;

        createJson();
    }

    private void createJson() {
        try {
            if(infoQuality != UNKNOWN)
                jsonSituationContainer.put(JsonKey.SituationContainer.INFO_QUALITY.key(), infoQuality);
            jsonSituationContainer.put(JsonKey.SituationContainer.EVENT_TYPE.key(), eventType.getJsonEventType());
            if(linkedCause != null)
                jsonSituationContainer.put(JsonKey.SituationContainer.LINKED_CAUSE.key(), linkedCause.getJsonLinkedCause());
            if(eventHistory != null)
                jsonSituationContainer.put(JsonKey.SituationContainer.EVENT_TYPE.key(), eventHistory);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonSituationContainer() {
        return jsonSituationContainer;
    }

    public int getInfoQuality() {
        return infoQuality;
    }

    public boolean hasInfoQuality() {
        return infoQuality != UNKNOWN;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LinkedCause getLinkedCause() {
        return linkedCause;
    }

    public JSONArray getEventHistory() {
        return eventHistory;
    }

    public static SituationContainer jsonParser(JSONObject jsonSituationContainer) {
        if(jsonSituationContainer == null || jsonSituationContainer.length() == 0) return null;
        try {
            int infoQuality = jsonSituationContainer.optInt(JsonKey.SituationContainer.INFO_QUALITY.key(), UNKNOWN);
            JSONObject jsonEventType = jsonSituationContainer.getJSONObject(JsonKey.SituationContainer.EVENT_TYPE.key());
            EventType eventType = EventType.jsonParser(jsonEventType);
            JSONObject jsonLinkedCause = jsonSituationContainer.optJSONObject(JsonKey.SituationContainer.LINKED_CAUSE.key());
            LinkedCause linkedCause = LinkedCause.jsonParser(jsonLinkedCause);
            JSONArray eventHistory = jsonSituationContainer.optJSONArray(JsonKey.SituationContainer.EVENT_HISTORY.key());

            return new SituationContainer(
                    infoQuality,
                    eventType,
                    linkedCause,
                    eventHistory);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
