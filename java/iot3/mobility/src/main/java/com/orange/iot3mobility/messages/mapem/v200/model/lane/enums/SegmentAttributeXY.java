/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute flags for lane segments between node points
 * (DSRC schema – {@code segment_attribute_xy}).
 * <p>
 * Used in {@code NodeAttributes.enabled} and {@code NodeAttributes.disabled}.
 */
public enum SegmentAttributeXY {

    /** Reserved. */
    RESERVED("reserved"),

    /** Vehicles must not block the box at this segment. */
    DO_NOT_BLOCK("doNotBlock"),

    /** A white line is present at this segment. */
    WHITE_LINE("whiteLine"),

    /** A merging lane is present on the left. */
    MERGING_LANE_LEFT("mergingLaneLeft"),

    /** A merging lane is present on the right. */
    MERGING_LANE_RIGHT("mergingLaneRight"),

    /** A curb is present on the left. */
    CURB_ON_LEFT("curbOnLeft"),

    /** A curb is present on the right. */
    CURB_ON_RIGHT("curbOnRight"),

    /** A loading zone is on the left side. */
    LOADING_ZONE_ON_LEFT("loadingZoneOnLeft"),

    /** A loading zone is on the right side. */
    LOADING_ZONE_ON_RIGHT("loadingZoneOnRight"),

    /** A turn-out point is on the left side. */
    TURN_OUT_POINT_ON_LEFT("turnOutPointOnLeft"),

    /** A turn-out point is on the right side. */
    TURN_OUT_POINT_ON_RIGHT("turnOutPointOnRight"),

    /** Adjacent parking is on the left side. */
    ADJACENT_PARKING_ON_LEFT("adjacentParkingOnLeft"),

    /** Adjacent parking is on the right side. */
    ADJACENT_PARKING_ON_RIGHT("adjacentParkingOnRight"),

    /** An adjacent bicycle lane is on the left side. */
    ADJACENT_BIKE_LANE_ON_LEFT("adjacentBikeLaneOnLeft"),

    /** An adjacent bicycle lane is on the right side. */
    ADJACENT_BIKE_LANE_ON_RIGHT("adjacentBikeLaneOnRight"),

    /** This lane is shared with a bicycle lane. */
    SHARED_BIKE_LANE("sharedBikeLane"),

    /** A bike box is present in front of this segment. */
    BIKE_BOX_IN_FRONT("bikeBoxInFront"),

    /** A transit stop is on the left side. */
    TRANSIT_STOP_ON_LEFT("transitStopOnLeft"),

    /** A transit stop is on the right side. */
    TRANSIT_STOP_ON_RIGHT("transitStopOnRight"),

    /** A transit stop is in the lane. */
    TRANSIT_STOP_IN_LANE("transitStopInLane"),

    /** This lane is shared with a tracked vehicle lane. */
    SHARED_WITH_TRACKED_VEHICLE("sharedWithTrackedVehicle"),

    /** A safe island is present within this segment. */
    SAFE_ISLAND("safeIsland"),

    /** Low curbs are present. */
    LOW_CURBS_PRESENT("lowCurbsPresent"),

    /** A rumble strip is present. */
    RUMBLE_STRIP_PRESENT("rumbleStripPresent"),

    /** Audible signaling is present. */
    AUDIBLE_SIGNALING_PRESENT("audibleSignalingPresent"),

    /** Adaptive timing is present. */
    ADAPTIVE_TIMING_PRESENT("adaptiveTimingPresent"),

    /** RF signal request equipment is present. */
    RF_SIGNAL_REQUEST_PRESENT("rfSignalRequestPresent"),

    /** A partial curb intrusion is present. */
    PARTIAL_CURB_INTRUSION("partialCurbIntrusion"),

    /** The lane tapers to the left. */
    TAPER_TO_LEFT("taperToLeft"),

    /** The lane tapers to the right. */
    TAPER_TO_RIGHT("taperToRight"),

    /** The lane tapers to the centre line. */
    TAPER_TO_CENTER_LINE("taperToCenterLine"),

    /** Parallel parking is present. */
    PARALLEL_PARKING("parallelParking"),

    /** Head-in parking is present. */
    HEAD_IN_PARKING("headInParking"),

    /** Free parking is available. */
    FREE_PARKING("freeParking"),

    /** Time restrictions apply to parking. */
    TIME_RESTRICTIONS_ON_PARKING("timeRestrictionsOnParking"),

    /** Parking has a cost. */
    COST_TO_PARK("costToPark"),

    /** A mid-block curb is present. */
    MID_BLOCK_CURB_PRESENT("midBlockCurbPresent"),

    /** Uneven pavement is present. */
    UNEVEN_PAVEMENT_PRESENT("unEvenPavementPresent");

    private final String jsonValue;

    private static final Map<String, SegmentAttributeXY> BY_VALUE = new HashMap<>();

    static {
        for (SegmentAttributeXY segmentAttributeXY : values()) {
            BY_VALUE.put(segmentAttributeXY.jsonValue, segmentAttributeXY);
        }
    }

    SegmentAttributeXY(String jsonValue) {
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
     * Resolve a JSON string to the corresponding {@link SegmentAttributeXY}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static SegmentAttributeXY fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

