/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OriginatingVehicleContainer {

    private static final Logger LOGGER = Logger.getLogger(OriginatingVehicleContainer.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.1 degree. Heading of the vehicle movement with regards to the true north;
     *
     * wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
     */
    private final int heading;

    /**
     * Unit: 0.01 m/s. Driving speed.
     *
     * standstill(0), oneCentimeterPerSec(1), unavailable(16383).
     */
    private final int speed;

    /**
     * Drive direction.
     *
     * forward (0), backward (1), unavailable (2).
     */
    private final int driveDirection;

    /**
     * Unit: 10 cm. Vehicle length.
     *
     * tenCentimeters(1), outOfRange(1022), unavailable(1023).
     */
    private final int vehicleLength;

    /**
     * Unit: 10 cm. Vehicle width.
     *
     * tenCentimeters(1), outOfRange(61), unavailable(62).
     */
    private final int vehicleWidth;

    /**
     * Unit: 0.01 degree/s. Yaw rate
     *
     * straight(0), degSec-000-01ToRight(-1), degSec-000-01ToLeft(1), unavailable(32767).
     */
    private final int yawRate;

    /**
     * Unit: 0.1 m/s2. Longitudinal acceleration.
     *
     * pointOneMeterPerSecSquaredForward(1), pointOneMeterPerSecSquaredBackward(-1), unavailable(161).
     */
    private final int longitudinalAcceleration;

    /**
     * Unit: 0.1 m/s2. Lateral acceleration.
     *
     * pointOneMeterPerSecSquaredToRight(-1), pointOneMeterPerSecSquaredToLeft(1), unavailable(161).
     */
    private final int lateralAcceleration;

    /**
     * Unit: 0.1 m/s2. Vertical acceleration.
     *
     * pointOneMeterPerSecSquaredUp(1), pointOneMeterPerSecSquaredDown(-1), unavailable(161).
     */
    private final int verticalAcceleration;

    /**
     * Confidence for all the above fields.
     */
    private final OriginatingVehicleConfidence confidence;

    public OriginatingVehicleContainer(
            final int heading,
            final int speed,
            final int driveDirection,
            final int vehicleLength,
            final int vehicleWidth,
            final int yawRate,
            final int longitudinalAcceleration,
            final int lateralAcceleration,
            final int verticalAcceleration,
            final OriginatingVehicleConfidence confidence
    ) throws IllegalArgumentException {
        if(heading == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer heading missing");
        } else if(CPM.isStrictMode() && (heading > 3601 || heading < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer heading should be in the range of [0 - 3601]."
                    + " Value: " + heading);
        }
        this.heading = heading;
        if(speed == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer speed missing");
        } else if(CPM.isStrictMode() && (speed > 16383 || speed < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer speed should be in the range of [0 - 16383]."
                    + " Value: " + speed);
        }
        this.speed = speed;
        if(driveDirection != UNKNOWN && CPM.isStrictMode() && (driveDirection > 2 || driveDirection < 0)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer driveDirection should be in the range of [0 - 2]."
                    + " Value: " + driveDirection);
        }
        this.driveDirection = driveDirection;
        if(vehicleLength != UNKNOWN && CPM.isStrictMode() && (vehicleLength > 1023 || vehicleLength < 1)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer vehicleLength should be in the range of [1 - 1023]."
                    + " Value: " + vehicleLength);
        }
        this.vehicleLength = vehicleLength;
        if(vehicleWidth != UNKNOWN && CPM.isStrictMode() && (vehicleWidth > 62 || vehicleWidth < 1)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer vehicleWidth should be in the range of [1 - 62]."
                    + " Value: " + vehicleWidth);
        }
        this.vehicleWidth = vehicleWidth;
        if(yawRate != UNKNOWN && CPM.isStrictMode() && (yawRate > 32767 || yawRate < -32766)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer yawRate should be in the range of [-32766 - 32767]."
                    + " Value: " + yawRate);
        }
        this.yawRate = yawRate;
        if(longitudinalAcceleration != UNKNOWN && CPM.isStrictMode() && (longitudinalAcceleration > 161 || longitudinalAcceleration < -160)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer longitudinalAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + longitudinalAcceleration);
        }
        this.longitudinalAcceleration = longitudinalAcceleration;
        if(lateralAcceleration != UNKNOWN && CPM.isStrictMode() && (lateralAcceleration > 161 || lateralAcceleration < -160)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer lateralAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + lateralAcceleration);
        }
        this.lateralAcceleration = lateralAcceleration;
        if(verticalAcceleration != UNKNOWN && CPM.isStrictMode() && (verticalAcceleration > 161 || verticalAcceleration < -160)) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer verticalAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + verticalAcceleration);
        }
        this.verticalAcceleration = verticalAcceleration;
        if(confidence == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM OriginatingVehicleContainer confidence missing");
        }
        this.confidence = confidence;

        createJson();
    }

    private void createJson() {
        try {
            if(heading != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.HEADING.key(), heading);
            if(speed != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.SPEED.key(), speed);
            if(driveDirection != UNKNOWN)
                json.put(JsonCpmKey.OriginatingVehicleContainer.DRIVE_DIRECTION.key(), driveDirection);
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
            if(confidence != null)
                json.put(JsonCpmKey.OriginatingVehicleContainer.CONFIDENCE.key(), confidence.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM OriginatingVehicleContainer JSON build error", "Error: " + e);
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

    public int getDriveDirection() {
        return driveDirection;
    }

    public int getVehicleLength() {
        return vehicleLength;
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

    public static OriginatingVehicleContainer jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        try {
            int heading = json.getInt(JsonCpmKey.OriginatingVehicleContainer.HEADING.key());
            int speed = json.getInt(JsonCpmKey.OriginatingVehicleContainer.SPEED.key());
            int driveDirection = json.optInt(JsonCpmKey.OriginatingVehicleContainer.DRIVE_DIRECTION.key(), UNKNOWN);
            int vehicleLength = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_LENGTH.key(), UNKNOWN);
            int vehicleWidth = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VEHICLE_WIDTH.key(), UNKNOWN);
            int yawRate = json.optInt(JsonCpmKey.OriginatingVehicleContainer.YAW_RATE.key(), UNKNOWN);
            int longitudinalAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.LONGITUDINAL_ACCELERATION.key(), UNKNOWN);
            int lateralAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.LATERAL_ACCELERATION.key(), UNKNOWN);
            int verticalAcceleration = json.optInt(JsonCpmKey.OriginatingVehicleContainer.VERTICAL_ACCELERATION.key(), UNKNOWN);
            JSONObject jsonConfidence = json.getJSONObject(JsonCpmKey.OriginatingVehicleContainer.CONFIDENCE.key());
            OriginatingVehicleConfidence confidence = OriginatingVehicleConfidence.jsonParser(jsonConfidence);

            return new OriginatingVehicleContainer(
                    heading,
                    speed,
                    driveDirection,
                    vehicleLength,
                    vehicleWidth,
                    yawRate,
                    longitudinalAcceleration,
                    lateralAcceleration,
                    verticalAcceleration,
                    confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM OriginatingVehicleContainer JSON build error", "Error: " + e);
        }
        return null;
    }

}
