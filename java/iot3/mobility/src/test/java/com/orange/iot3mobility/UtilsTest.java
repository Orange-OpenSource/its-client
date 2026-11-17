/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility;

import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    // computeFootprint
    // -------------------------------------------------------------------------

    @Test
    void computeFootprintReturnsSixPoints() {
        LatLng position = new LatLng(48.8566, 2.3522);
        List<LatLng> footprint = Utils.computeFootprint(position, 0.0, 4.5, 2.0);
        assertEquals(6, footprint.size(),
                "Footprint should have 6 points: 2 rear corners + 2 front-cut points on each side");
    }

    @Test
    void computeFootprintRearEdgeIsFullWidth() {
        LatLng position = new LatLng(0.0, 0.0);
        double length = 4.0;
        double width = 2.0;
        List<LatLng> footprint = Utils.computeFootprint(position, 0.0, length, width);

        LatLng rearLeft = footprint.get(0);
        LatLng rearRight = footprint.get(1);

        // Rear corners should be symmetric around the central axis (same latitude offset
        // magnitude since heading is North, opposite longitude sign).
        double rearLeftLatOffset = rearLeft.getLatitude() - position.getLatitude();
        double rearRightLatOffset = rearRight.getLatitude() - position.getLatitude();
        assertEquals(rearLeftLatOffset, rearRightLatOffset, DELTA,
                "Rear-left and rear-right should be at the same distance behind the center");
    }

    @Test
    void computeFootprintFrontCutPointsAreInsetFromFullWidthCorners() {
        LatLng position = new LatLng(0.0, 0.0);
        double orientation = 0.0; // heading North
        double length = 4.0;
        double width = 2.0;
        List<LatLng> footprint = Utils.computeFootprint(position, orientation, length, width);

        LatLng frontRightCut = footprint.get(3);
        LatLng frontLeftCut = footprint.get(4);

        // The theoretical, uncut front corners (what frontLeft/frontRight used to be).
        LatLng frontCenter = Utils.pointFromPosition(position, orientation, length / 2);
        LatLng theoreticalFrontLeft =
                Utils.pointFromPosition(frontCenter, (orientation - 90 + 360) % 360, width / 2);
        LatLng theoreticalFrontRight =
                Utils.pointFromPosition(frontCenter, (orientation + 90 + 360) % 360, width / 2);

        // Cut points should be strictly inside the theoretical full-width front edge,
        // i.e. closer to the central axis than the uncut corners.
        assertTrue(Math.abs(frontLeftCut.getLongitude() - position.getLongitude())
                        < Math.abs(theoreticalFrontLeft.getLongitude() - position.getLongitude()),
                "Front-left cut point should be closer to the central axis than the uncut corner");
        assertTrue(Math.abs(frontRightCut.getLongitude() - position.getLongitude())
                        < Math.abs(theoreticalFrontRight.getLongitude() - position.getLongitude()),
                "Front-right cut point should be closer to the central axis than the uncut corner");
    }

    @Test
    void computeFootprintIsSymmetricLeftRight() {
        LatLng position = new LatLng(0.0, 0.0);
        List<LatLng> footprint = Utils.computeFootprint(position, 0.0, 4.0, 2.0);

        LatLng rearLeft = footprint.get(0);
        LatLng rearRight = footprint.get(1);
        LatLng frontRightSide = footprint.get(2);
        LatLng frontLeftSide = footprint.get(5);

        // Symmetric longitude offsets (opposite sign, same magnitude) around the central axis.
        assertEquals(Math.abs(rearLeft.getLongitude() - position.getLongitude()),
                Math.abs(rearRight.getLongitude() - position.getLongitude()), DELTA);
        assertEquals(Math.abs(frontLeftSide.getLongitude() - position.getLongitude()),
                Math.abs(frontRightSide.getLongitude() - position.getLongitude()), DELTA);
    }

    @Test
    void computeFootprintChamferScalesWithSmallestDimension() {
        LatLng position = new LatLng(0.0, 0.0);
        double orientation = 0.0;

        // A longer vehicle (length > width): chamfer is based on width (smallest dimension).
        List<LatLng> narrowFootprint = Utils.computeFootprint(position, orientation, 10.0, 2.0);
        LatLng narrowFrontLeftSide = narrowFootprint.get(5);

        // Same width, bigger length should not change the chamfer size (still based on width).
        List<LatLng> longerFootprint = Utils.computeFootprint(position, orientation, 20.0, 2.0);
        LatLng longerFrontLeftSide = longerFootprint.get(5);

        double narrowChamferDistance = Math.abs(
                narrowFrontLeftSide.getLatitude() - Utils.pointFromPosition(
                        position, orientation, 10.0 / 2).getLatitude());
        double longerChamferDistance = Math.abs(
                longerFrontLeftSide.getLatitude() - Utils.pointFromPosition(
                        position, orientation, 20.0 / 2).getLatitude());

        assertEquals(narrowChamferDistance, longerChamferDistance, 1e-4,
                "Chamfer size should depend on the smallest dimension (width), not on length");
    }

    @Test
    void computeFootprintSmallFootprintUsesMinimumChamferFloor() {
        LatLng position = new LatLng(0.0, 0.0);
        double orientation = 0.0;
        // A small pedestrian-like footprint: 15% of 0.5m (0.075m) is below the 0.15m floor,
        // so the floor should be applied instead of the proportional ratio.
        double length = 0.5;
        double width = 0.5;
        List<LatLng> footprint = Utils.computeFootprint(position, orientation, length, width);

        LatLng frontLeftSide = footprint.get(5);
        LatLng theoreticalFrontLeft = Utils.pointFromPosition(
                Utils.pointFromPosition(position, orientation, length / 2),
                (orientation - 90 + 360) % 360, width / 2);

        double chamferDistanceMeters = haversineDistanceMeters(frontLeftSide, theoreticalFrontLeft);

        assertEquals(0.15, chamferDistanceMeters, 0.01,
                "Chamfer should be floored to 0.15m since 15% of the smallest dimension is smaller");
    }

    @Test
    void computeFootprintTinyFootprintChamferNeverExceedsHalfSmallestDimension() {
        LatLng position = new LatLng(0.0, 0.0);
        double orientation = 0.0;
        // A tiny footprint where even the 0.15m floor would exceed half of the smallest
        // dimension (0.1m): the chamfer must be capped to avoid a self-intersecting polygon.
        double length = 0.2;
        double width = 0.2;
        List<LatLng> footprint = Utils.computeFootprint(position, orientation, length, width);

        assertEquals(6, footprint.size());

        LatLng frontLeftSide = footprint.get(5);
        LatLng theoreticalFrontLeft = Utils.pointFromPosition(
                Utils.pointFromPosition(position, orientation, length / 2),
                (orientation - 90 + 360) % 360, width / 2);

        double chamferDistanceMeters = haversineDistanceMeters(frontLeftSide, theoreticalFrontLeft);

        assertTrue(chamferDistanceMeters <= 0.1 + 1e-6,
                "Chamfer must not exceed half of the smallest dimension (0.1m here)");
    }

    private static double haversineDistanceMeters(LatLng a, LatLng b) {
        double earthRadiusMeters = 6378100.0;
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double deltaLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double deltaLng = Math.toRadians(b.getLongitude() - a.getLongitude());

        double h = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return earthRadiusMeters * c;
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

