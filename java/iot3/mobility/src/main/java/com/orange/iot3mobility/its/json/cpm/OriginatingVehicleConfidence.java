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

public class OriginatingVehicleConfidence {

    private static final Logger LOGGER = Logger.getLogger(OriginatingVehicleConfidence.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.1 degree. Heading accuracy of the vehicle movement with regards to the true north.
     *
     * equalOrWithinZeroPointOneDegree (1), equalOrWithinOneDegree (10), outOfRange(126),
     * unavailable(127).
     */
    private final int heading;

    /**
     * Unit: 0.01 m/s. Speed accuracy.
     *
     * equalOrWithinOneCentimeterPerSec(1), equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127).
     */
    private final int speed;

    /**
     *  noTrailerPresent(0), trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2),
     *  trailerPresenceIsUnknown(3), unavailable(4).
     */
    private final int vehicleLength;

    /**
     * not specified... can be removed
     */
    private final int vehicleWidth;

    /**
     * degSec-000-01 (0), degSec-000-05 (1), degSec-000-10 (2), degSec-001-00 (3),
     * degSec-005-00 (4), degSec-010-00 (5), degSec-100-00 (6), outOfRange (7), unavailable (8).
     */
    private final int yawRate;

    /**
     * Unit: 0.1 m/s2. Longitudinal acceleration accuracy.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int longitudinalAcceleration;

    /**
     * Unit: 0.1 m/s2. Lateral acceleration accuracy.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int lateralAcceleration;

    /**
     * Unit: 0.1 m/s2. Vertical acceleration accuracy.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int verticalAcceleration;

    public OriginatingVehicleConfidence(
            final int heading,
            final int speed,
            final int vehicleLength,
            final int vehicleWidth,
            final int yawRate,
            final int longitudinalAcceleration,
            final int lateralAcceleration,
            final int verticalAcceleration
    ) throws IllegalArgumentException {
        if(heading != UNKNOWN && CPM.isStrictMode() && (heading > 127 || heading < 1)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence heading should be in the range of [1 - 127]."
                    + " Value: " + heading);
        }
        this.heading = heading;
        if(speed != UNKNOWN && CPM.isStrictMode() && (speed > 127 || speed < 1)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence speed should be in the range of [1 - 127]."
                    + " Value: " + speed);
        }
        this.speed = speed;
        if(vehicleLength != UNKNOWN && CPM.isStrictMode() && (vehicleLength > 4 || vehicleLength < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence vehicleLength should be in the range of [0 - 4]."
                    + " Value: " + vehicleLength);
        }
        this.vehicleLength = vehicleLength;
        if(vehicleWidth != UNKNOWN && CPM.isStrictMode() && (vehicleWidth > 4 || vehicleWidth < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence vehicleWidth should be in the range of [0 - 4]."
                    + " Value: " + vehicleWidth);
        }
        this.vehicleWidth = vehicleWidth;
        if(yawRate != UNKNOWN && CPM.isStrictMode() && (yawRate > 8 || yawRate < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence yawRate should be in the range of [0 - 8]."
                    + " Value: " + yawRate);
        }
        this.yawRate = yawRate;
        if(longitudinalAcceleration != UNKNOWN && CPM.isStrictMode() && (longitudinalAcceleration > 102 || longitudinalAcceleration < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence longitudinalAcceleration should be in the range of [0 - 102]."
                    + " Value: " + longitudinalAcceleration);
        }
        this.longitudinalAcceleration = longitudinalAcceleration;
        if(lateralAcceleration != UNKNOWN && CPM.isStrictMode() && (lateralAcceleration > 102 || lateralAcceleration < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence lateralAcceleration should be in the range of [0 - 102]."
                    + " Value: " + lateralAcceleration);
        }
        this.lateralAcceleration = lateralAcceleration;
        if(verticalAcceleration != UNKNOWN && CPM.isStrictMode() && (verticalAcceleration > 102 || verticalAcceleration < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleConfidence verticalAcceleration should be in the range of [0 - 102]."
                    + " Value: " + verticalAcceleration);
        }
        this.verticalAcceleration = verticalAcceleration;

        createJson();
    }

    private void createJson() {
        try {
            if(heading != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.HEADING.key(), heading);
            if(speed != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.SPEED.key(), speed);
            if(vehicleLength != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_LENGTH.key(), vehicleLength);
            if(vehicleWidth != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_WIDTH.key(), vehicleWidth);
            if(yawRate != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.YAW_RATE.key(), yawRate);
            if(longitudinalAcceleration != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.LONGITUDINAL_ACCELERATION.key(), longitudinalAcceleration);
            if(lateralAcceleration != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.LATERAL_ACCELERATION.key(), lateralAcceleration);
            if(verticalAcceleration != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.VERTICAL_ACCELERATION.key(), verticalAcceleration);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM OriginatingVehicleConfidence JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getHeading() {
        return heading;
    }

    public int getSpeed() {
        return speed;
    }

    public int getVehicleWidth() {
        return vehicleWidth;
    }

    public int getYawRate() {
        return yawRate;
    }

    public int getLongitudinalAcceleration() {
        return longitudinalAcceleration;
    }

    public int getLateralAcceleration() {
        return lateralAcceleration;
    }

    public int getVerticalAcceleration() {
        return verticalAcceleration;
    }

    public static OriginatingVehicleConfidence jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        int heading = json.optInt(JsonCpmKey.OriginatingVehicleContainer.HEADING.key(), UNKNOWN);
        int speed = json.optInt(JsonCpmKey.OriginatingVehicleContainer.SPEED.key(), UNKNOWN);
        int vehicleLength = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_LENGTH.key(), UNKNOWN);
        int vehicleWidth = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_WIDTH.key(), UNKNOWN);
        int yawRate = json.optInt(JsonCpmKey.OriginatingVehicleContainer.YAW_RATE.key(), UNKNOWN);
        int longitudinalAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.LONGITUDINAL_ACCELERATION.key(), UNKNOWN);
        int lateralAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.LATERAL_ACCELERATION.key(), UNKNOWN);
        int verticalAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VERTICAL_ACCELERATION.key(), UNKNOWN);

        return new OriginatingVehicleConfidence(
                heading,
                speed,
                vehicleLength,
                vehicleWidth,
                yawRate,
                longitudinalAcceleration,
                lateralAcceleration,
                verticalAcceleration);
    }

}
