/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cam;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.EtsiUtils;
import com.orange.iot3mobility.its.json.JsonKey;

import org.json.JSONException;
import org.json.JSONObject;

public class HighFrequencyContainer {

    private final JSONObject jsonHighFrequencyContainer = new JSONObject();
    private final int heading;
    private final int speed;
    private final int driveDirection;
    private final int vehicleLength;
    private final int vehicleWidth;
    private final int longitudinalAcceleration;
    private final int lateralAcceleration;
    private final int verticalAcceleration;
    private final int yawRate;
    private final int lanePosition;
    private final int curvature;
    private final int curvatureCalculationMode;
    private final String accelerationControl;
    private final int headingConfidence;
    private final int speedConfidence;
    private final int vehicleLengthConfidence;
    private final int longitudinalAccelerationConfidence;
    private final int lateralAccelerationConfidence;
    private final int verticalAccelerationConfidence;
    private final int yawRateConfidence;
    private final int curvatureConfidence;

    public HighFrequencyContainer(
            final int heading,
            final int speed,
            final int longitudinalAcceleration,
            final int yawRate)
    {
        this(
                heading,
                speed,
                UNKNOWN,
                UNKNOWN,
                longitudinalAcceleration,
                yawRate,
                UNKNOWN);
    }

    public HighFrequencyContainer(
            final int heading,
            final int speed,
            final int vehicleLength,
            final int vehicleWidth,
            final int longitudinalAcceleration,
            final int yawRate,
            final int lanePosition)
    {
        this(
                heading,
                speed,
                UNKNOWN,
                vehicleLength,
                vehicleWidth,
                longitudinalAcceleration,
                UNKNOWN,
                UNKNOWN,
                yawRate,
                lanePosition,
                UNKNOWN,
                UNKNOWN,
                "",
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN);
    }

    public HighFrequencyContainer(
            final int heading,
            final int speed,
            final int driveDirection,
            final int vehicleLength,
            final int vehicleWidth,
            final int longitudinalAcceleration,
            final int lateralAcceleration,
            final int verticalAcceleration,
            final int yawRate,
            final int lanePosition,
            final int curvature,
            final int curvatureCalculationMode,
            final String accelerationControl,
            final int headingConfidence,
            final int speedConfidence,
            final int vehicleLengthConfidence,
            final int longitudinalAccelerationConfidence,
            final int lateralAccelerationConfidence,
            final int verticalAccelerationConfidence,
            final int yawRateConfidence,
            final int curvatureConfidence)
    {
        if(heading != UNKNOWN && (heading > 3601 || heading < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer Heading should be in the range of [0 - 3601]."
                    + " Value: " + heading);
        }
        this.heading = heading;
        if(speed != UNKNOWN && (speed > 16383 || speed < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer Speed should be in the range of [0 - 16383]."
                    + " Value: " + speed);
        }
        this.speed = speed;
        if(driveDirection != UNKNOWN && (driveDirection > 2 || driveDirection < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer DriveDirection should be in the range of [0 - 2]."
                    + " Value: " + driveDirection);
        }
        this.driveDirection = driveDirection;
        if(vehicleLength != UNKNOWN && (vehicleLength > 1023 || vehicleLength < 1)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer VehicleLength should be in the range of [1 - 1023]."
                    + " Value: " + vehicleLength);
        }
        this.vehicleLength = vehicleLength;
        if(vehicleWidth != UNKNOWN && (vehicleWidth > 62 || vehicleWidth < 1)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer VehicleWidth should be in the range of [1 - 62]."
                    + " Value: " + vehicleWidth);
        }
        this.vehicleWidth = vehicleWidth;
        if(longitudinalAcceleration != UNKNOWN && (longitudinalAcceleration > 161 || longitudinalAcceleration < -160)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer LongitudinalAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + longitudinalAcceleration);
        }
        this.longitudinalAcceleration = longitudinalAcceleration;
        if(lateralAcceleration != UNKNOWN && (lateralAcceleration > 161 || lateralAcceleration < -160)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer LateralAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + lateralAcceleration);
        }
        this.lateralAcceleration = lateralAcceleration;
        if(verticalAcceleration != UNKNOWN && (verticalAcceleration > 161 || verticalAcceleration < -160)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer VerticalAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + verticalAcceleration);
        }
        this.verticalAcceleration = verticalAcceleration;
        if(yawRate != UNKNOWN && (yawRate > 32767 || yawRate < -32766)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer YawRate should be in the range of [-32766 - 32767]."
                    + " Value: " + yawRate);
        }
        this.yawRate = yawRate;
        if(lanePosition != UNKNOWN && (lanePosition > 14 || lanePosition < -1)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer LanePosition should be in the range of [-1 - 14]."
                    + " Value: " + lanePosition);
        }
        this.lanePosition = lanePosition;
        if(curvature != UNKNOWN && (curvature > 1023 || curvature < -1023)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer Curvature should be in the range of [-1023 - 1023]."
                    + " Value: " + curvature);
        }
        this.curvature = curvature;
        if(curvatureCalculationMode != UNKNOWN && (curvatureCalculationMode > 2 || curvatureCalculationMode < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer CurvatureCalculationMode should be in the range of [0 - 2]."
                    + " Value: " + curvatureCalculationMode);
        }
        this.curvatureCalculationMode = curvatureCalculationMode;
        this.accelerationControl = accelerationControl;
        if(headingConfidence != UNKNOWN && (headingConfidence > 127 || headingConfidence < 1)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer HeadingConfidence should be in the range of [1 - 127]."
                    + " Value: " + headingConfidence);
        }
        this.headingConfidence = headingConfidence;
        if(speedConfidence != UNKNOWN && (speedConfidence > 127 || speedConfidence < 1)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer SpeedConfidence should be in the range of [1 - 127]."
                    + " Value: " + speedConfidence);
        }
        this.speedConfidence = speedConfidence;
        if(vehicleLengthConfidence != UNKNOWN && (vehicleLengthConfidence > 4 || vehicleLengthConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer VehicleLengthConfidence should be in the range of [0 - 4]."
                    + " Value: " + vehicleLengthConfidence);
        }
        this.vehicleLengthConfidence = vehicleLengthConfidence;
        if(longitudinalAccelerationConfidence != UNKNOWN && (longitudinalAccelerationConfidence > 102 || longitudinalAccelerationConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer LongitudinalAccelerationConfidence should be in the range of [0 - 102]."
                    + " Value: " + longitudinalAccelerationConfidence);
        }
        this.longitudinalAccelerationConfidence = longitudinalAccelerationConfidence;
        if(lateralAccelerationConfidence != UNKNOWN && (lateralAccelerationConfidence > 102 || lateralAccelerationConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer LateralAccelerationConfidence should be in the range of [0 - 102]."
                    + " Value: " + lateralAccelerationConfidence);
        }
        this.lateralAccelerationConfidence = lateralAccelerationConfidence;
        if(verticalAccelerationConfidence != UNKNOWN && (verticalAccelerationConfidence > 102 || verticalAccelerationConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer VerticalAccelerationConfidence should be in the range of [0 - 102]."
                    + " Value: " + verticalAccelerationConfidence);
        }
        this.verticalAccelerationConfidence = verticalAccelerationConfidence;
        if(yawRateConfidence != UNKNOWN && (yawRateConfidence > 8 || yawRateConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer YawRateConfidence should be in the range of [0 - 8]."
                    + " Value: " + yawRateConfidence);
        }
        this.yawRateConfidence = yawRateConfidence;
        if(curvatureConfidence != UNKNOWN && (curvatureConfidence > 7 || curvatureConfidence < 0)) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer CurvatureConfidence should be in the range of [0 - 7]."
                    + " Value: " + curvatureConfidence);
        }
        this.curvatureConfidence = curvatureConfidence;

        createJson();
    }

    private void createJson() {
        try {
            JSONObject confidence = new JSONObject();
            if(headingConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.HEADING.key(), headingConfidence);
            if(speedConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.SPEED.key(), speedConfidence);
            if(vehicleLengthConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.VEHICLE_LENGTH.key(), vehicleLengthConfidence);
            if(longitudinalAccelerationConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.LONGITUDINAL_ACCELERATION.key(), longitudinalAccelerationConfidence);
            if(lateralAccelerationConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.LATERAL_ACCELERATION.key(), lateralAccelerationConfidence);
            if(verticalAccelerationConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.VERTICAL_ACCELERATION.key(), verticalAccelerationConfidence);
            if(yawRateConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.YAW_RATE.key(), yawRateConfidence);
            if(curvatureConfidence != UNKNOWN)
                confidence.put(JsonKey.Confidence.CURVATURE.key(), curvatureConfidence);

            if(heading != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.HEADING.key(), heading);
            if(speed != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.SPEED.key(), speed);
            if(driveDirection != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.DRIVE_DIRECTION.key(), driveDirection);
            if(vehicleLength != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.VEHICLE_LENGTH.key(), vehicleLength);
            if(vehicleWidth != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.VEHICLE_WIDTH.key(), vehicleWidth);
            if(longitudinalAcceleration != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.LONGITUDINAL_ACCELERATION.key(), longitudinalAcceleration);
            if(lateralAcceleration != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.LATERAL_ACCELERATION.key(), lateralAcceleration);
            if(verticalAcceleration != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.VERTICAL_ACCELERATION.key(), verticalAcceleration);
            if(yawRate != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.YAW_RATE.key(), yawRate);
            if(lanePosition != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.LANE_POSITION.key(), lanePosition);
            if(curvature != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.CURVATURE.key(), curvature);
            if(curvatureCalculationMode != UNKNOWN)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.CURVATURE_CALCULATION_MODE.key(), curvatureCalculationMode);
            if(!accelerationControl.equals(""))
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.ACCELERATION_CONTROL.key(), accelerationControl);
            if(confidence.length() > 0)
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.CONFIDENCE.key(), confidence);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonHighFrequencyContainer() {
        return jsonHighFrequencyContainer;
    }

    public int getHeading() {
        return heading;
    }

    public float getHeadingDegree() {
        return (float) heading / EtsiUtils.ETSI_HEADING_FACTOR;
    }

    public int getSpeed() {
        return speed;
    }

    public float getSpeedMs() {
        return (float) speed / EtsiUtils.ETSI_SPEED_FACTOR;
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

    public int getLongitudinalAcceleration() {
        return longitudinalAcceleration;
    }

    public float getLongitudinalAccelerationMsSq() {
        if(longitudinalAcceleration != UNKNOWN){
            return (float)longitudinalAcceleration/EtsiUtils.ETSI_ACCELERATION_FACTOR;
        }else{
            return UNKNOWN;
        }
    }

    public int getLateralAcceleration() {
        return lateralAcceleration;
    }

    public float getLateralAccelerationMsSq() {
        if(lateralAcceleration != UNKNOWN){
            return (float)lateralAcceleration/EtsiUtils.ETSI_ACCELERATION_FACTOR;
        }else{
            return UNKNOWN;
        }
    }

    public int getVerticalAcceleration() {
        return verticalAcceleration;
    }

    public float getVerticalAccelerationMsSq() {
        if(verticalAcceleration != UNKNOWN){
            return (float)verticalAcceleration/EtsiUtils.ETSI_ACCELERATION_FACTOR;
        }else{
            return UNKNOWN;
        }
    }

    public int getYawRate() {
        return yawRate;
    }

    public float getYawRateDs() {
        if(yawRate != UNKNOWN){
            return (float)yawRate/EtsiUtils.ETSI_YAW_RATE_FACTOR;
        }else{
            return UNKNOWN;
        }
    }

    public int getLanePosition() {
        return lanePosition;
    }

    public int getCurvature() {
        return curvature;
    }

    public int getCurvatureCalculationMode() {
        return curvatureCalculationMode;
    }

    public String getAccelerationControl() {
        return accelerationControl;
    }

    public int getHeadingConfidence() {
        return headingConfidence;
    }

    public int getSpeedConfidence() {
        return speedConfidence;
    }

    public int getVehicleLengthConfidence() {
        return vehicleLengthConfidence;
    }

    public int getLongitudinalAccelerationConfidence() {
        return longitudinalAccelerationConfidence;
    }

    public int getLateralAccelerationConfidence() {
        return lateralAccelerationConfidence;
    }

    public int getVerticalAccelerationConfidence() {
        return verticalAccelerationConfidence;
    }

    public int getYawRateConfidence() {
        return yawRateConfidence;
    }

    public int getCurvatureConfidence() {
        return curvatureConfidence;
    }

    public static HighFrequencyContainer jsonParser(JSONObject jsonHighFrequencyContainer) {
        if(jsonHighFrequencyContainer == null || jsonHighFrequencyContainer.length() == 0) return null;
        int heading = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.HEADING.key(), UNKNOWN);
        int speed = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.SPEED.key(), UNKNOWN);
        int driveDirection = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.DRIVE_DIRECTION.key(), UNKNOWN);
        int vehicleLength = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.VEHICLE_LENGTH.key(), UNKNOWN);
        int vehicleWidth = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.VEHICLE_WIDTH.key(), UNKNOWN);
        int longitudinalAcceleration = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.LONGITUDINAL_ACCELERATION.key(), UNKNOWN);
        int lateralAcceleration = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.LATERAL_ACCELERATION.key(), UNKNOWN);
        int verticalAcceleration = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.VERTICAL_ACCELERATION.key(), UNKNOWN);
        int yawRate = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.YAW_RATE.key(), UNKNOWN);
        int lanePosition = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.LANE_POSITION.key(), UNKNOWN);
        int curvature = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.CURVATURE.key(), UNKNOWN);
        int curvatureCalculationMode = jsonHighFrequencyContainer.optInt(JsonKey.HighFrequencyContainer.CURVATURE_CALCULATION_MODE.key(), UNKNOWN);
        String accelerationControl = jsonHighFrequencyContainer.optString(JsonKey.HighFrequencyContainer.ACCELERATION_CONTROL.key(), "");

        JSONObject confidence = jsonHighFrequencyContainer.optJSONObject(JsonKey.HighFrequencyContainer.CONFIDENCE.key());
        int headingConfidence = UNKNOWN;
        int speedConfidence = UNKNOWN;
        int vehicleLengthConfidence = UNKNOWN;
        int longitudinalAccelerationConfidence = UNKNOWN;
        int lateralAccelerationConfidence = UNKNOWN;
        int verticalAccelerationConfidence = UNKNOWN;
        int yawRateConfidence = UNKNOWN;
        int curvatureConfidence = UNKNOWN;
        if(confidence != null) {
            headingConfidence = confidence.optInt(JsonKey.Confidence.HEADING.key(), UNKNOWN);
            speedConfidence = confidence.optInt(JsonKey.Confidence.SPEED.key(), UNKNOWN);
            vehicleLengthConfidence = confidence.optInt(JsonKey.Confidence.VEHICLE_LENGTH.key(), UNKNOWN);
            longitudinalAccelerationConfidence = confidence.optInt(JsonKey.Confidence.LONGITUDINAL_ACCELERATION.key(), UNKNOWN);
            lateralAccelerationConfidence = confidence.optInt(JsonKey.Confidence.LATERAL_ACCELERATION.key(), UNKNOWN);
            verticalAccelerationConfidence = confidence.optInt(JsonKey.Confidence.VERTICAL_ACCELERATION.key(), UNKNOWN);
            yawRateConfidence = confidence.optInt(JsonKey.Confidence.YAW_RATE.key(), UNKNOWN);
            curvatureConfidence = confidence.optInt(JsonKey.Confidence.CURVATURE.key(), UNKNOWN);
        }

        return new HighFrequencyContainer(
                heading,
                speed,
                driveDirection,
                vehicleLength,
                vehicleWidth,
                longitudinalAcceleration,
                lateralAcceleration,
                verticalAcceleration,
                yawRate,
                lanePosition,
                curvature,
                curvatureCalculationMode,
                accelerationControl,
                headingConfidence,
                speedConfidence,
                vehicleLengthConfidence,
                longitudinalAccelerationConfidence,
                lateralAccelerationConfidence,
                verticalAccelerationConfidence,
                yawRateConfidence,
                curvatureConfidence);
    }

}
