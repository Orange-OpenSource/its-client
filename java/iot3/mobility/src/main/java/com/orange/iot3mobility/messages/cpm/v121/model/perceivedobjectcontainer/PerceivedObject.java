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
        MapPosition matchedPosition) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer objectId;
        private Integer timeOfMeasurement;
        private Integer xDistance;
        private Integer yDistance;
        private Integer xSpeed;
        private Integer ySpeed;
        private Integer objectAge;
        private PerceivedObjectConfidence confidence;
        private Integer zDistance;
        private Integer zSpeed;
        private Integer xAcceleration;
        private Integer yAcceleration;
        private Integer zAcceleration;
        private Integer rollAngle;
        private Integer pitchAngle;
        private Integer yawAngle;
        private Integer rollRate;
        private Integer pitchRate;
        private Integer yawRate;
        private Integer rollAcceleration;
        private Integer pitchAcceleration;
        private Integer yawAcceleration;
        private LowerTriangularCorrelationMatrix lowerTriangularCorrelationMatrixColumns;
        private Integer planarObjectDimension1;
        private Integer planarObjectDimension2;
        private Integer verticalObjectDimension;
        private Integer objectRefPoint;
        private List<Integer> sensorIdList;
        private Integer dynamicStatus;
        private List<ObjectClassification> classification;
        private MapPosition matchedPosition;

        public Builder objectId(int objectId) {
            this.objectId = objectId;
            return this;
        }

        public Builder timeOfMeasurement(int timeOfMeasurement) {
            this.timeOfMeasurement = timeOfMeasurement;
            return this;
        }

        public Builder distance(int xDistance, int yDistance) {
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            return this;
        }

        public Builder speed(int xSpeed, int ySpeed) {
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            return this;
        }

        public Builder objectAge(int objectAge) {
            this.objectAge = objectAge;
            return this;
        }

        public Builder confidence(PerceivedObjectConfidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder zDistance(Integer zDistance) {
            this.zDistance = zDistance;
            return this;
        }

        public Builder zSpeed(Integer zSpeed) {
            this.zSpeed = zSpeed;
            return this;
        }

        public Builder acceleration(Integer xAcceleration, Integer yAcceleration) {
            this.xAcceleration = xAcceleration;
            return this;
        }

        public Builder zAcceleration(Integer zAcceleration) {
            this.zAcceleration = zAcceleration;
            return this;
        }

        public Builder rollAngle(Integer rollAngle) {
            this.rollAngle = rollAngle;
            return this;
        }

        public Builder pitchAngle(Integer pitchAngle) {
            this.pitchAngle = pitchAngle;
            return this;
        }

        public Builder yawAngle(Integer yawAngle) {
            this.yawAngle = yawAngle;
            return this;
        }

        public Builder rollRate(Integer rollRate) {
            this.rollRate = rollRate;
            return this;
        }

        public Builder pitchRate(Integer pitchRate) {
            this.pitchRate = pitchRate;
            return this;
        }

        public Builder yawRate(Integer yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder rollAcceleration(Integer rollAcceleration) {
            this.rollAcceleration = rollAcceleration;
            return this;
        }

        public Builder pitchAcceleration(Integer pitchAcceleration) {
            this.pitchAcceleration = pitchAcceleration;
            return this;
        }

        public Builder yawAcceleration(Integer yawAcceleration) {
            this.yawAcceleration = yawAcceleration;
            return this;
        }

        public Builder lowerTriangularCorrelationMatrixColumns(LowerTriangularCorrelationMatrix columns) {
            this.lowerTriangularCorrelationMatrixColumns = columns;
            return this;
        }

        public Builder planarObjectDimension(Integer planarObjectDimension1, Integer planarObjectDimension2) {
            this.planarObjectDimension1 = planarObjectDimension1;
            this.planarObjectDimension2 = planarObjectDimension2;
            return this;
        }

        public Builder verticalObjectDimension(Integer verticalObjectDimension) {
            this.verticalObjectDimension = verticalObjectDimension;
            return this;
        }

        public Builder objectRefPoint(Integer objectRefPoint) {
            this.objectRefPoint = objectRefPoint;
            return this;
        }

        public Builder sensorIdList(List<Integer> sensorIdList) {
            this.sensorIdList = sensorIdList;
            return this;
        }

        public Builder dynamicStatus(Integer dynamicStatus) {
            this.dynamicStatus = dynamicStatus;
            return this;
        }

        public Builder classification(List<ObjectClassification> classification) {
            this.classification = classification;
            return this;
        }

        public Builder matchedPosition(MapPosition matchedPosition) {
            this.matchedPosition = matchedPosition;
            return this;
        }

        public PerceivedObject build() {
            return new PerceivedObject(
                    requireNonNull(objectId, "object_id"),
                    requireNonNull(timeOfMeasurement, "time_of_measurement"),
                    requireNonNull(xDistance, "x_distance"),
                    requireNonNull(yDistance, "y_distance"),
                    requireNonNull(xSpeed, "x_speed"),
                    requireNonNull(ySpeed, "y_speed"),
                    requireNonNull(objectAge, "object_age"),
                    requireNonNull(confidence, "confidence"),
                    zDistance,
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
                    lowerTriangularCorrelationMatrixColumns,
                    planarObjectDimension1,
                    planarObjectDimension2,
                    verticalObjectDimension,
                    objectRefPoint,
                    sensorIdList,
                    dynamicStatus,
                    classification,
                    matchedPosition);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
