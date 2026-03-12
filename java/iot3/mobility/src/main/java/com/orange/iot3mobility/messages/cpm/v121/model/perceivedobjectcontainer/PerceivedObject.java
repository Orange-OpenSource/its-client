package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Perceived object.
 *
 * @param objectId Identifier assigned to a detected object. Value: [0..255].
 * @param timeOfMeasurement Time difference from generation delta time. Unit: 1 millisecond. Value: [-1500..1500].
 * @param xDistance Distance to detected object in x-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param yDistance Distance to detected object in y-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param xSpeed Speed in x-direction. Unit: 0.01 m/s. Value: negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382), unavailable(16383).
 * @param ySpeed Speed in y-direction. Unit: 0.01 m/s. Value: negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382), unavailable(16383).
 * @param objectAge Age of the detected object. Unit: 1 ms. Value: oneMiliSec(1), moreThan1Point5Second(1500).
 * @param confidence {@link PerceivedObjectConfidence}
 * @param zDistance Distance to detected object in z-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param zSpeed Speed in z-direction. Unit: 0.01 m/s. Value: negativeSpeedMaximum(-16383), standstill(0), oneCentimeterPerSec(1), speedMaximum(16382), unavailable(16383).
 * @param xAcceleration Acceleration in x-direction. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
 * @param yAcceleration Acceleration in y-direction. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
 * @param zAcceleration Acceleration in z-direction. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquared(1), minusPointOneMeterPerSecSquared(-1), unavailable(161).
 * @param rollAngle Roll angle of object. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param pitchAngle Pitch angle of object. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param yawAngle Yaw angle of object. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param rollRate Roll rate of object. Unit: 0.01 degree/s. Value: noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
 * @param pitchRate Pitch rate of object. Unit: 0.01 degree/s. Value: noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
 * @param yawRate Yaw rate of object. Unit: 0.01 degree/s. Value: noSpeed(0), oneDegreePerSecondAntiClockwise(100), oneDegreePerSecondClockwise(-100).
 * @param rollAcceleration Roll acceleration of object. Unit: 0.01 degree/s^2. Value: noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100), oneDegreePerSecondSquaredClockwise(-100).
 * @param pitchAcceleration Pitch acceleration of object. Unit: 0.01 degree/s^2. Value: noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100), oneDegreePerSecondSquaredClockwise(-100).
 * @param yawAcceleration Yaw acceleration of object. Unit: 0.01 degree/s^2. Value: noAcceleration(0), oneDegreePerSecondSquaredAntiClockwise(100), oneDegreePerSecondSquaredClockwise(-100).
 * @param lowerTriangularCorrelationMatrixColumns Lower triangular correlation matrix columns. Value: [-100..100] scaled by 100.
 * @param planarObjectDimension1 First planar dimension. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param planarObjectDimension2 Second planar dimension. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param verticalObjectDimension Vertical dimension. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param objectRefPoint Object reference point. Value: mid(0), bottomLeft(1), midLeft(2), topLeft(3), bottomMid(4), topMid(5), bottomRight(6), midRight(7), topRight(8).
 * @param sensorIdList List of sensor IDs providing measurement data. Value: [0..255].
 * @param dynamicStatus Dynamic status of detected object. Value: dynamic(0), hasBeenDynamic(1), static(2).
 * @param classification {@link ObjectClassification}
 * @param matchedPosition Map-matched position {@link MapPosition}
 */
public record PerceivedObject(
        int objectId,
        int timeOfMeasurement,
        int xDistance,
        int yDistance,
        int xSpeed,
        int ySpeed,
        int objectAge,
        PerceivedObjectConfidence confidence,
        Integer zDistance,
        Integer zSpeed,
        Integer xAcceleration,
        Integer yAcceleration,
        Integer zAcceleration,
        Integer rollAngle,
        Integer pitchAngle,
        Integer yawAngle,
        Integer rollRate,
        Integer pitchRate,
        Integer yawRate,
        Integer rollAcceleration,
        Integer pitchAcceleration,
        Integer yawAcceleration,
        LowerTriangularCorrelationMatrix lowerTriangularCorrelationMatrixColumns,
        Integer planarObjectDimension1,
        Integer planarObjectDimension2,
        Integer verticalObjectDimension,
        Integer objectRefPoint,
        List<Integer> sensorIdList,
        Integer dynamicStatus,
        List<ObjectClassification> classification,
        MapPosition matchedPosition) {}
