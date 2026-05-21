/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags local to a single node point in a lane path
 * (DSRC schema – {@code node_attribute_xy}).
 * <p>
 * Used in {@code NodeAttributes.localNode}.
 */
public enum NodeAttributeXY {

    /** Reserved. */
    RESERVED("reserved"),

    /** A stop line is located at this node. */
    STOP_LINE("stopLine"),

    /** The lane end uses rounded cap style A. */
    ROUNDED_CAP_STYLE_A("roundedCapStyleA"),

    /** The lane end uses rounded cap style B. */
    ROUNDED_CAP_STYLE_B("roundedCapStyleB"),

    /** This node is a merge point where lanes converge. */
    MERGE_POINT("mergePoint"),

    /** This node is a diverge point where lanes split. */
    DIVERGE_POINT("divergePoint"),

    /** A downstream stop line is located at this node. */
    DOWNSTREAM_STOP_LINE("downstreamStopLine"),

    /** This node marks the start of a downstream segment. */
    DOWNSTREAM_START_NODE("downstreamStartNode"),

    /** The lane is closed to traffic at this node. */
    CLOSED_TO_TRAFFIC("closedToTraffic"),

    /** A safe island is present at this node. */
    SAFE_ISLAND("safeIsland"),

    /** A curb is present at the step-off point at this node. */
    CURB_PRESENT_AT_STEP_OFF("curbPresentAtStepOff"),

    /** A fire hydrant is present at this node. */
    HYDRANT_PRESENT("hydrantPresent");

    private final String jsonValue;

    private static final Map<String, NodeAttributeXY> BY_VALUE = new HashMap<>();

    static {
        for (NodeAttributeXY nodeAttributeXY : values()) {
            BY_VALUE.put(nodeAttributeXY.jsonValue, nodeAttributeXY);
        }
    }

    NodeAttributeXY(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the DSRC schema.
     *
     * @return the exact string used in MAPEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link NodeAttributeXY}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static NodeAttributeXY fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

