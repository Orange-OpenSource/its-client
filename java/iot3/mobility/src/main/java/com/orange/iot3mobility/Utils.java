package com.orange.iot3mobility;

import com.orange.iot3mobility.quadkey.LatLng;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	// REGEX
    private static final String IP_ADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final String MAC_ADDRESS_PATTERN = "(([0-9A-Fa-f]{2}[-:]){5}[0-9A-Fa-f]{2})|(([0-9A-Fa-f]{4}.){2}[0-9A-Fa-f]{4})";
    private static final String COORDINATES_PATTERN = "^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)$";
    private static final String QUADKEY_PATTERN = "^[0-3]*$";
    private static final String IP_PORT_PATTERN = ":.\\d+";
    private static final String NUMBER_PATTERN = ".*\\d.*";
    
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * 
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distanceBetween(LatLng position1, LatLng position2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(position2.getLatitude() - position1.getLatitude());
        double lonDistance = Math.toRadians(position2.getLongitude() - position1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(position1.getLatitude())) * Math.cos(Math.toRadians(position2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
    
    public static LatLng pointFromPosition(LatLng origin, double bearing, double distance){
        double R = 6378.1; //Radius of the Earth
        double bearingRad = Math.toRadians(bearing); //Bearing is 90 degrees converted to radians.
        double d = distance/1000; //Distance in km


        double lat1 = Math.toRadians(origin.getLatitude()); //Current lat point converted to radians
        double lng1 = Math.toRadians(origin.getLongitude()); //Current long point converted to radians

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                Math.cos(lat1)*Math.sin(d/R)*Math.cos(bearingRad));

        double lng2 = lng1 + Math.atan2(Math.sin(bearingRad)*Math.sin(d/R)*Math.cos(lat1),
                Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lng2 = Math.toDegrees(lng2);

        return new LatLng(lat2, lng2);
    }

    /**
     * Returns the distance between two LatLng objects in meters.
     *
     * @param  position1	first LatLng position
     * @param  position2	second LatLng position
     */
    public static float distanceBetweenLatLng(LatLng position1, LatLng position2) {
    	double earthRadius = 6371000; // Earth's radius in meters
        
    	// Convert latitude and longitude values to radians
        double lat1 = Math.toRadians(position1.getLatitude());
        double lon1 = Math.toRadians(position1.getLongitude());
        double lat2 = Math.toRadians(position2.getLatitude());
        double lon2 = Math.toRadians(position2.getLongitude());
        
        // Calculate the differences in latitudes and longitudes
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        
        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        // Central angle (in radians)
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Calculate distance using the central angle and Earth's radius
        float distance = (float) (earthRadius * c);
        return distance;
    }
    
    /**
     * Returns the bearing between to LatLng objects in degrees.
     *
     * @param  initialPosition	bearing from this LatLng position
     * @param  finalPosition	to this LatLng position
     */
    public static float bearingToLatLng(LatLng initialPosition, LatLng finalPosition) {
    	// Convert latitude and longitude values to radians
        double lat1 = Math.toRadians(initialPosition.getLatitude());
        double lon1 = Math.toRadians(initialPosition.getLongitude());
        double lat2 = Math.toRadians(finalPosition.getLatitude());
        double lon2 = Math.toRadians(finalPosition.getLongitude());
        
        // Calculate the difference in longitudes
        double dLon = lon2 - lon1;
        
        // Calculate the components for the bearing formula
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        
        // Calculate the bearing in radians
        double bearingRad = Math.atan2(y, x);
        
        // Convert the bearing from radians to degrees
        float bearingDeg = (float) Math.toDegrees(bearingRad);
        
        // Normalize the bearing to a range of [0, 360)
        bearingDeg = (bearingDeg + 360) % 360;
        
        return bearingDeg;
    }
    
    /**
     * Length (angular) of a shortest way between two angles.
     * It will be in range [0, 180].
     */
    public static float angleBetween0and180(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;       // This is either the distance or 360 - distance
        double angle = phi > 180 ? 360 - phi : phi;
        return (float) angle;
    }

    public static float angleBetween0and90(float alpha, float beta) {
        float phi = angleBetween0and180(alpha, beta);
        float angle = phi > 90 ? 180 - phi : phi;
        return angle;
    }

    /**
     * Length (angular) between two angles.
     * It will be in range [-180, 180].
     */
    public static double getAngleDifference(double alpha, double beta) {
        double result = (beta - alpha) % 360.0;
        if (result < -180.0)
            result += 360.0;
        if (result >= 180.0)
            result -= 360.0;
        return result;
    }
    
    public static LatLng getCenterPoint(ArrayList<LatLng> points) {
    	double latSum = 0;
        double lngSum = 0;
        
        for(LatLng point: points) {
        	latSum += point.getLatitude();
        	lngSum += point.getLongitude();
        }
        
        int total = points.size();
        
        double latitude = latSum / total;
        double longitude = lngSum / total;
    	
    	return new LatLng(latitude, longitude);
    }
	
	public static int hexToDec(String hex) {
        return Integer.parseInt(hex,16);
    }

    public static String stringToHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    public static int stringToDec(String arg) {
        return hexToDec(stringToHex(arg));
    }
    
    public static String getRandomUuid() {
        return UUID.randomUUID().toString().substring(0,7);
    }
    
    public static int randomBetween(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
    public static boolean containsNumber(String string) {
        return string.matches(NUMBER_PATTERN);
    }

    public static boolean isValidIP(String ipAddress) {
        return matchingRegex(ipAddress, IP_ADDRESS_PATTERN);
    }

    public static boolean matchingCoordinates(String coordinates) {
        return matchingRegex(coordinates, COORDINATES_PATTERN);
    }
    
    public static boolean correctPosition(LatLng position) {
    	String latitude = String.valueOf(position.getLatitude());
    	String longitude = String.valueOf(position.getLongitude());
    	return matchingCoordinates(latitude) && matchingCoordinates(longitude);
    }

    public static boolean matchingQuadkey(String quadkey) {
        return matchingRegex(quadkey, QUADKEY_PATTERN);
    }
    
    public static boolean matchingMacAddress(String macAddress) {
        return matchingRegex(macAddress, MAC_ADDRESS_PATTERN);
    }

    public static boolean matchingIpAddress(String address) {
        return matchingRegex(address, IP_ADDRESS_PATTERN);
    }
    
    public static boolean matchingIpPort(String port) {
        return matchingRegex(port, IP_PORT_PATTERN);
    }

    public static boolean matchingRegex(String match, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(match);
        return matcher.find();
    }

    public static boolean doPolylinesIntersect(ArrayList<LatLng> polyline1, ArrayList<LatLng> polyline2) {
        int n1 = polyline1.size();
        int n2 = polyline2.size();
        for (int i = 1; i < n1; i++) {
            for (int j = 1; j < n2; j++) {
                LatLng p1 = polyline1.get(i - 1);
                LatLng q1 = polyline1.get(i);
                LatLng p2 = polyline2.get(j - 1);
                LatLng q2 = polyline2.get(j);
                if (doSegmentsIntersect(p1, q1, p2, q2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean doSegmentsIntersect(LatLng p1, LatLng q1, LatLng p2, LatLng q2) {
        double d1 = direction(p2, q2, p1);
        double d2 = direction(p2, q2, q1);
        double d3 = direction(p1, q1, p2);
        double d4 = direction(p1, q1, q2);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        } else if (d1 == 0 && onSegment(p2, q2, p1)) {
            return true;
        } else if (d2 == 0 && onSegment(p2, q2, q1)) {
            return true;
        } else if (d3 == 0 && onSegment(p1, q1, p2)) {
            return true;
        } else if (d4 == 0 && onSegment(p1, q1, q2)) {
            return true;
        } else {
            return false;
        }
    }

    public static double direction(LatLng pi, LatLng pj, LatLng pk) {
        return (pk.getLongitude() - pi.getLongitude()) * (pj.getLatitude() - pi.getLatitude()) - (pj.getLongitude() - pi.getLongitude()) * (pk.getLatitude() - pi.getLatitude());
    }

    public static boolean onSegment(LatLng pi, LatLng pj, LatLng pk) {
        return Math.min(pi.getLongitude(), pj.getLongitude()) <= pk.getLongitude() && pk.getLongitude() <= Math.max(pi.getLongitude(), pj.getLongitude()) &&
                Math.min(pi.getLatitude(), pj.getLatitude()) <= pk.getLatitude() && pk.getLatitude() <= Math.max(pi.getLatitude(), pj.getLatitude());
    }

    // Returns true if the segments intersect, otherwise false. In addition, if the lines
    // intersect the intersection point may be stored in the doubles ilat and ilng.
    public static boolean doSegmentsIntercept(double p0lat, double p0lng, double p1lat, double p1lng,
                               double p2lat, double p2lng, double p3lat, double p3lng) {
        double ilat, ilng;

        double s1lat, s1lng, s2lat, s2lng;
        s1lat = p1lat - p0lat;     s1lng = p1lng - p0lng;
        s2lat = p3lat - p2lat;     s2lng = p3lng - p2lng;

        double s, t;
        s = (-s1lng * (p0lat - p2lat) + s1lat * (p0lng - p2lng)) / (-s2lat * s1lng + s1lat * s2lng);
        t = ( s2lat * (p0lng - p2lng) - s2lng * (p0lat - p2lat)) / (-s2lat * s1lng + s1lat * s2lng);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            // Collision detected
            ilat = p0lat + (t * s1lat);
            ilng = p0lng + (t * s1lng);

            return true;
        }

        return false; // No collision
    }

    // Returns true if the segments intersect, otherwise false. In addition, if the lines
    // intersect the intersection point may be stored in the doubles ilat and ilng.
    public static LatLng getSegmentsIntercept(double p0lat, double p0lng, double p1lat, double p1lng,
                                              double p2lat, double p2lng, double p3lat, double p3lng) {
        double ilat, ilng;

        double s1lat, s1lng, s2lat, s2lng;
        s1lat = p1lat - p0lat;     s1lng = p1lng - p0lng;
        s2lat = p3lat - p2lat;     s2lng = p3lng - p2lng;

        double s, t;
        s = (-s1lng * (p0lat - p2lat) + s1lat * (p0lng - p2lng)) / (-s2lat * s1lng + s1lat * s2lng);
        t = ( s2lat * (p0lng - p2lng) - s2lng * (p0lat - p2lat)) / (-s2lat * s1lng + s1lat * s2lng);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            // Collision detected
            ilat = p0lat + (t * s1lat);
            ilng = p0lng + (t * s1lng);

            return new LatLng(ilat, ilng);
        }

        return null; // No collision
    }
    
    /**
     * Determines if a point is inside an ellipse.
     *
     * @param x The x-coordinate of the point to check
     * @param y The y-coordinate of the point to check
     * @param xCenter The x-coordinate of the center of the ellipse
     * @param yCenter The y-coordinate of the center of the ellipse
     * @param a The width of the ellipse
     * @param b The height of the ellipse
     * @param theta The angle of rotation of the ellipse (in degrees)
     * @return true if the point is inside the ellipse, false otherwise
     */
    public static boolean isPointInEllipse(double x, double y, double xCenter, double yCenter,
                                           double a, double b, double theta) {
        // Calculate the difference between the point and the center of the ellipse
        double xc = x - xCenter;
        double yc = y - yCenter;

        // Calculate the sine and cosine of the angle of rotation (converted to radians)
        double cosTheta = Math.cos(Math.toRadians(180 - theta));
        double sinTheta = Math.sin(Math.toRadians(180 - theta));

        // Rotate the point by the angle of the ellipse
        double xct = xc * cosTheta - yc * sinTheta;
        double yct = xc * sinTheta + yc * cosTheta;

        // Check if the point is inside the ellipse
        double rad_cc = (xct * xct) / ((a / 2) * (a / 2)) + (yct * yct) / ((b / 2) * (b / 2));

        return rad_cc <= 1;
    }
    
    public static boolean isPointNearPolyline(LatLng point, float heading, ArrayList<LatLng> polyline, float maxDistance, float maxAngle) {
        for(int i = 0; i < polyline.size() - 1; i++) {
            LatLng p1 = polyline.get(i);
            LatLng p2 = polyline.get(i + 1);

            // Calculate the unit vector for the polyline segment
            double segmentX = p2.getLongitude() - p1.getLongitude();
            double segmentY = p2.getLatitude() - p1.getLatitude();
            double segmentLength = Math.sqrt(segmentX * segmentX + segmentY * segmentY);
            segmentX /= segmentLength;
            segmentY /= segmentLength;

            // Check if the distance and angle thresholds are satisfied
            double distance = distanceToSegment(point, p1, p2);
            if(canBeProjectedOnSegment(point, p1, p2) && distance <= maxDistance) {
                // Calculate the heading of the segment
                float segmentHeading = (float) Math.toDegrees(Math.atan2(segmentY, segmentX));
                if(segmentHeading < 0) {
                    segmentHeading += 360;
                }
                // Calculate the difference between the object heading and the segment heading
                double angleDiff = getAngleDifference(heading, segmentHeading);
                // Check if the object is moving along the polyline
                if(Math.abs(angleDiff) <= maxAngle || Math.abs(angleDiff) >= 180 - maxAngle) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static double distanceToSegment(LatLng position, LatLng pointA, LatLng pointB) {
        // triangle points
        LatLng A = pointA;
        LatLng B = pointB;
        LatLng C = position;

        // triangle sides
        double a = distanceBetweenLatLng(B, C);
        double b = distanceBetweenLatLng(A, C);
        double c = distanceBetweenLatLng(A, B);

        // triangle angles
        double alpha = Math.acos((b*b+c*c-a*a)/(2*b*c));
        double beta = Math.acos((a*a+c*c-b*b)/(2*a*c));
        double gamma = Math.acos((a*a+b*b-c*c)/(2*a*b));

        // length to add from closest point to get to the base of triangle height
        double x = Math.abs(b*Math.cos(alpha));

        // latitude and longitude difference between A and B points
        double dLat = 0, dLng = 0;

        double resultLat = 0, resultLng = 0;

        // Determine the coordinates of closest point on the segment AB
        //showToast("alpha: "+alpha+" beta: "+beta+" gamma: "+gamma+" a: "+a+" b: "+b+" c: "+c+" x: "+x+" x/c: "+x/c, true);
        if(A.getLatitude() < B.getLatitude() && A.getLongitude() < B.getLongitude()){
            dLat = B.getLatitude() - A.getLatitude();
            dLng = B.getLongitude() - A.getLongitude();
            resultLat = A.getLatitude() + (dLat * x/c);
            resultLng = A.getLongitude() + (dLng * x/c);
        }else if(A.getLatitude() > B.getLatitude() && A.getLongitude() < B.getLongitude()){
            dLat = A.getLatitude() - B.getLatitude();
            dLng = B.getLongitude() - A.getLongitude();
            resultLat = A.getLatitude() - (dLat * x/c);
            resultLng = A.getLongitude() + (dLng * x/c);
        }else if(A.getLatitude() < B.getLatitude() && A.getLongitude() > B.getLongitude()){
            dLat = B.getLatitude() - A.getLatitude();
            dLng = A.getLongitude() - B.getLongitude();
            resultLat = A.getLatitude() + (dLat * x/c);
            resultLng = A.getLongitude() - (dLng * x/c);
        }else if(A.getLatitude() > B.getLatitude() && A.getLongitude() > B.getLongitude()){
            dLat = A.getLatitude() - B.getLatitude();
            dLng = A.getLongitude() - B.getLongitude();
            resultLat = A.getLatitude() - (dLat * x/c);
            resultLng = A.getLongitude() - (dLng * x/c);
        }

        LatLng closestPoint = new LatLng(resultLat, resultLng);
        return distanceBetweenLatLng(position, closestPoint);
    }

    public static boolean canBeProjectedOnSegment(LatLng position, LatLng pointA, LatLng pointB) {
        // triangle points
        LatLng A = pointA;
        LatLng B = pointB;
        LatLng C = position;

        // triangle sides
        double a = distanceBetweenLatLng(B, C);
        double b = distanceBetweenLatLng(A, C);
        double c = distanceBetweenLatLng(A, B);

        // triangle angles
        double alpha = Math.acos((b*b+c*c-a*a)/(2*b*c));
        double beta = Math.acos((a*a+c*c-b*b)/(2*a*c));

        return Math.toDegrees(alpha) <= 90 && Math.toDegrees(beta) <= 90;
    }

    public static double[] latLngToMercator(LatLng point) {
        double x = point.getLongitude();
        double y = Math.log(Math.tan(Math.PI / 4 + point.getLatitude() / 2));
        return new double[] {x, y};
    }

    public static double[] sphericalToCartesian(LatLng point) {
        double latitude = point.getLatitude();
        double longitude = point.getLongitude();
        double radius = 6378.1; // average radius of the Earth in kilometers
        double x = radius * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(longitude));
        double y = radius * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(longitude));
        double z = radius * Math.sin(Math.toRadians(latitude));
        return new double[]{x, y, z};
    }

    public static LatLng cartesianToLatLng(double x, double y) {
        double radius = 6378.1; // average radius of the Earth in kilometers
        double z = Math.sqrt(radius * radius - x * x - y * y);
        double latitude = Math.toDegrees(Math.asin(z / radius));
        double longitude = Math.toDegrees(Math.atan2(y, x));
        return new LatLng(latitude, longitude);
    }
    
    public static LatLng getAverageCenter(ArrayList<LatLng> points) {
        double latSum = 0;
        double lngSum = 0;
        int count = 0;

        for (LatLng point : points) {
            latSum += point.getLatitude();
            lngSum += point.getLongitude();
            count++;
        }

        if (count == 0) {
            return null;
        }

        double latAvg = latSum / count;
        double lngAvg = lngSum / count;

        return new LatLng(latAvg, lngAvg);
    }
    
    /**
     * Bearing (angular) from one point to another
     */
    public static double bearingBetween(LatLng position1, LatLng position2){
        double lat1Rad = Math.toRadians(position1.getLatitude());
        double lat2Rad = Math.toRadians(position2.getLatitude());
        double lng1 = position1.getLongitude();
        double lng2 = position2.getLongitude();

        double lngDiffRad = Math.toRadians(lng2-lng1);
        double y = Math.sin(lngDiffRad)*Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad)*Math.sin(lat2Rad)-Math.sin(lat1Rad)*Math.cos(lat2Rad)*Math.cos(lngDiffRad);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public static boolean isObjectAhead(LatLng selfPosition, float selfHeading, LatLng objectPosition, int fieldOfView, int maxDistance) {
        if(fieldOfView >= 360) fieldOfView = 358;
        int angleLeftAction = 360-(fieldOfView/2);
        int angleRightAction = fieldOfView/2;
        if(distanceBetweenLatLng(objectPosition, selfPosition) < maxDistance){
            double bearingToAction = ((bearingBetween(selfPosition, objectPosition)
                    - selfHeading+360)%360);
            if(bearingToAction >= angleLeftAction || bearingToAction <= angleRightAction) { // action is ahead
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    public static String unixMsToISO8601(long unixTimestampMillis) {
        Date date = new Date(unixTimestampMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
    
    public static double degreesToMeters(double degrees, double latitude) {
        // The Earth's radius at the given latitude (using WGS 84 ellipsoid)
        double radius = 6378137.0; // in meters
        return degrees * (Math.PI / 180) * radius;
    }
    
    public static String getTimeFromTimestamp(long unixTimestampMs, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(unixTimestampMs);
        return simpleDateFormat.format(date);
    }

    public static double calculateLongitudinalAcceleration(float currentSpeed, float newSpeed,
                                                           long currentTime, long newTime) {
        // Get time difference in milliseconds and convert to seconds
        long timeDifferenceMillis = newTime - currentTime;
        double timeDifferenceSeconds = timeDifferenceMillis / 1000.0;

        // Calculate acceleration (m/s^2)
        return  (newSpeed - currentSpeed) / timeDifferenceSeconds;
    }

    public static double calculateYawRate(float currentHeading, float newHeading,
                                          long currentTime, long newTime) {
        // Calculate heading difference (in degrees), handling wraparound
        float headingDifference = newHeading - currentHeading;
        if (headingDifference > 180) {
            headingDifference -= 360;
        } else if (headingDifference < -180) {
            headingDifference += 360;
        }

        // Get time difference in milliseconds and convert to seconds
        long timeDifferenceMillis = newTime - currentTime;
        double timeDifferenceSeconds = timeDifferenceMillis / 1000.0;

        // Calculate yaw rate (degrees per second)
        return headingDifference / timeDifferenceSeconds;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static float normalizeAngle(float heading) {
        return (heading % 360 + 360) % 360;
    }

}
