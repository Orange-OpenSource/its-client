package com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer;

/**
 * Confidence values for originating vehicle container.
 *
 * @param heading Heading accuracy of the vehicle movement with regards to the true north. Unit: 0.1 degree. Value: equalOrWithinZeroPointOneDegree(1), equalOrWithinOneDegree(10), outOfRange(126), unavailable(127).
 * @param speed Speed accuracy. Unit: 0.01 m/s. Value: equalOrWithinOneCentimeterPerSec(1), equalOrWithinOneMeterPerSec(100), outOfRange(126), unavailable(127).
 * @param vehicleLength Trailer presence/length confidence. Value: noTrailerPresent(0), trailerPresentWithKnownLength(1), trailerPresentWithUnknownLength(2), trailerPresenceIsUnknown(3), unavailable(4).
 * @param yawRate Yaw rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param longitudinalAcceleration Longitudinal acceleration confidence. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param lateralAcceleration Lateral acceleration confidence. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param verticalAcceleration Vertical acceleration confidence. Unit: 0.1 m/s2. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 */
public record VehicleConfidence(
        int heading,
        int speed,
        Integer vehicleLength,
        Integer yawRate,
        Integer longitudinalAcceleration,
        Integer lateralAcceleration,
        Integer verticalAcceleration) {}
