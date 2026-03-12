package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Confidence values for a perceived object.
 *
 * @param xDistance Distance confidence in x-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param yDistance Distance confidence in y-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param xSpeed Speed confidence in x-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param ySpeed Speed confidence in y-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param object Object confidence. Value: noConfidence(0), fullConfidence(15).
 * @param zDistance Distance confidence in z-direction. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(4094), unavailable(4095).
 * @param zSpeed Speed confidence in z-direction. Value: unavailable(0), prec100ms(1), prec10ms(2), prec5ms(3), prec1ms(4), prec0-1ms(5), prec0-05ms(6), prec0-01ms(7).
 * @param xAcceleration Acceleration confidence in x-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param yAcceleration Acceleration confidence in y-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param zAcceleration Acceleration confidence in z-direction. Value: pointOneMeterPerSecSquared(1), outOfRange(101), unavailable(102).
 * @param rollAngle Roll angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param pitchAngle Pitch angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param yawAngle Yaw angle confidence. Value: zeroPointOneDegree(1), oneDegree(10), outOfRange(126), unavailable(127).
 * @param rollRate Roll rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param pitchRate Pitch rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param yawRate Yaw rate confidence. Value: degSec-000-01(0), degSec-000-05(1), degSec-000-10(2), degSec-001-00(3), degSec-005-00(4), degSec-010-00(5), degSec-100-00(6), outOfRange(7), unavailable(8).
 * @param rollAcceleration Roll acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param pitchAcceleration Pitch acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param yawAcceleration Yaw acceleration confidence. Value: degSecSquared-000-01(0), degSecSquared-000-05(1), degSecSquared-000-10(2), degSecSquared-001-00(3), degSecSquared-005-00(4), degSecSquared-010-00(5), degSecSquared-100-00(6), outOfRange(7), unavailable(8).
 * @param planarObjectDimension1 Accuracy of first dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param planarObjectDimension2 Accuracy of second dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param verticalObjectDimension Accuracy of vertical dimension. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 * @param longitudinalLanePosition Accuracy of longitudinal lane position. Unit: 0.01 meter. Value: zeroPointZeroOneMeter(1), oneMeter(100), outOfRange(101), unavailable(102).
 */
public record PerceivedObjectConfidence(
        int xDistance,
        int yDistance,
        int xSpeed,
        int ySpeed,
        int object,
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
        Integer planarObjectDimension1,
        Integer planarObjectDimension2,
        Integer verticalObjectDimension,
        Integer longitudinalLanePosition) {}
