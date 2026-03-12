package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import java.util.List;

/**
 * Vehicle sensor detection area.
 *
 * @param refPointId Increasing counter of the trailer reference point (hitch point). Value: [0..255].
 * @param xSensorOffset Mounting position of sensor in x-direction from reference point. Unit: 0.01 meter. Value: negativeZeroPointZeroOneMeter(-1), negativeOneMeter(-100), negativeOutOfRange(-3094), positiveOneMeter(100), positiveOutOfRange(1001).
 * @param ySensorOffset Mounting position of sensor in y-direction from reference point. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param zSensorOffset Mounting position of sensor in z-direction from reference point. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100).
 * @param vehicleSensorPropertyList {@link VehicleSensorProperty}
 */
public record VehicleSensor(
        Integer refPointId,
        int xSensorOffset,
        int ySensorOffset,
        Integer zSensorOffset,
        List<VehicleSensorProperty> vehicleSensorPropertyList) {}
