package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianAngularVelocityComponent;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ObjectDimension;

import java.util.List;

/**
 * Information for an individual perceived object.
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

