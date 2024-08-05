/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

public class PerceivedObjectConfidence {

    private final JSONObject json = new JSONObject();

    /**
     * Unit: 0.01 meter. Distance confidence to detected object from the reference point in
     * x-direction at the time of measurement.
     *
     * Absolute accuracy of measurement to a confidence level of 95%.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
     */
    private final int xDistance;

    /**
     * Unit: 0.01 meter. Distance confidence to detected object from the reference point in
     * y-direction at the time of measurement.
     *
     * Absolute accuracy of measurement to a confidence level of 95%.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
     */
    private final int yDistance;

    /**
     * Unit: 0.01 meter. Distance confidence to detected object from the reference point in
     * z-direction at the time of measurement.
     *
     * Absolute accuracy of measurement to a confidence level of 95%.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
     */
    private final int zDistance;

    /**
     * Speed confidence of detected object from the reference point in x-direction at the time
     * of measurement.
     *
     * unavailable(0) Not Equipped or unavailable,
     * prec100ms(1) 100  meters / sec,
     * prec10ms(2) 10 meters / sec,
     * prec5ms(3) 5 meters / sec,
     * prec1ms(4) 1 meters / sec,
     * prec0-1ms(5) 0.1 meters / sec,
     * prec0-05ms(6) 0.05 meters / sec,
     * prec0-01ms(7) 0.01 meters / sec
     */
    private final int xSpeed;

    /**
     * Speed confidence of detected object from the reference point in y-direction at the time
     * of measurement.
     *
     * unavailable(0) Not Equipped or unavailable,
     * prec100ms(1) 100  meters / sec,
     * prec10ms(2) 10 meters / sec,
     * prec5ms(3) 5 meters / sec,
     * prec1ms(4) 1 meters / sec,
     * prec0-1ms(5) 0.1 meters / sec,
     * prec0-05ms(6) 0.05 meters / sec,
     * prec0-01ms(7) 0.01 meters / sec
     */
    private final int ySpeed;

    /**
     * Speed confidence of detected object from the reference point in z-direction at the time
     * of measurement.
     *
     * unavailable(0) Not Equipped or unavailable,
     * prec100ms(1) 100  meters / sec,
     * prec10ms(2) 10 meters / sec,
     * prec5ms(3) 5 meters / sec,
     * prec1ms(4) 1 meters / sec,
     * prec0-1ms(5) 0.1 meters / sec,
     * prec0-05ms(6) 0.05 meters / sec,
     * prec0-01ms(7) 0.01 meters / sec
     */
    private final int zSpeed;

    /**
     * Acceleration confidence of detected object from the reference point in x-direction at the
     * time of measurement.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int xAcceleration;

    /**
     * Acceleration confidence of detected object from the reference point in y-direction at the
     * time of measurement.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int yAcceleration;

    /**
     * Acceleration confidence of detected object from the reference point in z-direction at the
     * time of measurement.
     *
     * pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
     */
    private final int zAcceleration;

    /**
     * Roll angle confidence. The absolute accuracy of a reported angle value for a predefined
     * confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     *
     * zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
     */
    private final int rollAngle;

    /**
     * Pitch angle confidence. The absolute accuracy of a reported angle value for a predefined
     * confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     *
     * zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
     */
    private final int pitchAngle;

    /**
     * Yaw angle confidence. The absolute accuracy of a reported angle value for a predefined
     * confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     *
     * zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
     */
    private final int yawAngle;

    /**
     * Roll rate confidence. The absolute accuracy of a reported angular speed value for a
     * predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSec-000-01(0) if the accuracy is equal to or less than 0.01 degree/second,
     * degSec-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second,
     * degSec-000-10(2) if the accuracy is equal to or less than 0.1 degree/second,
     * degSec-001-00(3) if the accuracy is equal to or less than 1 degree/second,
     * degSec-005-00(4) if the accuracy is equal to or less than 5 degrees/second,
     * degSec-010-00(5) if the accuracy is equal to or less than 10 degrees/second,
     * degSec-100-00(6) if the accuracy is equal to or less than 100 degrees/second,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int rollRate;

    /**
     * Pitch rate confidence. The absolute accuracy of a reported angular speed value for a
     * predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSec-000-01(0) if the accuracy is equal to or less than 0.01 degree/second,
     * degSec-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second,
     * degSec-000-10(2) if the accuracy is equal to or less than 0.1 degree/second,
     * degSec-001-00(3) if the accuracy is equal to or less than 1 degree/second,
     * degSec-005-00(4) if the accuracy is equal to or less than 5 degrees/second,
     * degSec-010-00(5) if the accuracy is equal to or less than 10 degrees/second,
     * degSec-100-00(6) if the accuracy is equal to or less than 100 degrees/second,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int pitchRate;

    /**
     * Yaw rate confidence. The absolute accuracy of a reported angular speed value for a
     * predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSec-000-01(0) if the accuracy is equal to or less than 0.01 degree/second,
     * degSec-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second,
     * degSec-000-10(2) if the accuracy is equal to or less than 0.1 degree/second,
     * degSec-001-00(3) if the accuracy is equal to or less than 1 degree/second,
     * degSec-005-00(4) if the accuracy is equal to or less than 5 degrees/second,
     * degSec-010-00(5) if the accuracy is equal to or less than 10 degrees/second,
     * degSec-100-00(6) if the accuracy is equal to or less than 100 degrees/second,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int yawRate;

    /**
     * Roll acceleration confidence.The absolute accuracy of a reported angular acceleration value
     * for a predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSecSquared-000-01(0) if the accuracy is equal to or less than 0.01 degree/second^2,
     * degSecSquared-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second^2,
     * degSecSquared-000-10(2) if the accuracy is equal to or less than 0.1 degree/second^2,
     * degSecSquared-001-00(3) if the accuracy is equal to or less than 1 degree/second^2,
     * degSecSquared-005-00(4) if the accuracy is equal to or less than 5 degrees/second^2,
     * degSecSquared-010-00(5) if the accuracy is equal to or less than 10 degrees/second^2,
     * degSecSquared-100-00(6) if the accuracy is equal to or less than 100 degrees/second^2,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second^2,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int rollAcceleration;

    /**
     * Pitch acceleration confidence.The absolute accuracy of a reported angular acceleration value
     * for a predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSecSquared-000-01(0) if the accuracy is equal to or less than 0.01 degree/second^2,
     * degSecSquared-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second^2,
     * degSecSquared-000-10(2) if the accuracy is equal to or less than 0.1 degree/second^2,
     * degSecSquared-001-00(3) if the accuracy is equal to or less than 1 degree/second^2,
     * degSecSquared-005-00(4) if the accuracy is equal to or less than 5 degrees/second^2,
     * degSecSquared-010-00(5) if the accuracy is equal to or less than 10 degrees/second^2,
     * degSecSquared-100-00(6) if the accuracy is equal to or less than 100 degrees/second^2,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second^2,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int pitchAcceleration;

    /**
     * Yaw acceleration confidence.The absolute accuracy of a reported angular acceleration value
     * for a predefined confidence level (e.g. 95 %).
     *
     * The required confidence level is defined by the corresponding standards.
     * For correlation computation, maximum interval levels shall be assumed.
     *
     * degSecSquared-000-01(0) if the accuracy is equal to or less than 0.01 degree/second^2,
     * degSecSquared-000-05(1) if the accuracy is equal to or less than 0.05 degrees/second^2,
     * degSecSquared-000-10(2) if the accuracy is equal to or less than 0.1 degree/second^2,
     * degSecSquared-001-00(3) if the accuracy is equal to or less than 1 degree/second^2,
     * degSecSquared-005-00(4) if the accuracy is equal to or less than 5 degrees/second^2,
     * degSecSquared-010-00(5) if the accuracy is equal to or less than 10 degrees/second^2,
     * degSecSquared-100-00(6) if the accuracy is equal to or less than 100 degrees/second^2,
     * outOfRange(7) if the accuracy is out of range, i.e. greater than 100 degrees/second^2,
     * unavailable(8) if the accuracy information is unavailable
     */
    private final int yawAcceleration;

    /**
     * Unit: 0.01 m. Accuracy of first provided dimension value with a predefined confidence
     * level (e.g. 95%).
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
     */
    private final int planarObjectDimension1;

    /**
     * Unit: 0.01 m. Accuracy of second provided dimension value with a predefined confidence
     * level (e.g. 95%).
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
     */
    private final int planarObjectDimension2;

    /**
     * Unit: 0.01 m. Accuracy of vertical provided dimension value with a predefined confidence
     * level (e.g. 95%).
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
     */
    private final int verticalObjectDimension;

    /**
     * Unit: 0.01 m. Absolute accuracy of longitudinal lane position measurement to a confidence
     * level of 95%.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101) shall be set if the accuracy
     * is out of range, unavailable(102) shall be set if the accuracy data is unavailable
     */
    private final int longitudinalLanePosition;

    /**
     * The confidence associated to the object.
     *
     * The computation of the object confidence is based on a sensor's or, fusion system's specific
     * detection confidence, the binary detection success that is, if an object has been
     * successfully detected by the last measurement and the object age.
     *
     * A single-value indication about the overall information quality of a perceived object.
     * Its computation is based on several scaling factors and moving averages.
     *
     * noConfidence(0) no confidence in detected object, e.g. for ghost-objects or if confidence
     * could not be computed, fullConfidence(15) full confidence in detected object
     */
    private final int object;

    public PerceivedObjectConfidence(
            final int xDistance,
            final int yDistance,
            final int zDistance,
            final int xSpeed,
            final int ySpeed,
            final int zSpeed,
            final int xAcceleration,
            final int yAcceleration,
            final int zAcceleration,
            final int rollAngle,
            final int pitchAngle,
            final int yawAngle,
            final int rollRate,
            final int pitchRate,
            final int yawRate,
            final int rollAcceleration,
            final int pitchAcceleration,
            final int yawAcceleration,
            final int planarObjectDimension1,
            final int planarObjectDimension2,
            final int verticalObjectDimension,
            final int longitudinalLanePosition,
            final int object
    ) throws IllegalArgumentException {
        //********* DISTANCE ***********
        if(xDistance == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence xDistance is missing");
        } else if(CPM.isStrictMode() && (xDistance > 4095 || xDistance < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence xDistance should be in the range of [0 - 4095]."
                    + " Value: " + xDistance);
        }
        this.xDistance = xDistance;
        if(yDistance == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yDistance is missing");
        } else if(CPM.isStrictMode() && (yDistance > 4095 || yDistance < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yDistance should be in the range of [0 - 4095]."
                    + " Value: " + yDistance);
        }
        this.yDistance = yDistance;
        if(zDistance != UNKNOWN && CPM.isStrictMode()
                && (zDistance > 4095 || zDistance < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence zDistance should be in the range of [0 - 4095]."
                    + " Value: " + zDistance);
        }
        this.zDistance = zDistance;
        //********* SPEED ***********
        if(xSpeed == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence xSpeed is missing");
        } else if(CPM.isStrictMode() && (xSpeed > 7 || xSpeed < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence xSpeed should be in the range of [0 - 7]."
                    + " Value: " + xSpeed);
        }
        this.xSpeed = xSpeed;
        if(ySpeed == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence ySpeed is missing");
        } else if(CPM.isStrictMode() && (ySpeed > 7 || ySpeed < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence ySpeed should be in the range of [0 - 7]."
                    + " Value: " + ySpeed);
        }
        this.ySpeed = ySpeed;
        if(zSpeed != UNKNOWN && CPM.isStrictMode()
                && (zSpeed > 7 || zSpeed < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence zSpeed should be in the range of [0 - 7]."
                    + " Value: " + zSpeed);
        }
        this.zSpeed = zSpeed;
        //********* ACCELERATION ***********
        if(xAcceleration != UNKNOWN && CPM.isStrictMode()
                && (xAcceleration > 102 || xAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence xAcceleration should be in the range of [0 - 102]."
                    + " Value: " + xAcceleration);
        }
        this.xAcceleration = xAcceleration;
        if(yAcceleration != UNKNOWN && CPM.isStrictMode()
                && (yAcceleration > 102 || yAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yAcceleration should be in the range of [0 - 102]."
                    + " Value: " + yAcceleration);
        }
        this.yAcceleration = yAcceleration;
        if(zAcceleration != UNKNOWN && CPM.isStrictMode()
                && (zAcceleration > 102 || zAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence zAcceleration should be in the range of [0 - 102]."
                    + " Value: " + zAcceleration);
        }
        this.zAcceleration = zAcceleration;
        //********* ROTATION ***********
        if(rollAngle != UNKNOWN && CPM.isStrictMode()
                && (rollAngle > 127 || rollAngle < 1)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence rollAngle should be in the range of [1 - 127]."
                    + " Value: " + rollAngle);
        }
        this.rollAngle = rollAngle;
        if(pitchAngle != UNKNOWN && CPM.isStrictMode()
                && (pitchAngle > 127 || pitchAngle < 1)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence pitchAngle should be in the range of [1 - 127]."
                    + " Value: " + pitchAngle);
        }
        this.pitchAngle = pitchAngle;
        if(yawAngle != UNKNOWN && CPM.isStrictMode()
                && (yawAngle > 127 || yawAngle < 1)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yawAngle should be in the range of [1 - 127]."
                    + " Value: " + yawAngle);
        }
        this.yawAngle = yawAngle;
        //********* ROTATION RATE ***********
        if(rollRate != UNKNOWN && CPM.isStrictMode()
                && (rollRate > 8 || rollRate < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence rollRate should be in the range of [0 - 8]."
                    + " Value: " + rollRate);
        }
        this.rollRate = rollRate;
        if(pitchRate != UNKNOWN && CPM.isStrictMode()
                && (pitchRate > 8 || pitchRate < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence pitchRate should be in the range of [0 - 8]."
                    + " Value: " + pitchRate);
        }
        this.pitchRate = pitchRate;
        if(yawRate != UNKNOWN && CPM.isStrictMode()
                && (yawRate > 8 || yawRate < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yawRate should be in the range of [0 - 8]."
                    + " Value: " + yawRate);
        }
        this.yawRate = yawRate;
        //********* ROTATION ACCELERATION ***********
        if(rollAcceleration != UNKNOWN && CPM.isStrictMode()
                && (rollAcceleration > 8 || rollAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence rollAcceleration should be in the range of [0 - 8]."
                    + " Value: " + rollAcceleration);
        }
        this.rollAcceleration = rollAcceleration;
        if(pitchAcceleration != UNKNOWN && CPM.isStrictMode()
                && (pitchAcceleration > 8 || pitchAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence pitchAcceleration should be in the range of [0 - 8]."
                    + " Value: " + pitchAcceleration);
        }
        this.pitchAcceleration = pitchAcceleration;
        if(yawAcceleration != UNKNOWN && CPM.isStrictMode()
                && (yawAcceleration > 8 || yawAcceleration < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence yawAcceleration should be in the range of [0 - 8]."
                    + " Value: " + yawAcceleration);
        }
        this.yawAcceleration = yawAcceleration;
        //********* OBJECT DIMENSION ***********
        if(planarObjectDimension1 != UNKNOWN && CPM.isStrictMode()
                && (planarObjectDimension1 > 102 || planarObjectDimension1 < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence planarObjectDimension1 should be in the range of [0 - 102]."
                    + " Value: " + planarObjectDimension1);
        }
        this.planarObjectDimension1 = planarObjectDimension1;
        if(planarObjectDimension2 != UNKNOWN && CPM.isStrictMode()
                && (planarObjectDimension2 > 102 || planarObjectDimension2 < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence planarObjectDimension2 should be in the range of [0 - 102]."
                    + " Value: " + planarObjectDimension2);
        }
        this.planarObjectDimension2 = planarObjectDimension2;
        if(verticalObjectDimension != UNKNOWN && CPM.isStrictMode()
                && (verticalObjectDimension > 102 || verticalObjectDimension < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence verticalObjectDimension should be in the range of [0 - 102]."
                    + " Value: " + verticalObjectDimension);
        }
        this.verticalObjectDimension = verticalObjectDimension;

        if(longitudinalLanePosition != UNKNOWN && CPM.isStrictMode()
                && (longitudinalLanePosition > 102 || longitudinalLanePosition < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence longitudinalLanePosition should be in the range of [0 - 102]."
                    + " Value: " + verticalObjectDimension);
        }
        this.longitudinalLanePosition = longitudinalLanePosition;

        if(object == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence object is missing");
        } else if(CPM.isStrictMode() && (object > 15 || object < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObjectConfidence object should be in the range of [0 - 15]."
                    + " Value: " + object);
        }
        this.object = object;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectDistance.X_DISTANCE.key(), xDistance);
            json.put(JsonCpmKey.ObjectDistance.Y_DISTANCE.key(), yDistance);
            if(zDistance != UNKNOWN)
                json.put(JsonCpmKey.ObjectDistance.Z_DISTANCE.key(), zDistance);
            json.put(JsonCpmKey.ObjectSpeed.X_SPEED.key(), xSpeed);
            json.put(JsonCpmKey.ObjectSpeed.Y_SPEED.key(), ySpeed);
            if(zSpeed != UNKNOWN)
                json.put(JsonCpmKey.ObjectSpeed.Z_SPEED.key(), zSpeed);
            if(xAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.X_ACCELERATION.key(), xAcceleration);
            if(yAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.Y_ACCELERATION.key(), yAcceleration);
            if(zAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.Z_ACCELERATION.key(), zAcceleration);
            if(rollAngle != UNKNOWN)
                json.put(JsonCpmKey.ObjectAngle.ROLL_ANGLE.key(), rollAngle);
            if(pitchAngle != UNKNOWN)
                json.put(JsonCpmKey.ObjectAngle.PITCH_ANGLE.key(), pitchAngle);
            if(yawAngle != UNKNOWN)
                json.put(JsonCpmKey.ObjectAngle.YAW_ANGLE.key(), yawAngle);
            if(rollRate != UNKNOWN)
                json.put(JsonCpmKey.ObjectSpeed.ROLL_RATE.key(), rollRate);
            if(pitchRate != UNKNOWN)
                json.put(JsonCpmKey.ObjectSpeed.PITCH_RATE.key(), pitchRate);
            if(yawRate != UNKNOWN)
                json.put(JsonCpmKey.ObjectSpeed.YAW_RATE.key(), yawRate);
            if(rollAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.ROLL_ACCELERATION.key(), rollAcceleration);
            if(pitchAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.PITCH_ACCELERATION.key(), pitchAcceleration);
            if(yawAcceleration != UNKNOWN)
                json.put(JsonCpmKey.ObjectAcceleration.YAW_ACCELERATION.key(), yawAcceleration);
            if(planarObjectDimension1 != UNKNOWN)
                json.put(JsonCpmKey.ObjectDimension.PLANAR_DIMENSION_1.key(), planarObjectDimension1);
            if(planarObjectDimension2 != UNKNOWN)
                json.put(JsonCpmKey.ObjectDimension.PLANAR_DIMENSION_2.key(), planarObjectDimension2);
            if(verticalObjectDimension != UNKNOWN)
                json.put(JsonCpmKey.ObjectDimension.VERTICAL_DIMENSION.key(), verticalObjectDimension);
            if(longitudinalLanePosition != UNKNOWN)
                json.put(JsonCpmKey.ObjectLanePosition.LONGITUDINAL_LANE_POSITION.key(), longitudinalLanePosition);
            json.put(JsonCpmKey.PerceivedObjectContainer.OBJECT.key(), object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getDistanceX() {
        return xDistance;
    }

    public int getDistanceY() {
        return yDistance;
    }

    public int getDistanceZ() {
        return zDistance;
    }

    public int getSpeedX() {
        return xSpeed;
    }

    public int getSpeedY() {
        return ySpeed;
    }

    public int getSpeedZ() {
        return zSpeed;
    }

    public float getSpeedMs() {
        double speed = Math.sqrt(Math.abs(xSpeed*xSpeed) + Math.abs(ySpeed*ySpeed));
        return (float)(speed / 100f);
    }

    public int getSpeedKmh() {
        return (int)(getSpeedMs() * 3.6);
    }

    public int getAccelerationX() {
        return xAcceleration;
    }

    public int getAccelerationY() {
        return yAcceleration;
    }

    public int getAccelerationZ() {
        return zAcceleration;
    }

    public int getRollAngle() {
        return rollAngle;
    }

    public int getPitchAngle() {
        return pitchAngle;
    }

    public int getYawAngle() {
        return yawAngle;
    }

    public int getRollRate() {
        return rollRate;
    }

    public int getPitchRate() {
        return pitchRate;
    }

    public int getYawRate() {
        return yawRate;
    }

    public int getRollAcceleration() {
        return rollAcceleration;
    }

    public int getPitchAcceleration() {
        return pitchAcceleration;
    }

    public int getYawAcceleration() {
        return yawAcceleration;
    }

    public int getPlanarObjectDimension1() {
        return planarObjectDimension1;
    }

    public int getPlanarObjectDimension2() {
        return planarObjectDimension2;
    }

    public int getVerticalObjectDimension() {
        return verticalObjectDimension;
    }

    public int getLongitudinalLanePosition() {
        return longitudinalLanePosition;
    }

    public int getObject() {
        return object;
    }

    public static PerceivedObjectConfidence jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            int xDistance = json.getInt(JsonCpmKey.ObjectDistance.X_DISTANCE.key());
            int yDistance = json.getInt(JsonCpmKey.ObjectDistance.Y_DISTANCE.key());
            int zDistance = json.optInt(JsonCpmKey.ObjectDistance.Z_DISTANCE.key(), UNKNOWN);
            int xSpeed = json.getInt(JsonCpmKey.ObjectSpeed.X_SPEED.key());
            int ySpeed = json.getInt(JsonCpmKey.ObjectSpeed.Y_SPEED.key());
            int zSpeed = json.optInt(JsonCpmKey.ObjectSpeed.Z_SPEED.key(), UNKNOWN);
            int xAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.X_ACCELERATION.key(), UNKNOWN);
            int yAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.Y_ACCELERATION.key(), UNKNOWN);
            int zAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.Z_ACCELERATION.key(), UNKNOWN);
            int rollAngle = json.optInt(JsonCpmKey.ObjectAngle.ROLL_ANGLE.key(), UNKNOWN);
            int pitchAngle = json.optInt(JsonCpmKey.ObjectAngle.PITCH_ANGLE.key(), UNKNOWN);
            int yawAngle = json.optInt(JsonCpmKey.ObjectAngle.YAW_ANGLE.key(), UNKNOWN);
            int rollRate = json.optInt(JsonCpmKey.ObjectSpeed.ROLL_RATE.key(), UNKNOWN);
            int pitchRate = json.optInt(JsonCpmKey.ObjectSpeed.PITCH_RATE.key(), UNKNOWN);
            int yawRate = json.optInt(JsonCpmKey.ObjectSpeed.YAW_RATE.key(), UNKNOWN);
            int rollAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.ROLL_ACCELERATION.key(), UNKNOWN);
            int pitchAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.PITCH_ACCELERATION.key(), UNKNOWN);
            int yawAcceleration = json.optInt(JsonCpmKey.ObjectAcceleration.YAW_ACCELERATION.key(), UNKNOWN);
            int planarObjectDimension1 = json.optInt(JsonCpmKey.ObjectDimension.PLANAR_DIMENSION_1.key(), UNKNOWN);
            int planarObjectDimension2 = json.optInt(JsonCpmKey.ObjectDimension.PLANAR_DIMENSION_2.key(), UNKNOWN);
            int verticalObjectDimension = json.optInt(JsonCpmKey.ObjectDimension.VERTICAL_DIMENSION.key(), UNKNOWN);
            int longitudinalLanePosition = json.optInt(JsonCpmKey.ObjectLanePosition.LONGITUDINAL_LANE_POSITION.key(), UNKNOWN);
            int object = json.getInt(JsonCpmKey.PerceivedObjectContainer.OBJECT.key());

            return new PerceivedObjectConfidence(
                    xDistance,
                    yDistance,
                    zDistance,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    xAcceleration,
                    yAcceleration,
                    zAcceleration,
                    rollAngle,
                    pitchAngle,
                    yawAngle,
                    rollRate,
                    pitchRate,
                    yawRate,
                    rollAcceleration,
                    pitchAcceleration,
                    yawAcceleration,
                    planarObjectDimension1,
                    planarObjectDimension2,
                    verticalObjectDimension,
                    longitudinalLanePosition,
                    object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
