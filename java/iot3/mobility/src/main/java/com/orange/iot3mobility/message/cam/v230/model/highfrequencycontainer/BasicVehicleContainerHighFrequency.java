/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * BasicVehicleContainerHighFrequency v2.3.0
 *
 * @param heading {@link Heading}
 * @param speed {@link Speed}
 * @param driveDirection Vehicle drive direction (forward or backward) of the originating ITS-S. forward (0),
 *                       backward (1), unavailable (2)
 * @param vehicleLength {@link VehicleLength}
 * @param vehicleWidth Vehicle Width of the vehicle ITS-S that originates the CAM excluding side mirrors and possible
 *                     similar extensions. outOfRange  (61), unavailable (62)
 * @param longitudinalAcceleration Longitudinal {@link AccelerationComponent}
 * @param curvature {@link Curvature}
 * @param curvatureCalculationMode Whether vehicle yaw-rate is used in the calculation of the curvature of the vehicle
 *                                 ITS-S that originates the CAM. yawRateUsed (0), yawRateNotUsed (1), unavailable (2)
 * @param yawRate {@link YawRate}
 * @param accelerationControl Optional {@link AccelerationControl}
 * @param lanePosition Optional component which represents the lanePosition of the referencePosition of a vehicle.
 *                     offTheRoad (-1), innerHardShoulder (0), outerHardShoulder (14)
 * @param steeringWheelAngle Optional {@link SteeringWheelAngle}
 * @param lateralAcceleration Optional lateral {@link AccelerationComponent}
 * @param verticalAcceleration Optional vertical {@link AccelerationComponent}
 * @param performanceClass Optional component which characterizes the maximum age of the CAM data elements with regard
 *                         to the generation delta time. unavailable (0), performanceClassA (1), performanceClassB (2)
 * @param cenDsrcTollingZone Optional {@link CenDsrcTollingZone}
 */
public record BasicVehicleContainerHighFrequency(
        Heading heading,
        Speed speed,
        int driveDirection,
        VehicleLength vehicleLength,
        int vehicleWidth,
        AccelerationComponent longitudinalAcceleration,
        Curvature curvature,
        int curvatureCalculationMode,
        YawRate yawRate,
        AccelerationControl accelerationControl,
        Integer lanePosition,
        SteeringWheelAngle steeringWheelAngle,
        AccelerationComponent lateralAcceleration,
        AccelerationComponent verticalAcceleration,
        Integer performanceClass,
        CenDsrcTollingZone cenDsrcTollingZone) implements HighFrequencyContainer {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for BasicVehicleContainerHighFrequency.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>heading</li>
     * <li>speed</li>
     * <li>driveDirection</li>
     * <li>vehicleLength</li>
     * <li>vehicleWidth</li>
     * <li>longitudinalAcceleration</li>
     * <li>curvature</li>
     * <li>curvatureCalculationMode</li>
     * <li>yawRate</li>
     * </ul>
     */
    public static final class Builder {
        private Heading heading;
        private Speed speed;
        private Integer driveDirection;
        private VehicleLength vehicleLength;
        private Integer vehicleWidth;
        private AccelerationComponent longitudinalAcceleration;
        private Curvature curvature;
        private Integer curvatureCalculationMode;
        private YawRate yawRate;
        private AccelerationControl accelerationControl;
        private Integer lanePosition;
        private SteeringWheelAngle steeringWheelAngle;
        private AccelerationComponent lateralAcceleration;
        private AccelerationComponent verticalAcceleration;
        private Integer performanceClass;
        private CenDsrcTollingZone cenDsrcTollingZone;

        private Builder() {}

        public Builder heading(Heading heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(Speed speed) {
            this.speed = speed;
            return this;
        }

        public Builder driveDirection(Integer driveDirection) {
            this.driveDirection = driveDirection;
            return this;
        }

        public Builder vehicleLength(VehicleLength vehicleLength) {
            this.vehicleLength = vehicleLength;
            return this;
        }

        public Builder vehicleWidth(Integer vehicleWidth) {
            this.vehicleWidth = vehicleWidth;
            return this;
        }

        public Builder longitudinalAcceleration(AccelerationComponent longitudinalAcceleration) {
            this.longitudinalAcceleration = longitudinalAcceleration;
            return this;
        }

        public Builder curvature(Curvature curvature) {
            this.curvature = curvature;
            return this;
        }

        public Builder curvatureCalculationMode(Integer curvatureCalculationMode) {
            this.curvatureCalculationMode = curvatureCalculationMode;
            return this;
        }

        public Builder yawRate(YawRate yawRate) {
            this.yawRate = yawRate;
            return this;
        }

        public Builder accelerationControl(AccelerationControl accelerationControl) {
            this.accelerationControl = accelerationControl;
            return this;
        }

        public Builder lanePosition(Integer lanePosition) {
            this.lanePosition = lanePosition;
            return this;
        }

        public Builder steeringWheelAngle(SteeringWheelAngle steeringWheelAngle) {
            this.steeringWheelAngle = steeringWheelAngle;
            return this;
        }

        public Builder lateralAcceleration(AccelerationComponent lateralAcceleration) {
            this.lateralAcceleration = lateralAcceleration;
            return this;
        }

        public Builder verticalAcceleration(AccelerationComponent verticalAcceleration) {
            this.verticalAcceleration = verticalAcceleration;
            return this;
        }

        public Builder performanceClass(Integer performanceClass) {
            this.performanceClass = performanceClass;
            return this;
        }

        public Builder cenDsrcTollingZone(CenDsrcTollingZone cenDsrcTollingZone) {
            this.cenDsrcTollingZone = cenDsrcTollingZone;
            return this;
        }

        public BasicVehicleContainerHighFrequency build() {
            return new BasicVehicleContainerHighFrequency(
                    requireNonNull(heading, "heading"),
                    requireNonNull(speed, "speed"),
                    requireNonNull(driveDirection, "drive_direction"),
                    requireNonNull(vehicleLength, "vehicle_length"),
                    requireNonNull(vehicleWidth, "vehicle_width"),
                    requireNonNull(longitudinalAcceleration, "longitudinal_acceleration"),
                    requireNonNull(curvature, "curvature"),
                    requireNonNull(curvatureCalculationMode, "curvature_calculation_mode"),
                    requireNonNull(yawRate, "yaw_rate"),
                    accelerationControl,
                    lanePosition,
                    steeringWheelAngle,
                    lateralAcceleration,
                    verticalAcceleration,
                    performanceClass,
                    cenDsrcTollingZone);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
