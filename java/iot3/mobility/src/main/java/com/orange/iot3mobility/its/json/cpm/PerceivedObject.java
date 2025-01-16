/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerceivedObject {

    private static final Logger LOGGER = Logger.getLogger(PerceivedObject.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Identifier assigned to a detected object which remains constant as long as the object is
     * perceived.
     * <p>
     * Numbers are assigned in an increasing round-robin fashion. When the last identifier in the
     * allowed range has been used, the first counter for the identifier starts from the beginning
     * of the range again.
     * <p>
     * minimumValue(0), maximumValue(255).
     */
    private final int objectId;

    /**
     * Unit: 1 millisecond. Time difference from the message’s generation delta time to the time of
     * the measurement of the object.
     */
    private final int timeOfMeasurement;

    /**
     * Unit: 0.01 meter. Distance to detected object from the reference point in x-direction for
     * the time of measurement.
     * <p>
     * For a vehicle, the distance is reported in a body-fixed coordinate system as provided by
     * ISO 8855.
     * <p>
     * For a RSU, the distance is reported in a coordinate system in which the y-axis corresponds
     * to the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * zeroPointZeroOneMeter(1), oneMeter(100).
     */
    private final int xDistance;

    /**
     * Unit: 0.01 meter. Distance to detected object from the reference point in y-direction for
     * the time of measurement.
     * <p>
     * For a vehicle, the distance is reported in a body-fixed coordinate system as provided by
     * ISO 8855.
     * <p>
     * For a RSU, the distance is reported in a coordinate system in which the y-axis corresponds
     * to the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * zeroPointZeroOneMeter(1), oneMeter(100).
     */
    private final int yDistance;

    /**
     * Unit: 0.01 meter. Distance to detected object from the reference point in z-direction for
     * the time of measurement.
     * <p>
     * For a vehicle, the distance is reported in a body-fixed coordinate system as provided by
     * ISO 8855.
     * <p>
     * For a RSU, the distance is reported in a coordinate system in which the y-axis corresponds
     * to the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * zeroPointZeroOneMeter(1), oneMeter(100).
     */
    private final int zDistance;

    /**
     * Unit: 0.01 m/s. Speed of the detected object in the detecting reference system in x-direction
     * for the time of measurement (i.e. speed of the object relative to the origin of the station’s
     * reference system).
     * <p>
     * For a vehicle, the speed is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the speed is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382),
     * unavailable(16383).
     */
    private final int xSpeed;

    /**
     * Unit: 0.01 m/s. Speed of the detected object in the detecting reference system in y-direction
     * for the time of measurement (i.e. speed of the object relative to the origin of the station’s
     * reference system).
     * <p>
     * For a vehicle, the speed is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the speed is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382),
     * unavailable(16383).
     */
    private final int ySpeed;

    /**
     * Unit: 0.01 m/s. Speed of the detected object in the detecting reference system in z-direction
     * for the time of measurement (i.e. speed of the object relative to the origin of the station’s
     * reference system).
     * <p>
     * For a vehicle, the speed is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the speed is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z-axis to the vertical
     * direction.
     * <p>
     * negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382),
     * unavailable(16383).
     */
    private final int zSpeed;

    /**
     * Unit: 0.1 m/s2. Acceleration of the detected object from the reference point in x-direction
     * for the time of measurement.
     * <p>
     * For a vehicle, the acceleration is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z-axis to the
     * vertical direct.
     * <p>
     * pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
     */
    private final int xAcceleration;

    /**
     * Unit: 0.1 m/s2. Acceleration of the detected object from the reference point in y-direction
     * for the time of measurement.
     * <p>
     * For a vehicle, the acceleration is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z-axis to the
     * vertical direct.
     * <p>
     * pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
     */
    private final int yAcceleration;

    /**
     * Unit: 0.1 m/s2. Acceleration of the detected object from the reference point in z-direction
     * for the time of measurement.
     * <p>
     * For a vehicle, the acceleration is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z-axis to
     * the vertical direct.
     * <p>
     * pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
     */
    private final int zAcceleration;

    /**
     * Unit: 0.1 degrees. Roll angle of object from the reference point.
     * <p>
     * For a vehicle, the angle is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angle is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z- axis to the vertical
     * direction.
     * <p>
     * The angle is measured with positive values considering the object orientation turning
     * counter-clockwise around the x-axis.
     * <p>
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int rollAngle;

    /**
     * Unit: 0.1 degrees. Pitch angle of object from the reference point.
     * <p>
     * For a vehicle, the angle is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angle is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z- axis to the vertical
     * direction.
     * <p>
     * The angle is measured with positive values considering the object orientation turning
     * counter-clockwise around the y-axis.
     * <p>
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int pitchAngle;

    /**
     * Unit: 0.1 degrees. Yaw angle of object from the reference point.
     * <p>
     * For a vehicle, the angle is reported in a body-fixed coordinate system as provided by
     * ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angle is reported in a coordinate system in which the y-axis corresponds to
     * the North direction, the x-axis to the East direction, and the z- axis to the vertical
     * direction.
     * <p>
     * The angle is measured with positive values considering the object orientation turning
     * counter-clockwise around the z-axis.
     * <p>
     * zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
     */
    private final int yawAngle;

    /**
     * Unit: 0.01 degrees/s. Roll rate of object from the reference point.
     * <p>
     * For a vehicle, the angular rate is reported in a body-fixed coordinate system as provided
     * by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular rate is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to
     * the vertical direction.
     * <p>
     * The angular rate is measured with positive values considering the object orientation
     * turning counter-clockwise around the x-axis. An angular speed value described in a local
     * Cartesian coordinate system, counted positive in a right-hand local coordinate system from
     * the abscissa.
     * <p>
     * noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
     */
    private final int rollRate;

    /**
     * Unit: 0.01 degrees/s. Pitch rate of object from the reference point.
     * <p>
     * For a vehicle, the angular rate is reported in a body-fixed coordinate system as provided
     * by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular rate is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to
     * the vertical direction.
     * <p>
     * The angular rate is measured with positive values considering the object orientation turning
     * counter-clockwise around the x-axis. An angular speed value described in a local Cartesian
     * coordinate system, counted positive in a right-hand local coordinate system from the
     * abscissa.
     * <p>
     * noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
     */
    private final int pitchRate;

    /**
     * Unit: 0.01 degrees/s. Yaw rate of object from the reference point.
     * <p>
     * For a vehicle, the angular rate is reported in a body-fixed coordinate system as provided
     * by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular rate is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to
     * the vertical direction.
     * <p>
     * The angular rate is measured with positive values considering the object orientation turning
     * counter-clockwise around the x-axis. An angular speed value described in a local Cartesian
     * coordinate system, counted positive in a right-hand local coordinate system from the
     * abscissa.
     * <p>
     * noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
     */
    private final int yawRate;

    /**
     * Unit: 0.01 degrees/s^2 (degrees per second squared).
     * Roll acceleration of object from the reference point.
     * <p>
     * For a vehicle, the angular acceleration is reported in a body-fixed coordinate system as
     * provided by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to
     * the vertical direction.
     * <p>
     * The angular acceleration is measured with positive values considering the object orientation
     * turning counter-clockwise around the x-axis. An angular acceleration value described in a
     * local Cartesian coordinate system, counted positive in a right-hand local coordinate system
     * from the abscissa.
     * <p>
     * noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100),
     * oneDegreePerSecondSquaredClockwise(-100).
     */
    private final int rollAcceleration;

    /**
     * Unit: 0.01 degrees/s^2 (degrees per second squared).
     * Pitch acceleration of object from the reference point.
     * <p>
     * For a vehicle, the angular acceleration is reported in a body-fixed coordinate system as
     * provided by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to the
     * vertical direction.
     * <p>
     * The angular acceleration is measured with positive values considering the object orientation
     * turning counter-clockwise around the x-axis. An angular acceleration value described in a
     * local Cartesian coordinate system, counted positive in a right-hand local coordinate system
     * from the abscissa.
     * <p>
     * noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100),
     * oneDegreePerSecondSquaredClockwise(-100).
     */
    private final int pitchAcceleration;

    /**
     * Unit: 0.01 degrees/s^2 (degrees per second squared).
     * Yaw acceleration of object from the reference point.
     * <p>
     * For a vehicle, the angular acceleration is reported in a body-fixed coordinate system as
     * provided by ISO 8855 originating at the station’s reference point.
     * <p>
     * For a RSU, the angular acceleration is reported in a coordinate system in which the y-axis
     * corresponds to the North direction, the x-axis to the East direction, and the z- axis to the
     * vertical direction.
     * <p>
     * The angular acceleration is measured with positive values considering the object orientation
     * turning counter-clockwise around the x-axis. An angular acceleration value described in a
     * local Cartesian coordinate system, counted positive in a right-hand local coordinate system
     * from the abscissa.
     * <p>
     * noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100),
     * oneDegreePerSecondSquaredClockwise(-100).
     */
    private final int yawAcceleration;

    //lowerTriangularCorrelationMatrixColumns not implemented

    /**
     * Unit: 0.1 m. First dimension of object as provided by the sensor or environment model.
     * <p>
     * This dimension is always contained in the plane which is oriented perpendicular to the
     * direction of the angle indicated by the yawAngle and which contains the object's reference
     * point. A dimension for an object.
     * <p>
     * zeroPointOneMeter(1), oneMeter(10).",
     */
    private final int planarObjectDimension1;

    /**
     * Unit: 0.1 m. Second dimension of the object as provided by the sensor environment model.
     * <p>
     * This dimension is contained in the plane oriented in the direction of the angle indicated
     * by the yawAngle and the object's reference point. A dimension for an object.
     * <p>
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int planarObjectDimension2;

    /**
     * Unit: 0.1 m. Vertical dimension of object as provided by the sensor or object model.
     * <p>
     * A dimension for an object.
     * <p>
     * zeroPointOneMeter(1), oneMeter(10).
     */
    private final int verticalObjectDimension;

    /**
     * The reference point on the perceived object.
     * <p>
     * The kinematic attitude and state data provided for this object are valid for this reference
     * point of the object. In case no object reference point can be determined, it is assumed to
     * be the center point of the detected object.
     * <p>
     * {mid(0), bottomLeft(1), midLeft(2), topLeft(3), bottomMid(4), topMid(5), bottomRight(6),
     * midRight(7), topRight(8).
     */
    private final int objectRefPoint;

    /**
     * Unit: 1 ms. Provides the age of the detected and described object.
     * <p>
     * Age of object in milliseconds, i.e. for how long the object has been observed by the
     * disseminating station.
     * <p>
     * oneMiliSec(1), moreThan1Point5Second(1500).
     */
    private final int objectAge;

    /**
     * List of sensor-IDs which provided the measurement data.
     */
    private final List<Integer> sensorIdList;

    /**
     * Indicated the dynamic capabilities of a detected object.
     * <p>
     * Indication whether the detected object is classified as a dynamic (i.e. moving) object.
     * This value indicates whether an object has the general capability to move, i.e. change
     * its position.
     * <p>
     * dynamic(0) the object is moving, hasBeenDynamic(1) indicates whether an object
     * has been dynamic before, e.g., a car stopping at a traffic light, static(2) shall be used
     * in case an object is identified to be not moving throughout any previous observation
     */
    private final int dynamicStatus;

    /**
     * Provides the classification of the described object.
     * Multi-dimensional classification may be provided.
     */
    private final List<ClassificationItem> classification;

    //matchedPosition not implemented

    /**
     * The confidence associated with each field of the perceived object
      */
    private final PerceivedObjectConfidence confidence;

    /**
     * Object which is perceived by one or several sensors.
     */
    private PerceivedObject(
            final int objectId,
            final int timeOfMeasurement,
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
            final int objectRefPoint,
            final int objectAge,
            final List<Integer> sensorIdList,
            final int dynamicStatus,
            final List<ClassificationItem> classification,
            final PerceivedObjectConfidence confidence
    ) throws IllegalArgumentException {
        if(objectId == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject objectId is missing");
        } else if(CPM.isStrictMode() && (objectId > 255 || objectId < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject objectId should be in the range of [0 - 255]."
                    + " Value: " + objectId);
        }
        this.objectId = objectId;
        if(timeOfMeasurement == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject timeOfMeasurement is missing");
        } /*else if(CPM.isStrictMode() && (timeOfMeasurement > 1500 || timeOfMeasurement < -1500)) {
            throw new IllegalArgumentException("CPM PerceivedObject timeOfMeasurement should be in the range of [-1500 - 1500]."
                    + " Value: " + timeOfMeasurement);
        }*/
        this.timeOfMeasurement = timeOfMeasurement;
        //********* DISTANCE ***********
        if(xDistance == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject xDistance is missing");
        } else if(CPM.isStrictMode() && (xDistance > 132767 || xDistance < -132768)) {
            throw new IllegalArgumentException("CPM PerceivedObject xDistance should be in the range of [-132768 - 132767]."
                    + " Value: " + xDistance);
        }
        this.xDistance = xDistance;
        if(yDistance == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject yDistance is missing");
        } else if(CPM.isStrictMode() && (yDistance > 132767 || yDistance < -132768)) {
            throw new IllegalArgumentException("CPM PerceivedObject yDistance should be in the range of [-132768 - 132767]."
                    + " Value: " + yDistance);
        }
        this.yDistance = yDistance;
        if(zDistance != UNKNOWN && CPM.isStrictMode()
                && (zDistance > 132767 || zDistance < -132768)) {
            throw new IllegalArgumentException("CPM PerceivedObject zDistance should be in the range of [-132768 - 132767]."
                    + " Value: " + zDistance);
        }
        this.zDistance = zDistance;
        //********* SPEED ***********
        if(xSpeed == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject xSpeed is missing");
        } else if(CPM.isStrictMode() && (xSpeed > 16383 || xSpeed < -16383)) {
            throw new IllegalArgumentException("CPM PerceivedObject xSpeed should be in the range of [-16383 - 16383]."
                    + " Value: " + xSpeed);
        }
        this.xSpeed = xSpeed;
        if(ySpeed == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject ySpeed is missing");
        } else if(CPM.isStrictMode() && (ySpeed > 16383 || ySpeed < -16383)) {
            throw new IllegalArgumentException("CPM PerceivedObject ySpeed should be in the range of [-16383 - 16383]."
                    + " Value: " + ySpeed);
        }
        this.ySpeed = ySpeed;
        if(zSpeed != UNKNOWN && CPM.isStrictMode()
                && (zSpeed > 16383 || zSpeed < -16383)) {
            throw new IllegalArgumentException("CPM PerceivedObject zSpeed should be in the range of [-16383 - 16383]."
                    + " Value: " + zSpeed);
        }
        this.zSpeed = zSpeed;
        //********* ACCELERATION ***********
        if(xAcceleration != UNKNOWN && CPM.isStrictMode()
                && (xAcceleration > 161 || xAcceleration < -160)) {
            throw new IllegalArgumentException("CPM PerceivedObject xAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + xAcceleration);
        }
        this.xAcceleration = xAcceleration;
        if(yAcceleration != UNKNOWN && CPM.isStrictMode()
                && (yAcceleration > 161 || yAcceleration < -160)) {
            throw new IllegalArgumentException("CPM PerceivedObject yAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + yAcceleration);
        }
        this.yAcceleration = yAcceleration;
        if(zAcceleration != UNKNOWN && CPM.isStrictMode()
                && (zAcceleration > 161 || zAcceleration < -160)) {
            throw new IllegalArgumentException("CPM PerceivedObject zAcceleration should be in the range of [-160 - 161]."
                    + " Value: " + zAcceleration);
        }
        this.zAcceleration = zAcceleration;
        //********* ROTATION ***********
        if(rollAngle != UNKNOWN && CPM.isStrictMode()
                && (rollAngle > 3601 || rollAngle < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject rollAngle should be in the range of [0 - 3601]."
                    + " Value: " + rollAngle);
        }
        this.rollAngle = rollAngle;
        if(pitchAngle != UNKNOWN && CPM.isStrictMode()
                && (pitchAngle > 3601 || pitchAngle < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject pitchAngle should be in the range of [0 - 3601]."
                    + " Value: " + pitchAngle);
        }
        this.pitchAngle = pitchAngle;
        if(yawAngle != UNKNOWN && CPM.isStrictMode()
                && (yawAngle > 3601 || yawAngle < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject yawAngle should be in the range of [0 - 3601]."
                    + " Value: " + yawAngle);
        }
        this.yawAngle = yawAngle;
        //********* ROTATION RATE ***********
        if(rollRate != UNKNOWN && CPM.isStrictMode()
                && (rollRate > 32767 || rollRate < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject rollRate should be in the range of [-32766 - 32767]."
                    + " Value: " + rollRate);
        }
        this.rollRate = rollRate;
        if(pitchRate != UNKNOWN && CPM.isStrictMode()
                && (pitchRate > 32767 || pitchRate < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject pitchRate should be in the range of [-32766 - 32767]."
                    + " Value: " + pitchRate);
        }
        this.pitchRate = pitchRate;
        if(yawRate != UNKNOWN && CPM.isStrictMode()
                && (yawRate > 32767 || yawRate < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject yawRate should be in the range of [-32766 - 32767]."
                    + " Value: " + yawRate);
        }
        this.yawRate = yawRate;
        //********* ROTATION ACCELERATION ***********
        if(rollAcceleration != UNKNOWN && CPM.isStrictMode()
                && (rollAcceleration > 32767 || rollAcceleration < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject rollAcceleration should be in the range of [-32766 - 32767]."
                    + " Value: " + rollAcceleration);
        }
        this.rollAcceleration = rollAcceleration;
        if(pitchAcceleration != UNKNOWN && CPM.isStrictMode()
                && (pitchAcceleration > 32767 || pitchAcceleration < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject pitchAcceleration should be in the range of [-32766 - 32767]."
                    + " Value: " + pitchAcceleration);
        }
        this.pitchAcceleration = pitchAcceleration;
        if(yawAcceleration != UNKNOWN && CPM.isStrictMode()
                && (yawAcceleration > 32767 || yawAcceleration < -32766)) {
            throw new IllegalArgumentException("CPM PerceivedObject yawAcceleration should be in the range of [-32766 - 32767]."
                    + " Value: " + yawAcceleration);
        }
        this.yawAcceleration = yawAcceleration;
        //********* OBJECT DIMENSION ***********
        if(planarObjectDimension1 != UNKNOWN && CPM.isStrictMode()
                && (planarObjectDimension1 > 1023 || planarObjectDimension1 < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject planarObjectDimension1 should be in the range of [0 - 1023]."
                    + " Value: " + planarObjectDimension1);
        }
        this.planarObjectDimension1 = planarObjectDimension1;
        if(planarObjectDimension2 != UNKNOWN && CPM.isStrictMode()
                && (planarObjectDimension2 > 1023 || planarObjectDimension2 < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject planarObjectDimension2 should be in the range of [0 - 1023]."
                    + " Value: " + planarObjectDimension2);
        }
        this.planarObjectDimension2 = planarObjectDimension2;
        if(verticalObjectDimension != UNKNOWN && CPM.isStrictMode()
                && (verticalObjectDimension > 1023 || verticalObjectDimension < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject verticalObjectDimension should be in the range of [0 - 1023]."
                    + " Value: " + verticalObjectDimension);
        }
        this.verticalObjectDimension = verticalObjectDimension;
        if(objectRefPoint != UNKNOWN && CPM.isStrictMode()
                && (objectRefPoint > 8 || objectRefPoint < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject objectRefPoint should be in the range of [0 - 8]."
                    + " Value: " + objectRefPoint);
        }
        this.objectRefPoint = objectRefPoint;
        if(objectAge == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject objectAge is missing");
        } else if(CPM.isStrictMode() && (objectAge > 1500 || objectAge < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject objectAge should be in the range of [0 - 1500]."
                    + " Value: " + objectAge);
        }
        this.objectAge = objectAge;
        this.sensorIdList = sensorIdList;
        if(dynamicStatus != UNKNOWN && CPM.isStrictMode()
                && (dynamicStatus > 2 || dynamicStatus < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject dynamicStatus should be in the range of [0 - 2]."
                    + " Value: " + dynamicStatus);
        }
        this.dynamicStatus = dynamicStatus;
        this.classification = classification;
        if(confidence == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject confidence is missing");
        }
        this.confidence = confidence;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.PerceivedObjectContainer.OBJECT_ID.key(), objectId);
            json.put(JsonCpmKey.PerceivedObjectContainer.TIME_OF_MEASUREMENT.key(), timeOfMeasurement);
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
            if(objectRefPoint != UNKNOWN)
                json.put(JsonCpmKey.PerceivedObjectContainer.OBJECT_REF_POINT.key(), objectRefPoint);
            json.put(JsonCpmKey.PerceivedObjectContainer.OBJECT_AGE.key(), objectAge);
            if(sensorIdList != null && !sensorIdList.isEmpty()) {
                JSONArray jsonSensorIdList = new JSONArray();
                for(int sensorId: sensorIdList) {
                    jsonSensorIdList.put(sensorId);
                }
                json.put(JsonCpmKey.PerceivedObjectContainer.SENSOR_ID_LIST.key(), jsonSensorIdList);
            }
            if(dynamicStatus != UNKNOWN)
                json.put(JsonCpmKey.PerceivedObjectContainer.DYNAMIC_STATUS.key(), dynamicStatus);
            if(classification != null && !classification.isEmpty()) {
                JSONArray jsonClassification = new JSONArray();
                for(ClassificationItem classificationItem: classification) {
                    if(classificationItem != null) jsonClassification.put(classificationItem.getJson());
                }
                if(!JsonUtil.isNullOrEmpty(jsonClassification))
                    json.put(JsonCpmKey.PerceivedObjectContainer.CLASSIFICATION.key(), jsonClassification);
            }
            json.put(JsonCpmKey.PerceivedObjectContainer.CONFIDENCE.key(), confidence.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM PerceivedObject JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getTimeOfMeasurement() {
        return timeOfMeasurement;
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
        return (float) Math.sqrt(Math.abs(xSpeed * xSpeed) + Math.abs(ySpeed * ySpeed)) / 100f;
    }

    public float getHeadingDegree() {
        return (float) (90 - (180 / Math.PI) * Math.atan2(ySpeed, xSpeed)) % 360;
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

    public boolean hasPlanarObjectDimensions() {
        return planarObjectDimension1 != UNKNOWN && planarObjectDimension2 != UNKNOWN;
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

    public int getObjectRefPoint() {
        return objectRefPoint;
    }

    public int getObjectAge() {
        return objectAge;
    }

    public List<Integer> getSensorIdList() {
        return sensorIdList;
    }

    public int getDynamicStatus() {
        return dynamicStatus;
    }

    public List<ClassificationItem> getClassification() {
        return classification;
    }

    public PerceivedObjectConfidence getConfidence() {
        return confidence;
    }

    public static class PerceivedObjectBuilder {
        private final int objectId;
        private final int timeOfMeasurement;
        private int xDistance;
        private int yDistance;
        private int zDistance = UNKNOWN;
        private int xSpeed;
        private int ySpeed;
        private int zSpeed = UNKNOWN;
        private int xAcceleration = UNKNOWN;
        private int yAcceleration = UNKNOWN;
        private int zAcceleration = UNKNOWN;
        private int rollAngle = UNKNOWN;
        private int pitchAngle = UNKNOWN;
        private int yawAngle = UNKNOWN;
        private int rollRate = UNKNOWN;
        private int pitchRate = UNKNOWN;
        private int yawRate = UNKNOWN;
        private int rollAcceleration = UNKNOWN;
        private int pitchAcceleration = UNKNOWN;
        private int yawAcceleration = UNKNOWN;
        private int planarObjectDimension1 = UNKNOWN;
        private int planarObjectDimension2 = UNKNOWN;
        private int verticalObjectDimension = UNKNOWN;
        private int objectRefPoint = UNKNOWN;
        private final int objectAge;
        private List<Integer> sensorIdList;
        private int dynamicStatus = UNKNOWN;
        private List<ClassificationItem> classification;
        private PerceivedObjectConfidence confidence;

        /**
         * Start building a PerceivedObject.
         *
         * @param objectId {@link PerceivedObject#objectId}
         * @param timeOfMeasurement {@link PerceivedObject#timeOfMeasurement}
         * @param objectAge {@link PerceivedObject#objectAge}
         */
        public PerceivedObjectBuilder(int objectId,
                                      int timeOfMeasurement,
                                      int objectAge) {
            this.objectId = objectId;
            this.timeOfMeasurement = timeOfMeasurement;
            this.objectAge = objectAge;
        }

        /**
         * Sets the distance between the sensor and the perceived object.
         * <p>
         * These fields are mandatory.
         *
         * @param xDistance {@link PerceivedObject#xDistance}
         * @param yDistance {@link PerceivedObject#yDistance}
         */
        public PerceivedObjectBuilder distance(int xDistance,
                                               int yDistance) {
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            return this;
        }

        /**
         * Sets the distance between the sensor and the perceived object.
         * <p>
         * These fields are mandatory, except zDistance - use {@link #distance(int, int)} if not known.
         *
         * @param xDistance {@link PerceivedObject#xDistance}
         * @param yDistance {@link PerceivedObject#yDistance}
         * @param zDistance {@link PerceivedObject#zDistance}
         */
        public PerceivedObjectBuilder distance(int xDistance,
                                                  int yDistance,
                                                  int zDistance) {
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            this.zDistance = zDistance;
            return this;
        }

        /**
         * Sets the speed of the perceived object.
         * <p>
         * These fields are mandatory.
         *
         * @param xSpeed {@link PerceivedObject#xSpeed}
         * @param ySpeed {@link PerceivedObject#ySpeed}
         */
        public PerceivedObjectBuilder speed(int xSpeed,
                                            int ySpeed) {
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            return this;
        }

        /**
         * Sets the speed of the perceived object.
         * <p>
         * These fields are mandatory, except zSpeed - use {@link #speed(int, int)} if not known.
         *
         * @param xSpeed {@link PerceivedObject#xSpeed}
         * @param ySpeed {@link PerceivedObject#ySpeed}
         * @param zSpeed {@link PerceivedObject#zSpeed}
         */
        public PerceivedObjectBuilder speed(int xSpeed,
                                               int ySpeed,
                                               int zSpeed) {
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            this.zSpeed = zSpeed;
            return this;
        }

        /**
         * Sets the acceleration of the perceived object.
         * <p>
         * These fields are optional.
         *
         * @param xAcceleration {@link PerceivedObject#xAcceleration}
         * @param yAcceleration {@link PerceivedObject#yAcceleration}
         * @param zAcceleration {@link PerceivedObject#zAcceleration}
         */
        public PerceivedObjectBuilder acceleration(int xAcceleration,
                                                   int yAcceleration,
                                                   int zAcceleration) {
            this.xAcceleration = xAcceleration;
            this.yAcceleration = yAcceleration;
            this.zAcceleration = zAcceleration;
            return this;
        }

        /**
         * Sets the angle of the perceived object.
         * <p>
         * These fields are optional.
         *
         * @param rollAngle {@link PerceivedObject#rollAngle}
         * @param pitchAngle {@link PerceivedObject#pitchAngle}
         * @param yawAngle {@link PerceivedObject#yawAngle}
         */
        public PerceivedObjectBuilder angle(int rollAngle,
                                            int pitchAngle,
                                            int yawAngle) {
            this.rollAngle = rollAngle;
            this.pitchAngle = pitchAngle;
            this.yawAngle = yawAngle;
            return this;
        }

        /**
         * Sets the angular rate of the perceived object.
         * <p>
         * These fields are optional.
         *
         * @param rollRate {@link PerceivedObject#rollRate}
         * @param pitchRate {@link PerceivedObject#pitchRate}
         * @param yawRate {@link PerceivedObject#yawRate}
         */
        public PerceivedObjectBuilder angleRate(int rollRate,
                                                int pitchRate,
                                                int yawRate) {
            this.rollRate = rollRate;
            this.pitchRate = pitchRate;
            this.yawRate = yawRate;
            return this;
        }

        /**
         * Sets the angular acceleration of the perceived object.
         * <p>
         * These fields are optional.
         *
         * @param rollAcceleration {@link PerceivedObject#rollAcceleration}
         * @param pitchAcceleration {@link PerceivedObject#pitchAcceleration}
         * @param yawAcceleration {@link PerceivedObject#yawAcceleration}
         */
        public PerceivedObjectBuilder angleAcceleration(int rollAcceleration,
                                                        int pitchAcceleration,
                                                        int yawAcceleration) {
            this.rollAcceleration = rollAcceleration;
            this.pitchAcceleration = pitchAcceleration;
            this.yawAcceleration = yawAcceleration;
            return this;
        }

        /**
         * Sets the dimensions of the perceived object.
         * <p>
         * These fields are optional.
         *
         * @param planarObjectDimension1 {@link PerceivedObject#planarObjectDimension1}
         * @param planarObjectDimension2 {@link PerceivedObject#planarObjectDimension2}
         * @param verticalObjectDimension {@link PerceivedObject#verticalObjectDimension}
         * @param objectRefPoint {@link PerceivedObject#objectRefPoint}
         */
        public PerceivedObjectBuilder objectDimension(int planarObjectDimension1,
                                                      int planarObjectDimension2,
                                                      int verticalObjectDimension,
                                                      int objectRefPoint) {
            this.planarObjectDimension1 = planarObjectDimension1;
            this.planarObjectDimension2 = planarObjectDimension2;
            this.verticalObjectDimension = verticalObjectDimension;
            this.objectRefPoint = objectRefPoint;
            return this;
        }

        /**
         * Sets the list of sensors which have detected the perceived object.
         * <p>
         * This list is optional.
         *
         * @param sensorIdList {@link PerceivedObject#sensorIdList}
         */
        public PerceivedObjectBuilder sensorIdList(List<Integer> sensorIdList) {
            this.sensorIdList = sensorIdList;
            return this;
        }

        /**
         * Sets the dynamic status of the perceived object.
         * <p>
         * This field is optional.
         *
         * @param dynamicStatus {@link PerceivedObject#dynamicStatus}
         */
        public PerceivedObjectBuilder dynamicStatus(int dynamicStatus) {
            this.dynamicStatus = dynamicStatus;
            return this;
        }

        /**
         * Sets the list of classification of the perceived object.
         * <p>
         * This list is optional.
         *
         * @param classification {@link PerceivedObject#classification}
         */
        public PerceivedObjectBuilder classification(List<ClassificationItem> classification) {
            this.classification = classification;
            return this;
        }

        /**
         * Sets the confidence of the perceived object.
         * <p>
         * This field is mandatory.
         *
         * @param confidence {@link PerceivedObject#confidence}
         */
        public PerceivedObjectBuilder confidence(PerceivedObjectConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        /**
         * Build the perceived object.
         * <p>
         * Call after setting all the mandatory fields.
         *
         * @return {@link #PerceivedObject}
         */
        public PerceivedObject build() {
            return new PerceivedObject(
                    objectId,
                    timeOfMeasurement,
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
                    objectRefPoint,
                    objectAge,
                    sensorIdList,
                    dynamicStatus,
                    classification,
                    confidence);
        }

    }

    public static PerceivedObject jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        try {
            int objectId = json.getInt(JsonCpmKey.PerceivedObjectContainer.OBJECT_ID.key());
            int timeOfMeasurement = json.getInt(JsonCpmKey.PerceivedObjectContainer.TIME_OF_MEASUREMENT.key());
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
            int objectRefPoint = json.optInt(JsonCpmKey.PerceivedObjectContainer.OBJECT_REF_POINT.key(), UNKNOWN);
            int objectAge = json.getInt(JsonCpmKey.PerceivedObjectContainer.OBJECT_AGE.key());
            JSONArray jsonSensorIdList = json.optJSONArray(JsonCpmKey.PerceivedObjectContainer.SENSOR_ID_LIST.key());
            ArrayList<Integer> sensorIdList = null;
            if(jsonSensorIdList != null) {
                sensorIdList = new ArrayList<>();
                for(int i = 0; i < jsonSensorIdList.length(); i++) {
                    sensorIdList.add(jsonSensorIdList.getInt(i));
                }
            }
            int dynamicStatus = json.optInt(JsonCpmKey.PerceivedObjectContainer.DYNAMIC_STATUS.key(), UNKNOWN);
            JSONArray jsonClassification = json.optJSONArray(JsonCpmKey.PerceivedObjectContainer.CLASSIFICATION.key());
            ArrayList<ClassificationItem> classification = null;
            if(jsonClassification != null) {
                classification = new ArrayList<>();
                for(int i = 0; i < jsonClassification.length(); i++) {
                    classification.add(ClassificationItem.jsonParser(jsonClassification.getJSONObject(i)));
                }
            }
            JSONObject jsonConfidence = json.getJSONObject(JsonCpmKey.PerceivedObjectContainer.CONFIDENCE.key());
            PerceivedObjectConfidence confidence = PerceivedObjectConfidence.jsonParser(jsonConfidence);

            return new PerceivedObjectBuilder(
                    objectId,
                    timeOfMeasurement,
                    objectAge)
                    .distance(
                            xDistance,
                            yDistance,
                            zDistance)
                    .speed(
                            xSpeed,
                            ySpeed,
                            zSpeed)
                    .acceleration(
                            xAcceleration,
                            yAcceleration,
                            zAcceleration)
                    .angle(
                            rollAngle,
                            pitchAngle,
                            yawAngle)
                    .angleRate(
                            rollRate,
                            pitchRate,
                            yawRate)
                    .angleAcceleration(
                            rollAcceleration,
                            pitchAcceleration,
                            yawAcceleration)
                    .objectDimension(
                            planarObjectDimension1,
                            planarObjectDimension2,
                            verticalObjectDimension,
                            objectRefPoint)
                    .sensorIdList(sensorIdList)
                    .dynamicStatus(dynamicStatus)
                    .classification(classification)
                    .confidence(confidence)
                    .build();
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM PerceivedObject JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
