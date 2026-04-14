/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility;

import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    private static final double DELTA = 1e-6;

    // -------------------------------------------------------------------------
    // pointFromPosition
    // -------------------------------------------------------------------------

    @Test
    void pointFromPositionNorthShiftsLatitude() {
        LatLng origin = new LatLng(0.0, 0.0);
        // Move 1 km North (bearing 0°). Latitude should increase, longitude unchanged.
        LatLng result = Utils.pointFromPosition(origin, 0.0, 1000.0);
        assertTrue(result.getLatitude() > 0.0,
                "Moving North should increase latitude");
        assertEquals(0.0, result.getLongitude(), 1e-4,
                "Moving North should not change longitude");
    }

    @Test
    void pointFromPositionEastShiftsLongitude() {
        LatLng origin = new LatLng(0.0, 0.0);
        // Move 1 km East (bearing 90°). Longitude should increase, latitude roughly unchanged.
        LatLng result = Utils.pointFromPosition(origin, 90.0, 1000.0);
        assertTrue(result.getLongitude() > 0.0,
                "Moving East should increase longitude");
        assertEquals(0.0, result.getLatitude(), 1e-4,
                "Moving East at equator should not change latitude");
    }

    @Test
    void pointFromPositionZeroDistanceReturnsSamePoint() {
        LatLng origin = new LatLng(48.8566, 2.3522);
        LatLng result = Utils.pointFromPosition(origin, 45.0, 0.0);
        assertEquals(origin.getLatitude(), result.getLatitude(), DELTA);
        assertEquals(origin.getLongitude(), result.getLongitude(), DELTA);
    }

    @Test
    void pointFromPositionApproximateDistanceCheck() {
        // 111 km ≈ 1 degree of latitude at the equator
        LatLng origin = new LatLng(0.0, 0.0);
        LatLng result = Utils.pointFromPosition(origin, 0.0, 111000.0);
        assertEquals(1.0, result.getLatitude(), 0.01,
                "111 km North should be ~1 degree of latitude");
    }

    // -------------------------------------------------------------------------
    // clamp
    // -------------------------------------------------------------------------

    @Test
    void clampReturnMinWhenBelowMin() {
        assertEquals(0.0f, Utils.clamp(-5.0f, 0.0f, 10.0f), 0.0f);
    }

    @Test
    void clampReturnMaxWhenAboveMax() {
        assertEquals(10.0f, Utils.clamp(15.0f, 0.0f, 10.0f), 0.0f);
    }

    @Test
    void clampReturnValueWhenWithinRange() {
        assertEquals(5.0f, Utils.clamp(5.0f, 0.0f, 10.0f), 0.0f);
    }

    @Test
    void clampReturnMinWhenEqualToMin() {
        assertEquals(0.0f, Utils.clamp(0.0f, 0.0f, 10.0f), 0.0f);
    }

    @Test
    void clampReturnMaxWhenEqualToMax() {
        assertEquals(10.0f, Utils.clamp(10.0f, 0.0f, 10.0f), 0.0f);
    }

    // -------------------------------------------------------------------------
    // normalizeAngle
    // -------------------------------------------------------------------------

    @Test
    void normalizeAnglePositiveWithinRangeUnchanged() {
        assertEquals(90.0f, Utils.normalizeAngle(90.0f), 0.001f);
    }

    @Test
    void normalizeAngleZeroStaysZero() {
        assertEquals(0.0f, Utils.normalizeAngle(0.0f), 0.001f);
    }

    @Test
    void normalizeAngle360BecomesZero() {
        assertEquals(0.0f, Utils.normalizeAngle(360.0f), 0.001f);
    }

    @Test
    void normalizeAngleNegativeWrapsAround() {
        assertEquals(270.0f, Utils.normalizeAngle(-90.0f), 0.001f);
    }

    @Test
    void normalizeAngleOver360Wraps() {
        assertEquals(90.0f, Utils.normalizeAngle(450.0f), 0.001f);
    }

    // -------------------------------------------------------------------------
    // randomBetween
    // -------------------------------------------------------------------------

    @RepeatedTest(20)
    void randomBetweenIsWithinBounds() {
        int result = Utils.randomBetween(5, 10);
        assertTrue(result >= 5 && result <= 10,
                "randomBetween(5,10) must be in [5, 10], got " + result);
    }

    @Test
    void randomBetweenSameMinAndMaxReturnsThatValue() {
        assertEquals(7, Utils.randomBetween(7, 7));
    }
}

