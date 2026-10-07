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

    public static List<LatLng> computeFootprint(LatLng position, double orientation, double length, double width) {
        LatLng frontCenter = Utils.pointFromPosition(position, orientation, length / 2);
        LatLng frontLeft = Utils.pointFromPosition(frontCenter, (orientation - 90 + 360) % 360, width / 2);
        LatLng frontRight = Utils.pointFromPosition(frontCenter, (orientation + 90 + 360) % 360, width / 2);
        LatLng rearCenter = Utils.pointFromPosition(position, (orientation + 180 + 360) % 360, length / 2);
        LatLng rearLeft = Utils.pointFromPosition(rearCenter, (orientation - 90 + 360) % 360, width / 2);
        LatLng rearRight = Utils.pointFromPosition(rearCenter, (orientation + 90 + 360) % 360, width / 2);
        return List.of(frontLeft, frontRight, rearRight, rearLeft);
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
