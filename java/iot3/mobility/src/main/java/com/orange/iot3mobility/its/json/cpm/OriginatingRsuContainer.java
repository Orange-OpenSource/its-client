/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OriginatingRsuContainer {

    private static final Logger LOGGER = Logger.getLogger(OriginatingRsuContainer.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Road regulator id. When is present the intersection or road segment reference id is
     * guaranteed to be globally unique.
     */
    private final int region;

    /**
     * Intersection id. Unique within that region.
     */
    private final int intersectionReferenceId;

    /**
     * Road segment id. Unique within that region.
     */
    private final int roadSegmentReferenceId;

    public OriginatingRsuContainer(
            final int region,
            final int intersectionReferenceId,
            final int roadSegmentReferenceId
    ) throws IllegalArgumentException {
        if(region != UNKNOWN && CPM.isStrictMode()
                && (region > 65535 || region < 0)) {
            throw new IllegalArgumentException("CPM OriginatingRsuContainer region should be in the range of [0 - 65535]."
                    + " Value: " + region);
        }
        this.region = region;
        if(intersectionReferenceId != UNKNOWN && CPM.isStrictMode()
                && (intersectionReferenceId > 65535 || intersectionReferenceId < 0)) {
            throw new IllegalArgumentException("CPM OriginatingRsuContainer intersectionReferenceId should be in the range of [0 - 65535]."
                    + " Value: " + intersectionReferenceId);
        }
        this.intersectionReferenceId = intersectionReferenceId;
        if(roadSegmentReferenceId != UNKNOWN && CPM.isStrictMode()
                && (roadSegmentReferenceId > 65535 || roadSegmentReferenceId < 0)) {
            throw new IllegalArgumentException("CPM OriginatingRsuContainer roadSegmentReferenceId should be in the range of [0 - 65535]."
                    + " Value: " + roadSegmentReferenceId);
        }
        this.roadSegmentReferenceId = roadSegmentReferenceId;

        createJson();
    }

    private void createJson() {
        try {
            if(region != UNKNOWN)
                json.put(JsonCpmKey.OriginatingRsuContainer.REGION.key(), region);
            if(intersectionReferenceId != UNKNOWN)
                json.put(JsonCpmKey.OriginatingRsuContainer.INTERSECTION_REFERENCE_ID.key(), intersectionReferenceId);
            if(roadSegmentReferenceId != UNKNOWN)
                json.put(JsonCpmKey.OriginatingRsuContainer.ROAD_SEGMENT_REFERENCE_ID.key(), roadSegmentReferenceId);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM OriginatingRsuContainer JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getRegion() {
        return region;
    }

    public int getIntersectionReferenceId() {
        return intersectionReferenceId;
    }

    public int getRoadSegmentReferenceId() {
        return roadSegmentReferenceId;
    }

    public static OriginatingRsuContainer jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        int region = json.optInt(JsonCpmKey.OriginatingRsuContainer.REGION.key(), UNKNOWN);
        int intersectionReferenceId = json.optInt(JsonCpmKey.OriginatingRsuContainer.INTERSECTION_REFERENCE_ID.key(), UNKNOWN);
        int roadSegmentReferenceId = json.optInt(JsonCpmKey.OriginatingRsuContainer.ROAD_SEGMENT_REFERENCE_ID.key(), UNKNOWN);

        return new OriginatingRsuContainer(region, intersectionReferenceId, roadSegmentReferenceId);
    }

}
