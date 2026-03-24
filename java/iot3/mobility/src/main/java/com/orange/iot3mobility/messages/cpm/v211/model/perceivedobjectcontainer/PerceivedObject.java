/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianAngularVelocityComponent;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ObjectDimension;

import java.util.List;

/**
 * Perceived object
 * <p>
 * Information for an individual perceived object.
 *
 * @param measurementDeltaTime Time difference from a reference time to the time of the measurement of the object.
 *                             Unit: ms (-2048..2047).
 * @param position {@link CartesianPosition3dWithConfidence} of the geometric centre of the object's bounding box
 *                                                          within the pre-defined coordinate system.
 * @param objectId Optional. Identifier assigned to a detected object (0..65535).
 * @param velocity Optional. {@link Velocity} vector of the object within the pre-defined coordinate system.
 * @param acceleration Optional. {@link Acceleration} vector of the object within the pre-defined coordinate system.
 * @param angles Optional. {@link EulerAngles} of the object bounding box at the time of measurement.
 * @param zAngularVelocity Optional. {@link CartesianAngularVelocityComponent} around the z-axis at the time of
 *                         measurement.
 * @param lowerTriangularCorrelationMatrices Optional. List of {@link LowerTriangularCorrelationMatrix} entries.
 * @param objectDimensionZ Optional. {@link ObjectDimension} z-dimension of object bounding box.
 * @param objectDimensionY Optional. {@link ObjectDimension} y-dimension of object bounding box.
 * @param objectDimensionX Optional. {@link ObjectDimension} x-dimension of object bounding box.
 * @param objectAge Optional. Age of the detected object since first detection. Unit: ms (-2048..2047).
 * @param objectPerceptionQuality Optional. Overall perception quality (0..15). noConfidence (0), fullConfidence (15).
 * @param sensorIdList Optional. List of sensor-IDs which provided the measurement data (1..128 items).
 * @param classification Optional. List of {@link ObjectClassification} entries (1..8 items).
 * @param mapPosition Optional. {@link MapPosition}.
 */
public record PerceivedObject(
        int measurementDeltaTime,
        CartesianPosition3dWithConfidence position,
        Integer objectId,
        Velocity velocity,
        Acceleration acceleration,
        EulerAngles angles,
        CartesianAngularVelocityComponent zAngularVelocity,
        List<LowerTriangularCorrelationMatrix> lowerTriangularCorrelationMatrices,
        ObjectDimension objectDimensionZ,
        ObjectDimension objectDimensionY,
        ObjectDimension objectDimensionX,
        Integer objectAge,
        Integer objectPerceptionQuality,
        List<Integer> sensorIdList,
        List<ObjectClassification> classification,
        MapPosition mapPosition) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer measurementDeltaTime;
        private CartesianPosition3dWithConfidence position;
        private Integer objectId;
        private Velocity velocity;
        private Acceleration acceleration;
        private EulerAngles angles;
        private CartesianAngularVelocityComponent zAngularVelocity;
        private List<LowerTriangularCorrelationMatrix> lowerTriangularCorrelationMatrices;
        private ObjectDimension objectDimensionZ;
        private ObjectDimension objectDimensionY;
        private ObjectDimension objectDimensionX;
        private Integer objectAge;
        private Integer objectPerceptionQuality;
        private List<Integer> sensorIdList;
        private List<ObjectClassification> classification;
        private MapPosition mapPosition;

        public Builder measurementDeltaTime(int measurementDeltaTime) {
            this.measurementDeltaTime = measurementDeltaTime;
            return this;
        }

        public Builder position(CartesianPosition3dWithConfidence position) {
            this.position = position;
            return this;
        }

        public Builder objectId(Integer objectId) {
            this.objectId = objectId;
            return this;
        }

        public Builder velocity(Velocity velocity) {
            this.velocity = velocity;
            return this;
        }

        public Builder acceleration(Acceleration acceleration) {
            this.acceleration = acceleration;
            return this;
        }

        public Builder angles(EulerAngles angles) {
            this.angles = angles;
            return this;
        }

        public Builder zAngularVelocity(CartesianAngularVelocityComponent zAngularVelocity) {
            this.zAngularVelocity = zAngularVelocity;
            return this;
        }

        public Builder lowerTriangularCorrelationMatrices(List<LowerTriangularCorrelationMatrix> matrices) {
            this.lowerTriangularCorrelationMatrices = matrices;
            return this;
        }

        public Builder objectDimensionZ(ObjectDimension objectDimensionZ) {
            this.objectDimensionZ = objectDimensionZ;
            return this;
        }

        public Builder objectDimensionY(ObjectDimension objectDimensionY) {
            this.objectDimensionY = objectDimensionY;
            return this;
        }

        public Builder objectDimensionX(ObjectDimension objectDimensionX) {
            this.objectDimensionX = objectDimensionX;
            return this;
        }

        public Builder objectAge(Integer objectAge) {
            this.objectAge = objectAge;
            return this;
        }

        public Builder objectPerceptionQuality(Integer objectPerceptionQuality) {
            this.objectPerceptionQuality = objectPerceptionQuality;
            return this;
        }

        public Builder sensorIdList(List<Integer> sensorIdList) {
            this.sensorIdList = sensorIdList;
            return this;
        }

        public Builder classification(List<ObjectClassification> classification) {
            this.classification = classification;
            return this;
        }

        public Builder mapPosition(MapPosition mapPosition) {
            this.mapPosition = mapPosition;
            return this;
        }

        public PerceivedObject build() {
            return new PerceivedObject(
                    requireNonNull(measurementDeltaTime, "measurement_delta_time"),
                    requireNonNull(position, "position"),
                    objectId,
                    velocity,
                    acceleration,
                    angles,
                    zAngularVelocity,
                    lowerTriangularCorrelationMatrices,
                    objectDimensionZ,
                    objectDimensionY,
                    objectDimensionX,
                    objectAge,
                    objectPerceptionQuality,
                    sensorIdList,
                    classification,
                    mapPosition);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}

