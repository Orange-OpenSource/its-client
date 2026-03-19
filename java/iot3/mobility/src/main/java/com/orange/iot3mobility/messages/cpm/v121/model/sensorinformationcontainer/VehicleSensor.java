/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import java.util.List;

/**
 * Vehicle sensor detection area.
 *
 * @param refPointId Optional. Increasing counter of the trailer reference point (hitch point). Value: [0..255].
 * @param xSensorOffset Mounting position of sensor in x-direction from reference point. Unit: 0.01 meter.
 *                      Value: negativeZeroPointZeroOneMeter(-1), negativeOneMeter(-100), negativeOutOfRange(-3094),
 *                      positiveOneMeter(100), positiveOutOfRange(1001).
 * @param ySensorOffset Mounting position of sensor in y-direction from reference point. Unit: 0.01 meter.
 *                      Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param zSensorOffset Optional. Mounting position of sensor in z-direction from reference point. Unit: 0.01 meter.
 *                      Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param vehicleSensorPropertyList {@link VehicleSensorProperty}
 */
public record VehicleSensor(
        Integer refPointId,
        int xSensorOffset,
        int ySensorOffset,
        Integer zSensorOffset,
        List<VehicleSensorProperty> vehicleSensorPropertyList) {
    
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer refPointId;
        private Integer xSensorOffset;
        private Integer ySensorOffset;
        private Integer zSensorOffset;
        private List<VehicleSensorProperty> vehicleSensorPropertyList;

        public Builder refPointId(Integer refPointId) {
            this.refPointId = refPointId;
            return this;
        }

        public Builder sensorOffset(int xSensorOffset, int ySensorOffset, Integer zSensorOffset) {
            this.xSensorOffset = xSensorOffset;
            this.ySensorOffset = ySensorOffset;
            this.zSensorOffset = zSensorOffset;
            return this;
        }

        public Builder vehicleSensorPropertyList(List<VehicleSensorProperty> vehicleSensorPropertyList) {
            this.vehicleSensorPropertyList = vehicleSensorPropertyList;
            return this;
        }

        public VehicleSensor build() {
            return new VehicleSensor(
                    refPointId,
                    requireNonNull(xSensorOffset, "x_sensor_offset"),
                    requireNonNull(ySensorOffset, "y_sensor_offset"),
                    zSensorOffset,
                    requireNonNull(vehicleSensorPropertyList, "vehicle_sensor_property_list"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
