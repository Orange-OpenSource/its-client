/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility;

import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Utils class cannot be instantiated");
    }

    public static LatLng pointFromPosition(LatLng origin, double bearing, double distance){
        double earthRadius = 6378.1;
        double bearingRad = Math.toRadians(bearing);
        double d = distance / 1000;


        double lat1 = Math.toRadians(origin.getLatitude()); //Current lat point converted to radians
        double lng1 = Math.toRadians(origin.getLongitude()); //Current long point converted to radians

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/earthRadius) +
                Math.cos(lat1)*Math.sin(d/earthRadius)*Math.cos(bearingRad));

        double lng2 = lng1 + Math.atan2(Math.sin(bearingRad)*Math.sin(d/earthRadius)*Math.cos(lat1),
                Math.cos(d/earthRadius)-Math.sin(lat1)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lng2 = Math.toDegrees(lng2);

        return new LatLng(lat2, lng2);
    }

    /**
     * Ratio applied to the smallest of {@code length}/{@code width} to compute the size of the
     * front corner cuts (chamfers), used to make the front of the footprint visually distinct
     * from its rectangular rear.
     */
    private static final double FRONT_CHAMFER_RATIO = 0.15;

    /**
     * Minimum absolute chamfer size, in meters. Without this floor, the proportional chamfer
     * becomes imperceptible on small footprints (e.g. pedestrians, small objects below 1 meter),
     * making the hexagon visually indistinguishable from a plain rectangle.
     */
    private static final double MIN_FRONT_CHAMFER_METERS = 0.15;

    /**
     * Computes the ground footprint of an object as a hexagon with its two front corners cut
     * (chamfered), so the front of the shape is visually distinguishable from its rectangular
     * rear.
     *
     * @param position    center position of the object
     * @param orientation orientation (heading) in degrees, 0 = North, clockwise
     * @param length      length of the object, in meters
     * @param width       width of the object, in meters
     * @return the 6 points of the footprint, in order: rear-left, rear-right, front-right-side,
     *         front-right-cut, front-left-cut, front-left-side
     */
    public static List<LatLng> computeFootprint(LatLng position, double orientation, double length, double width) {
        double minDimension = Math.min(length, width);
        // Floor the chamfer to a minimum absolute size so it stays visible on small footprints,
        // but never let it exceed half of the smallest dimension to avoid a self-intersecting
        // (bowtie) polygon on very small objects.
        double chamfer = Math.min(
                Math.max(minDimension * FRONT_CHAMFER_RATIO, MIN_FRONT_CHAMFER_METERS),
                minDimension / 2);

        LatLng frontCenter = Utils.pointFromPosition(position, orientation, length / 2);
        LatLng frontLeft = Utils.pointFromPosition(frontCenter, (orientation - 90 + 360) % 360, width / 2);
        LatLng frontRight = Utils.pointFromPosition(frontCenter, (orientation + 90 + 360) % 360, width / 2);
        LatLng rearCenter = Utils.pointFromPosition(position, (orientation + 180 + 360) % 360, length / 2);
        LatLng rearLeft = Utils.pointFromPosition(rearCenter, (orientation - 90 + 360) % 360, width / 2);
        LatLng rearRight = Utils.pointFromPosition(rearCenter, (orientation + 90 + 360) % 360, width / 2);

        // Cut the front-left corner: one point moved back along the left side, one point
        // moved inward along the front edge, replacing the single frontLeft corner.
        LatLng frontLeftSide = Utils.pointFromPosition(
                frontLeft, (orientation + 180 + 360) % 360, chamfer);
        LatLng frontLeftCut = Utils.pointFromPosition(
                frontLeft, (orientation + 90 + 360) % 360, chamfer);

        // Cut the front-right corner symmetrically.
        LatLng frontRightSide = Utils.pointFromPosition(
                frontRight, (orientation + 180 + 360) % 360, chamfer);
        LatLng frontRightCut = Utils.pointFromPosition(
                frontRight, (orientation - 90 + 360) % 360, chamfer);

        return List.of(rearLeft, rearRight, frontRightSide, frontRightCut, frontLeftCut, frontLeftSide);
    }

    public static String getRandomUuid() {
        return UUID.randomUUID().toString().substring(0,7);
    }

    public static int randomBetween(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        else if (value > max) return max;
        return value;
    }

    public static float normalizeAngle(float heading) {
        return (heading % 360 + 360) % 360;
    }

}
