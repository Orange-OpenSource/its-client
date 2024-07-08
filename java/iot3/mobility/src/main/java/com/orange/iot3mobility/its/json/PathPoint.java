package com.orange.iot3mobility.its.json;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class PathPoint {

    private final JSONObject jsonPathPoint = new JSONObject();
    private final PathPosition pathPosition;
    private final int pathDeltaTime;

    public PathPoint(
            PathPosition pathPosition)
    {
        this(pathPosition, UNKNOWN);
    }

    public PathPoint(
            PathPosition pathPosition,
            int pathDeltaTime)
    {
        this.pathPosition = pathPosition;
        if(pathDeltaTime != UNKNOWN && (pathDeltaTime > 65535 || pathDeltaTime < 1)) {
            throw new IllegalArgumentException("PathPosition PathDeltaTime should be in the range of [1 - 65535]."
                    + " Value: " + pathDeltaTime);
        }
        this.pathDeltaTime = pathDeltaTime;

        createJson();
    }

    private void createJson() {
        try {
            jsonPathPoint.put(JsonKey.PathPoint.PATH_POSITION.key(), pathPosition);
            if(pathDeltaTime != UNKNOWN)
                jsonPathPoint.put(JsonKey.PathPoint.PATH_DELTA_TIME.key(), pathDeltaTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonPathPoint() {
        return jsonPathPoint;
    }

    public PathPosition getPathPosition() {
        return pathPosition;
    }

    public int getPathDeltaTime() {
        return pathDeltaTime;
    }

    public int getPathDeltaTimeMs() {
        return pathDeltaTime*10;
    }

    public static PathPoint jsonParser(JSONObject jsonPathPoint) {
        if(jsonPathPoint == null || jsonPathPoint.length() == 0) return null;
        JSONObject jsonPathPosition = jsonPathPoint.optJSONObject(JsonKey.PathPoint.PATH_POSITION.key());
        PathPosition pathPosition = PathPosition.jsonParser(jsonPathPosition);
        int pathDeltaTime = jsonPathPoint.optInt(JsonKey.PathPoint.PATH_DELTA_TIME.key(), UNKNOWN);

        return new PathPoint(
                pathPosition,
                pathDeltaTime);
    }

}
