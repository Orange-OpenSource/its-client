package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.Offset;

/**
 * Stationary radial sensor detection area.
 *
 * @param range Radial range of the sensor from the reference point. Unit: 0.1 meter. Value: zeroPointOneMeter(1), oneMeter(10).
 * @param horizontalOpeningAngleStart Start of the stationary sensor's horizontal opening angle in WGS84. Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param horizontalOpeningAngleEnd End of the stationary sensor's horizontal opening angle in WGS84. Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param verticalOpeningAngleStart Start of the stationary sensor's vertical opening angle. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleEnd End of the stationary sensor's vertical opening angle. Unit: 0.1 degree. Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param sensorPositionOffset Offset of the mounting point from the station's reference position.
 */
public record StationarySensorRadial(
        int range,
        int horizontalOpeningAngleStart,
        int horizontalOpeningAngleEnd,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd,
        Offset sensorPositionOffset) {}
