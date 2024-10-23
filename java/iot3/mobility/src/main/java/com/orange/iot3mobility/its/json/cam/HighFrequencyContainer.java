/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cam;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.EtsiUtils;
import com.orange.iot3mobility.its.json.JsonKey;

import com.orange.iot3mobility.its.json.cpm.PerceivedObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CAM HighFrequencyContainer.
 * <p>
 * Provides detailed information about a vehicle's status.
 */
public class HighFrequencyContainer {

    private static final Logger LOGGER = Logger.getLogger(HighFrequencyContainer.class.getName());

    private final JSONObject jsonHighFrequencyContainer = new JSONObject();

    /**
     * Unit: 0.1 degree. Heading of the vehicle movement of the originating ITS-S
     * with regards to the true north.
     * <p> 
     * wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601)
     */
    private final int heading;

    /**
     * Unit 0.01 m/s. Driving speed of the originating ITS-S.
     * <p>
     * standstill(0), oneCentimeterPerSec(1), unavailable(16383)
     */
    private final int speed;

    /**
     * Vehicle drive direction (forward or backward) of the originating ITS-S.
     * <p>
     * forward (0), backward (1), unavailable (2)
     */
    private final int driveDirection;

    /**
     * Unit 0.1 m. Length of the ITS-S.
     * <p>
     * tenCentimeters(1), outOfRange(1022), unavailable(1023)
     */
    private final int vehicleLength;

    /**
     * Unit 0.1 m. Width of the ITS-S.
     * <p>
     * tenCentimeters(1), outOfRange(61), unavailable(62)
     */
    private final int vehicleWidth;

    /**
     * unit: 0.1 m/s2. Vehicle longitudinal acceleration of the originating ITS-S in the centre of the mass
     * of the empty vehicle.
     * <p>
     * pointOneMeterPerSecSquaredForward(1), pointOneMeterPerSecSquaredBackward(-1), unavailable(161)
     */
    private final int longitudinalAcceleration;

    /**
     * Unit: 0.1 m/s2. Vehicle lateral acceleration of the originating ITS-S in the centre of the mass of
     * the empty vehicle.
     * <p>
     * pointOneMeterPerSecSquaredToRight(-1), pointOneMeterPerSecSquaredToLeft(1), unavailable(161)
     */
    private final int lateralAcceleration;

    /**
     * Unit: 0.1 m/s2. Vertical Acceleration of the originating ITS-S in the centre of the mass of the
     * empty vehicle.
     * <p>
     * pointOneMeterPerSecSquaredUp(1), pointOneMeterPerSecSquaredDown(-1), unavailable(161)
     */
    private final int verticalAcceleration;

    /**
     * Unit: 0.01 degree/s. Vehicle rotation around the centre of mass of
     * the empty vehicle. The leading sign denotes the direction of rotation.
     * The value is negative if the motion is clockwise when viewing from the
     * top.
     * <p>
     * straight(0), degSec-000-01ToRight(-1), degSec-000-01ToLeft(1), unavailable(32767)
     */
    private final int yawRate;

    /**
     * The lanePosition of the referencePosition of a vehicle, counted from the
     * outside border of the road, in the direction of the traffic flow.
     * <p>
     * offTheRoad(-1), innerHardShoulder(0), innermostDrivingLane(1), secondLaneFromInside(2), outterHardShoulder(14)
     */
    private final int lanePosition;

    /**
     * Inverse of the vehicle current curve radius and the turning direction of the curve with regards to the driving
     * direction of the vehicle.
     * <p>
     * straight(0), unavailable(1023)
     */
    private final int curvature;

    /**
     * It describes whether the yaw rate is used to calculate the curvature.
     * <p>
     * yawRateUsed(0), yawRateNotUsed(1), unavailable(2)
     */
    private final int curvatureCalculationMode;

    /**
     * Current controlling mechanism for longitudinal movement of the vehicle. Represented as a bit string:
     * <p>
     * brakePedalEngaged (0), gasPedalEngaged (1), emergencyBrakeEngaged (2), collisionWarningEngaged(3),
     * accEngaged(4), cruiseControlEngaged(5), speedLimiterEngaged(6)
     */
    private final String accelerationControl;

    /**
     * equalOrWithinZeroPointOneDegree (1), equalOrWithinOneDegree (10), outOfRange(126), unavailable(127)
     */
    private final int headingConfidence;

    /**
     * equalOrWithinOneCentimeterPerSec(1), equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127)
     */
    private final int speedConfidence;

    /**
     * noTrailerPresent(0), trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2),
     * trailerPresenceIsUnknown(3), unavailable(4)
     */
    private final int vehicleLengthConfidence;

    /**
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
     */
    private final int longitudinalAccelerationConfidence;

    /**
     * Unit: 0.1 m/s2. 
     * <p>
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
     */
    private final int lateralAccelerationConfidence;

    /**
     * Unit: 0.1 m/s2. 
     * <p>
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102)
     */
    private final int verticalAccelerationConfidence;

    /**
     * degSec-000-01 (0), degSec-000-05 (1), degSec-000-10 (2), degSec-001-00 (3), degSec-005-00 (4), degSec-010-00 (5), 
     * degSec-100-00 (6), outOfRange (7), unavailable (8)
     */
    private final int yawRateConfidence;

    /**
     * onePerMeter-0-00002 (0), onePerMeter-0-0001 (1), onePerMeter-0-0005 (2), onePerMeter-0-002 (3),
     * onePerMeter-0-01 (4), onePerMeter-0-1 (5), outOfRange (6), unavailable (7)
     */
    private final int curvatureConfidence;

    /**
     * Build a CAM HighFrequencyContainer.
     * <p>
     * Provides detailed information about vehicle's status.
     */
    private HighFrequencyContainer(
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
            if(!accelerationControl.isEmpty())
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.ACCELERATION_CONTROL.key(), accelerationControl);
            if(!confidence.isEmpty())
                jsonHighFrequencyContainer.put(JsonKey.HighFrequencyContainer.CONFIDENCE.key(), confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "HighFrequencyContainer JSON build error", "Error: " + e);
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

    public static class HighFrequencyContainerBuilder {
        private int heading = UNKNOWN;
        private int speed = UNKNOWN;
        private int driveDirection = UNKNOWN;
        private int vehicleLength = UNKNOWN;
        private int vehicleWidth = UNKNOWN;
        private int longitudinalAcceleration = UNKNOWN;
        private int lateralAcceleration = UNKNOWN;
        private int verticalAcceleration = UNKNOWN;
        private int yawRate = UNKNOWN;
        private int lanePosition = UNKNOWN;
        private int curvature = UNKNOWN;
        private int curvatureCalculationMode = UNKNOWN;
        private String accelerationControl = "";
        private int headingConfidence = UNKNOWN;
        private int speedConfidence = UNKNOWN;
        private int vehicleLengthConfidence = UNKNOWN;
        private int longitudinalAccelerationConfidence = UNKNOWN;
        private int lateralAccelerationConfidence = UNKNOWN;
        private int verticalAccelerationConfidence = UNKNOWN;
        private int yawRateConfidence = UNKNOWN;
        private int curvatureConfidence = UNKNOWN;

        /**
         * Start building a HighFrequencyContainer.
         */
        public HighFrequencyContainerBuilder() {
        }

        /**
         * Sets the speed of the ITS-S.
         *
         * @param speed {@link HighFrequencyContainer#speed}
         */
        public HighFrequencyContainerBuilder speed(int speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Sets the speed and speed confidence of the ITS-S.
         *
         * @param speed {@link HighFrequencyContainer#speed}
         * @param speedConfidence {@link HighFrequencyContainer#speedConfidence}
         */
        public HighFrequencyContainerBuilder speed(int speed,
                                                   int speedConfidence) {
            this.speed = speed;
            this.speedConfidence = speedConfidence;
            return this;
        }

        /**
         * Sets the headinge of the ITS-S.
         *
         * @param heading {@link HighFrequencyContainer#heading}
         */
        public HighFrequencyContainerBuilder heading(int heading) {
            this.heading = heading;
            return this;
        }

        /**
         * Sets the heading and heading confidence of the ITS-S.
         *
         * @param heading {@link HighFrequencyContainer#heading}
         * @param headingConfidence {@link HighFrequencyContainer#headingConfidence}
         */
        public HighFrequencyContainerBuilder heading(int heading,
                                                     int headingConfidence) {
            this.heading = heading;
            this.headingConfidence = headingConfidence;
            return this;
        }

        /**
         * Sets the drive direction of the ITS-S.
         *
         * @param driveDirection {@link HighFrequencyContainer#driveDirection}
         */
        public HighFrequencyContainerBuilder driveDirection(int driveDirection) {
            this.driveDirection = driveDirection;
            return this;
        }

        /**
         * Sets the size of the ITS-S.
         *
         * @param vehicleLength {@link HighFrequencyContainer#vehicleLength}
         * @param vehicleWidth {@link HighFrequencyContainer#vehicleWidth}
         */
        public HighFrequencyContainerBuilder vehicleSize(int vehicleLength,
                                                         int vehicleWidth) {
            this.vehicleLength = vehicleLength;
            this.vehicleWidth = vehicleWidth;
            return this;
        }

        /**
         * Sets the size and size confidence of the ITS-S.
         *
         * @param vehicleLength {@link HighFrequencyContainer#vehicleLength}
         * @param vehicleWidth {@link HighFrequencyContainer#vehicleWidth}
         * @param vehicleLengthConfidence {@link HighFrequencyContainer#vehicleLengthConfidence}
         */
        public HighFrequencyContainerBuilder vehicleSize(int vehicleLength,
                                                         int vehicleWidth,
                                                         int vehicleLengthConfidence) {
            this.vehicleLength = vehicleLength;
            this.vehicleWidth = vehicleWidth;
            this.vehicleLengthConfidence = vehicleLengthConfidence;
            return this;
        }

        /**
         * Sets the yaw rate of the ITS-S.
         *
         * @param yawRate {@link HighFrequencyContainer#yawRate}
         */
        public HighFrequencyContainerBuilder yawRate(int yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        /**
         * Sets the yaw rate and yaw rate confidence of the ITS-S.
         *
         * @param yawRate {@link HighFrequencyContainer#yawRate}
         * @param yawRateConfidence {@link HighFrequencyContainer#yawRateConfidence}
         */
        public HighFrequencyContainerBuilder yawRate(int yawRate,
                                                     int yawRateConfidence) {
            this.yawRate = yawRate;
            this.yawRateConfidence = yawRateConfidence;
            return this;
        }

        /**
         * Sets the longitudinal acceleration and of the ITS-S.
         *
         * @param longitudinalAcceleration {@link HighFrequencyContainer#longitudinalAcceleration}
         */
        public HighFrequencyContainerBuilder longitudinalAcceleration(int longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            return this;
        }

        /**
         * Sets the longitudinal acceleration and of the ITS-S and its confidence.
         *
         * @param longitudinalAcceleration {@link HighFrequencyContainer#longitudinalAcceleration}
         * @param longitudinalAccelerationConfidence {@link HighFrequencyContainer#longitudinalAccelerationConfidence}
         */
        public HighFrequencyContainerBuilder longitudinalAcceleration(int longitudinalAcceleration,
                                                                      int longitudinalAccelerationConfidence) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            this.longitudinalAccelerationConfidence = longitudinalAccelerationConfidence;
            return this;
        }

        /**
         * Sets the lateral acceleration and of the ITS-S.
         *
         * @param lateralAcceleration {@link HighFrequencyContainer#lateralAcceleration}
         */
        public HighFrequencyContainerBuilder lateralAcceleration(int lateralAcceleration) {
            this.lateralAcceleration = lateralAcceleration;
            return this;
        }

        /**
         * Sets the lateral acceleration and of the ITS-S and its confidence.
         *
         * @param lateralAcceleration {@link HighFrequencyContainer#lateralAcceleration}
         * @param lateralAccelerationConfidence {@link HighFrequencyContainer#lateralAccelerationConfidence}
         */
        public HighFrequencyContainerBuilder lateralAcceleration(int lateralAcceleration,
                                                                 int lateralAccelerationConfidence) {
            this.lateralAcceleration = lateralAcceleration;
            this.lateralAccelerationConfidence = lateralAccelerationConfidence;
            return this;
        }

        /**
         * Sets the vertical acceleration and of the ITS-S.
         *
         * @param verticalAcceleration {@link HighFrequencyContainer#verticalAcceleration}
         */
        public HighFrequencyContainerBuilder verticalAcceleration(int verticalAcceleration) {
            this.verticalAcceleration = verticalAcceleration;
            return this;
        }

        /**
         * Sets the vertical acceleration and of the ITS-S and its confidence.
         *
         * @param verticalAcceleration {@link HighFrequencyContainer#verticalAcceleration}
         * @param verticalAccelerationConfidence {@link HighFrequencyContainer#verticalAccelerationConfidence}
         */
        public HighFrequencyContainerBuilder verticalAcceleration(int verticalAcceleration,
                                                                  int verticalAccelerationConfidence) {
            this.verticalAcceleration = verticalAcceleration;
            this.verticalAccelerationConfidence = verticalAccelerationConfidence;
            return this;
        }

        /**
         * Sets the acceleration control mechanism of the ITS-S.
         *
         * @param accelerationControl {@link HighFrequencyContainer#accelerationControl}
         */
        public HighFrequencyContainerBuilder accelerationControl(String accelerationControl) {
            this.accelerationControl = accelerationControl;
            return this;
        }

        /**
         * Sets the curvature of the ITS-S and its calculation mode.
         *
         * @param curvature {@link HighFrequencyContainer#curvature}
         * @param curvatureCalculationMode {@link HighFrequencyContainer#curvatureCalculationMode}
         */
        public HighFrequencyContainerBuilder curvature(int curvature,
                                                       int curvatureCalculationMode) {
            this.curvature = curvature;
            this.curvatureCalculationMode = curvatureCalculationMode;
            return this;
        }

        /**
         * Sets the curvature of the ITS-S, and its confidence and calculation mode.
         *
         * @param curvature {@link HighFrequencyContainer#curvature}
         * @param curvatureConfidence {@link HighFrequencyContainer#curvatureConfidence}
         * @param curvatureCalculationMode {@link HighFrequencyContainer#curvatureCalculationMode}
         */
        public HighFrequencyContainerBuilder curvature(int curvature,
                                                       int curvatureConfidence,
                                                       int curvatureCalculationMode) {
            this.curvature = curvature;
            this.curvatureConfidence = curvatureConfidence;
            this.curvatureCalculationMode = curvatureCalculationMode;
            return this;
        }

        /**
         * Sets the lane position of the ITS-S.
         *
         * @param lanePosition {@link HighFrequencyContainer#lanePosition}
         */
        public HighFrequencyContainerBuilder lanePosition(int lanePosition) {
            this.lanePosition = lanePosition;
            return this;
        }

        /**
         * Build the HighFrequencyContainer.
         *
         * @return {@link #HighFrequencyContainer}
         */
        public HighFrequencyContainer build() {
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

    public static HighFrequencyContainer jsonParser(JSONObject jsonHighFrequencyContainer) {
        if(jsonHighFrequencyContainer == null || jsonHighFrequencyContainer.isEmpty()) return null;
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

        return new HighFrequencyContainerBuilder()
                .speed(speed, speedConfidence)
                .heading(heading, headingConfidence)
                .driveDirection(driveDirection)
                .longitudinalAcceleration(longitudinalAcceleration, longitudinalAccelerationConfidence)
                .lateralAcceleration(lateralAcceleration, lateralAccelerationConfidence)
                .verticalAcceleration(verticalAcceleration, verticalAccelerationConfidence)
                .accelerationControl(accelerationControl)
                .yawRate(yawRate, yawRateConfidence)
                .vehicleSize(vehicleLength, vehicleWidth, vehicleLengthConfidence)
                .curvature(curvature, curvatureConfidence, curvatureCalculationMode)
                .lanePosition(lanePosition)
                .build();
    }

}
