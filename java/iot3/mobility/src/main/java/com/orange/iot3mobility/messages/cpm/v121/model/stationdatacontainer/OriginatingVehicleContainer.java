package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Originating vehicle container.
 *
 * @param heading Heading of the vehicle movement with regards to the true north. Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800), wgs84West(2700), unavailable(3601).
 * @param speed Driving speed. Unit: 0.01 m/s. Value: standstill(0), oneCentimeterPerSec(1), unavailable(16383).
 * @param confidence {@link VehicleConfidence}
 * @param driveDirection Drive direction. Value: forward(0), backward(1), unavailable(2).
 * @param vehicleLength Vehicle length. Unit: 0.1 meter. Value: tenCentimeters(1), outOfRange(1022), unavailable(1023).
 * @param vehicleWidth Vehicle width. Unit: 0.1 meter. Value: tenCentimeters(1), outOfRange(61), unavailable(62).
 * @param longitudinalAcceleration Longitudinal acceleration. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquaredForward(1), pointOneMeterPerSecSquaredBackward(-1), unavailable(161).
 * @param yawRate Yaw rate. Unit: 0.01 degree/s. Value: straight(0), degSec-000-01ToRight(-1), degSec-000-01ToLeft(1), unavailable(32767).
 * @param lateralAcceleration Lateral acceleration. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquaredToRight(-1), pointOneMeterPerSecSquaredToLeft(1), unavailable(161).
 * @param verticalAcceleration Vertical acceleration. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquaredUp(1), pointOneMeterPerSecSquaredDown(-1), unavailable(161).
 */
public record OriginatingVehicleContainer(
        int heading,
        int speed,
        VehicleConfidence confidence,
        Integer driveDirection,
        Integer vehicleLength,
        Integer vehicleWidth,
        Integer longitudinalAcceleration,
        Integer yawRate,
        Integer lateralAcceleration,
        Integer verticalAcceleration) {}
