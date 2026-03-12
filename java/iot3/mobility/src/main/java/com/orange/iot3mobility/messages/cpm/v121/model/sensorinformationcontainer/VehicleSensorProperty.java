package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

/**
 * Vehicle sensor property item.
 *
 * @param range Range of sensor within the indicated azimuth angle. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param horizontalOpeningAngleStart Start of the sensor's horizontal opening angle extension. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param horizontalOpeningAngleEnd End of the sensor's horizontal opening angle extension. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleStart Start of the sensor's vertical opening angle extension. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleEnd End of the sensor's vertical opening angle extension. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 */
public record VehicleSensorProperty(
        int range,
        int horizontalOpeningAngleStart,
        int horizontalOpeningAngleEnd,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd) {}
