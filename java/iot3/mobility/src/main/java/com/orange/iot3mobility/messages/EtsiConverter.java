/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages;

/**
 * Utility methods to convert ETSI ITS units to/from SI units.
 * <p>
 * "ETSI" values are the encoded integer values from the message.
 * "SI" values are returned/taken in standard units (meters, seconds, radians, etc.).
 */
public final class EtsiConverter {

    private EtsiConverter() {
        // Utility class
    }

    // -------------------------------------------------------------------------
    // Time
    // -------------------------------------------------------------------------

    /**
     * SI -> ETSI
     * @param etsiTimestampMs ETSI timestamp in milliseconds
     * @return encoded value in milliseconds (clamped to 0..65535)
     */
    public static int generationDeltaTimeEtsi(long etsiTimestampMs) {
        return (int) (etsiTimestampMs & 0xFFFF); // equivalent to % 65536 but ensures positive result
    }

    /**
     * Convenience: absolute epoch ms -> seconds.
     */
    public static double epochMillisToSeconds(long epochMillis) {
        return epochMillis / 1000.0;
    }

    // -------------------------------------------------------------------------
    // Position (latitude / longitude / altitude)
    // -------------------------------------------------------------------------

    /**
     * Latitude in CAM: integer in 1e-7 degrees.
     * <p>
     * ETSI -> SI (degrees)
     */
    public static double latitudeDegrees(int latitudeEtsi) {
        return latitudeEtsi / 10_000_000.0;
    }

    /**
     * SI -> ETSI (1e-7 degrees units)
     */
    public static int latitudeEtsi(double latitudeDegrees) {
        long etsi = Math.round(latitudeDegrees * 10_000_000.0);
        if (etsi < -900000000L) etsi = -900000000L;
        if (etsi >  900000001L) etsi =  900000001L;
        return (int) etsi;
    }

    /**
     * Longitude in CAM: integer in 1e-7 degrees.
     * <p>
     * ETSI -> SI (degrees)
     */
    public static double longitudeDegrees(int longitudeEtsi) {
        return longitudeEtsi / 10_000_000.0;
    }

    /**
     * SI -> ETSI (1e-7 degrees units)
     */
    public static int longitudeEtsi(double longitudeDegrees) {
        long etsi = Math.round(longitudeDegrees * 10_000_000.0);
        if (etsi < -1800000000L) etsi = -1800000000L;
        if (etsi >  1800000001L) etsi =  1800000001L;
        return (int) etsi;
    }

    /**
     * Altitude in CAM (example):
     * value in [0..800001] with:
     *   altitudeMeters = value * 0.01
     * <p>
     * ETSI -> SI (meters)
     */
    public static double altitudeMeters(int altitudeEtsi) {
        return altitudeEtsi * 0.01;
    }

    /**
     * SI -> ETSI.
     * Inverse of altitudeMeters():
     *   value = altitudeMeters / 0.01
     */
    public static int altitudeEtsi(double altitudeMeters) {
        long etsi = Math.round((altitudeMeters) / 0.01);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 800001L) etsi = 800001L;
        return (int) etsi;
    }

    // -------------------------------------------------------------------------
    // Speed
    // -------------------------------------------------------------------------

    /**
     * Speed in CAM: 0..16383 in steps of 0.01 m/s.
     * <p>
     * ETSI -> SI (m/s)
     */
    public static double speedMetersPerSecond(int speedEtsi) {
        return speedEtsi * 0.01;
    }

    /**
     * Convenience: ETSI -> SI (km/h).
     */
    public static double speedKilometersPerHour(int speedEtsi) {
        return speedMetersPerSecond(speedEtsi) * 3.6;
    }

    /**
     * SI -> ETSI: from m/s.
     */
    public static int speedEtsiFromMetersPerSecond(double speedMps) {
        long etsi = Math.round(speedMps / 0.01);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 16383L) etsi = 16383L;
        return (int) etsi;
    }

    /**
     * SI -> ETSI: from km/h (convenience).
     */
    public static int speedEtsiFromKilometersPerHour(double speedKmh) {
        double mps = speedKmh / 3.6;
        return speedEtsiFromMetersPerSecond(mps);
    }

    // -------------------------------------------------------------------------
    // Heading / orientation
    // -------------------------------------------------------------------------

    /**
     * Heading in CAM: 0..3601 in steps of 0.1°
     *  - typical sentinel: 3600 or 3601 => unavailable.
     * <p>
     * ETSI -> SI (degrees). Returns NaN if sentinel.
     */
    public static double headingDegrees(int headingEtsi) {
        if (headingEtsi >= 3600) {
            return Double.NaN;
        }
        return headingEtsi * 0.1;
    }

    /**
     * ETSI -> SI (radians). Propagate NaN if unavailable.
     */
    public static double headingRadians(int headingEtsi) {
        double deg = headingDegrees(headingEtsi);
        return Double.isNaN(deg) ? Double.NaN : Math.toRadians(deg);
    }

    /**
     * SI -> ETSI (degrees). If degrees is NaN, return sentinel (3601).
     */
    public static int headingEtsiFromDegrees(double headingDegrees) {
        if (Double.isNaN(headingDegrees)) {
            return 3601; // sentinel for "unavailable" (to adapt if needed)
        }
        double normalized = ((headingDegrees % 360.0) + 360.0) % 360.0;
        long etsi = Math.round(normalized / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 3600L) etsi = 3600L;
        return (int) etsi;
    }

    /**
     * SI -> ETSI (radians).
     */
    public static int headingEtsiFromRadians(double headingRadians) {
        if (Double.isNaN(headingRadians)) {
            return 3601;
        }
        double deg = Math.toDegrees(headingRadians);
        return headingEtsiFromDegrees(deg);
    }

    // -------------------------------------------------------------------------
    // Vehicle dimensions
    // -------------------------------------------------------------------------

    /**
     * Vehicle length in CAM: steps of 0.1 m.
     *
     * ETSI -> SI (meters)
     */
    public static double vehicleLengthMeters(int lengthEtsi) {
        return lengthEtsi * 0.1;
    }

    /**
     * SI -> ETSI.
     */
    public static int vehicleLengthEtsi(double lengthMeters) {
        long etsi = Math.round(lengthMeters / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 1023L) etsi = 1023L;
        return (int) etsi;
    }

    /**
     * Vehicle width in CAM: steps of 0.1 m.
     * <p>
     * ETSI -> SI (meters)
     */
    public static double vehicleWidthMeters(int widthEtsi) {
        return widthEtsi * 0.1;
    }

    /**
     * SI -> ETSI.
     */
    public static int vehicleWidthEtsi(double widthMeters) {
        long etsi = Math.round(widthMeters / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 62L) etsi = 62L;
        return (int) etsi;
    }

    // -------------------------------------------------------------------------
    // Acceleration, curvature, yaw rate
    // -------------------------------------------------------------------------

    /**
     * Longitudinal / lateral / vertical acceleration in CAM:
     * steps of 0.1 m/s², signed.
     * <p>
     * ETSI -> SI (m/s²)
     */
    public static double accelerationMetersPerSecondSquared(int accelerationEtsi) {
        return accelerationEtsi * 0.1;
    }

    /**
     * SI -> ETSI.
     */
    public static int accelerationEtsi(double accelerationMps2) {
        long etsi = Math.round(accelerationMps2 / 0.1);
        if (etsi < -160L) etsi = -160L;
        if (etsi >  161L) etsi =  161L;
        return (int) etsi;
    }

    /**
     * Curvature in CAM (example): unit 1/30000 m⁻¹.
     * <p>
     * ETSI -> SI (1/m)
     */
    public static double curvaturePerMeter(int curvatureEtsi) {
        return curvatureEtsi / 30000.0;
    }

    /**
     * SI -> ETSI.
     */
    public static int curvatureEtsi(double curvaturePerMeter) {
        long etsi = Math.round(curvaturePerMeter * 30000.0);
        if (etsi < -1023L) etsi = -1023L;
        if (etsi >  1023L) etsi =  1023L;
        return (int) etsi;
    }

    /**
     * Yaw rate in CAM: steps of 0.01 deg/s, signed.
     * <p>
     * ETSI -> SI (rad/s)
     */
    public static double yawRateRadiansPerSecond(int yawRateEtsi) {
        double yawDegPerSec = yawRateEtsi * 0.01;
        return Math.toRadians(yawDegPerSec);
    }

    /**
     * SI -> ETSI.
     */
    public static int yawRateEtsiFromRadiansPerSecond(double yawRateRadPerSec) {
        double yawDegPerSec = Math.toDegrees(yawRateRadPerSec);
        return yawRateEtsiFromDegreesPerSecond(yawDegPerSec);
    }

    public static int yawRateEtsiFromDegreesPerSecond(double yawDegPerSec) {
        long etsi = Math.round(yawDegPerSec / 0.01);
        if (etsi < -32766L) etsi = -32766L;
        if (etsi >  32767L) etsi =  32767L;
        return (int) etsi;
    }

    // -------------------------------------------------------------------------
    // Delta positions (path history etc.)
    // -------------------------------------------------------------------------

    /**
     * Delta position (example): 0.1 m steps, signed.
     * <p>
     * ETSI -> SI (meters)
     */
    public static double deltaPositionMeters(int deltaEtsi) {
        return deltaEtsi * 0.1;
    }

    /**
     * SI -> ETSI.
     */
    public static int deltaPositionEtsi(double deltaMeters) {
        long etsi = Math.round(deltaMeters / 0.1);
        if (etsi < -131071L) etsi = -131071L;
        if (etsi >  131072L) etsi =  131072L;
        return (int) etsi;
    }

    /**
     * Delta altitude (example): 0.1 m steps, signed.
     * <p>
     * ETSI -> SI (meters)
     */
    public static double deltaAltitudeMeters(int deltaAltitudeEtsi) {
        return deltaAltitudeEtsi * 0.1;
    }

    /**
     * SI -> ETSI.
     */
    public static int deltaAltitudeEtsi(double deltaAltitudeMeters) {
        long etsi = Math.round(deltaAltitudeMeters / 0.1);
        if (etsi < -12700L) etsi = -12700L;
        if (etsi >  12800L) etsi =  12800L;
        return (int) etsi;
    }

    // -------------------------------------------------------------------------
    // Generic helpers
    // -------------------------------------------------------------------------

    /**
     * Degrees -> radians.
     */
    public static double degreesToRadians(double deg) {
        return Math.toRadians(deg);
    }

    /**
     * Radians -> degrees.
     */
    public static double radiansToDegrees(double rad) {
        return Math.toDegrees(rad);
    }

    /**
     * m/s -> km/h.
     */
    public static double metersPerSecondToKilometersPerHour(double mps) {
        return mps * 3.6;
    }

    /**
     * km/h -> m/s.
     */
    public static double kilometersPerHourToMetersPerSecond(double kmh) {
        return kmh / 3.6;
    }

    // -------------------------------------------------------------------------
    // CPM-specific helpers
    // -------------------------------------------------------------------------

    /**
     * CPM distance component in 0.01 m steps, signed (e.g. x_distance, y_distance, z_distance).
     * <p>
     * ETSI -> SI (meters)
     */
    public static double cpmDistanceMeters(int distanceEtsi) {
        return distanceEtsi * 0.01;
    }

    /**
     * SI -> ETSI for CPM distance components (0.01 m steps, signed).
     */
    public static int cpmDistanceEtsi(double distanceMeters) {
        long etsi = Math.round(distanceMeters / 0.01);
        if (etsi < -132768L) etsi = -132768L;
        if (etsi >  132767L) etsi =  132767L;
        return (int) etsi;
    }

    /**
     * CPM speed component in 0.01 m/s steps, signed (e.g. x_speed, y_speed, z_speed).
     * <p>
     * ETSI -> SI (m/s)
     */
    public static double cpmSpeedMetersPerSecond(int speedEtsi) {
        return speedEtsi * 0.01;
    }

    /**
     * CPM X and Y speed components in 0.01 m/s steps.
     * <p>
     * ETSI -> SI (m/s)
     */
    public static double cpmDerivedSpeedMetersPerSecond(int xSpeed, int ySpeed) {
        return Math.sqrt(Math.abs(xSpeed * xSpeed) + Math.abs(ySpeed * ySpeed)) * 0.01;
    }

    /**
     * SI -> ETSI for CPM speed components (0.01 m/s steps, signed).
     */
    public static int cpmSpeedEtsiFromMetersPerSecond(double speedMps) {
        long etsi = Math.round(speedMps / 0.01);
        if (etsi < -16383L) etsi = -16383L;
        if (etsi >  16383L) etsi =  16383L;
        return (int) etsi;
    }

    /**
     * CPM X and Y speed components in 0.01 m/s steps.
     */
    public static double cpmDerivedHeadingDegrees(int xSpeed, int ySpeed) {
        return (90 - (180 / Math.PI) * Math.atan2(ySpeed, xSpeed)) % 360;
    }

    /**
     * CPM attitude angle in 0.1 degree steps (roll/pitch/yaw). Sentinel: 3601 => unavailable.
     * <p>
     * ETSI -> SI (degrees). Returns NaN if sentinel.
     */
    public static double cpmAngleDegrees(int angleEtsi) {
        if (angleEtsi >= 3601) {
            return Double.NaN;
        }
        return angleEtsi * 0.1;
    }

    /**
     * SI -> ETSI for CPM attitude angles (0.1 degree steps). If NaN, returns sentinel 3601.
     */
    public static int cpmAngleEtsiFromDegrees(double angleDegrees) {
        if (Double.isNaN(angleDegrees)) {
            return 3601;
        }
        double normalized = ((angleDegrees % 360.0) + 360.0) % 360.0;
        long etsi = Math.round(normalized / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 3601L) etsi = 3601L;
        return (int) etsi;
    }

    /**
     * CPM angular rate in 0.01 deg/s steps, signed (roll/pitch/yaw rate).
     * <p>
     * ETSI -> SI (deg/s)
     */
    public static double cpmAngularRateDegreesPerSecond(int rateEtsi) {
        return rateEtsi * 0.01;
    }

    /**
     * ETSI -> SI (rad/s) for CPM angular rate.
     */
    public static double cpmAngularRateRadiansPerSecond(int rateEtsi) {
        return Math.toRadians(cpmAngularRateDegreesPerSecond(rateEtsi));
    }

    /**
     * SI -> ETSI for CPM angular rate (0.01 deg/s steps, signed).
     */
    public static int cpmAngularRateEtsiFromDegreesPerSecond(double rateDegPerSec) {
        long etsi = Math.round(rateDegPerSec / 0.01);
        if (etsi < -32766L) etsi = -32766L;
        if (etsi >  32767L) etsi =  32767L;
        return (int) etsi;
    }

    /**
     * SI -> ETSI for CPM angular rate (rad/s).
     */
    public static int cpmAngularRateEtsiFromRadiansPerSecond(double rateRadPerSec) {
        return cpmAngularRateEtsiFromDegreesPerSecond(Math.toDegrees(rateRadPerSec));
    }

    /**
     * CPM angular acceleration in 0.01 deg/s^2 steps, signed (roll/pitch/yaw acceleration).
     * <p>
     * ETSI -> SI (deg/s^2)
     */
    public static double cpmAngularAccelerationDegreesPerSecondSquared(int accelEtsi) {
        return accelEtsi * 0.01;
    }

    /**
     * ETSI -> SI (rad/s^2) for CPM angular acceleration.
     */
    public static double cpmAngularAccelerationRadiansPerSecondSquared(int accelEtsi) {
        return Math.toRadians(cpmAngularAccelerationDegreesPerSecondSquared(accelEtsi));
    }

    /**
     * SI -> ETSI for CPM angular acceleration (0.01 deg/s^2 steps, signed).
     */
    public static int cpmAngularAccelerationEtsiFromDegreesPerSecondSquared(double accelDegPerSec2) {
        long etsi = Math.round(accelDegPerSec2 / 0.01);
        if (etsi < -32766L) etsi = -32766L;
        if (etsi >  32767L) etsi =  32767L;
        return (int) etsi;
    }

    /**
     * SI -> ETSI for CPM angular acceleration (rad/s^2).
     */
    public static int cpmAngularAccelerationEtsiFromRadiansPerSecondSquared(double accelRadPerSec2) {
        return cpmAngularAccelerationEtsiFromDegreesPerSecondSquared(Math.toDegrees(accelRadPerSec2));
    }

    /**
     * CPM object dimensions in 0.1 m steps (planar and vertical dimensions).
     * <p>
     * ETSI -> SI (meters)
     */
    public static double cpmObjectDimensionMeters(int dimensionEtsi) {
        return dimensionEtsi * 0.1;
    }

    /**
     * SI -> ETSI for CPM object dimensions (0.1 m steps).
     */
    public static int cpmObjectDimensionEtsi(double dimensionMeters) {
        long etsi = Math.round(dimensionMeters / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 1023L) etsi = 1023L;
        return (int) etsi;
    }

    /**
     * CPM longitudinal lane position in 0.1 m steps.
     * <p>
     * ETSI -> SI (meters)
     */
    public static double cpmLanePositionMeters(int lanePositionEtsi) {
        return lanePositionEtsi * 0.1;
    }

    /**
     * SI -> ETSI for CPM longitudinal lane position (0.1 m steps).
     */
    public static int cpmLanePositionEtsi(double lanePositionMeters) {
        long etsi = Math.round(lanePositionMeters / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 32767L) etsi = 32767L;
        return (int) etsi;
    }

    /**
     * CPM offsets and sensor positions in 0.01 m steps, signed (Offset.x/y/z, sensor offsets).
     * <p>
     * ETSI -> SI (meters)
     */
    public static double cpmOffsetMeters(int offsetEtsi) {
        return offsetEtsi * 0.01;
    }

    /**
     * SI -> ETSI for CPM offsets (0.01 m steps, signed).
     */
    public static int cpmOffsetEtsi(double offsetMeters) {
        long etsi = Math.round(offsetMeters / 0.01);
        if (etsi < -32768L) etsi = -32768L;
        if (etsi >  32767L) etsi =  32767L;
        return (int) etsi;
    }

    /**
     * CPM range and radius in 0.1 m steps (sensor ranges and area dimensions).
     * <p>
     * ETSI -> SI (meters)
     */
    public static double cpmRangeMeters(int rangeEtsi) {
        return rangeEtsi * 0.1;
    }

    /**
     * SI -> ETSI for CPM ranges and radii (0.1 m steps).
     */
    public static int cpmRangeEtsi(double rangeMeters) {
        long etsi = Math.round(rangeMeters / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 10000L) etsi = 10000L;
        return (int) etsi;
    }

    /**
     * CPM opening angles in 0.1 degree steps. Sentinel: 3601 => unavailable.
     * <p>
     * ETSI -> SI (degrees). Returns NaN if sentinel.
     */
    public static double cpmOpeningAngleDegrees(int angleEtsi) {
        if (angleEtsi >= 3601) {
            return Double.NaN;
        }
        return angleEtsi * 0.1;
    }

    /**
     * SI -> ETSI for CPM opening angles (0.1 degree steps). If NaN, returns sentinel 3601.
     */
    public static int cpmOpeningAngleEtsiFromDegrees(double angleDegrees) {
        if (Double.isNaN(angleDegrees)) {
            return 3601;
        }
        long etsi = Math.round(angleDegrees / 0.1);
        if (etsi < 0L) etsi = 0L;
        if (etsi > 3601L) etsi = 3601L;
        return (int) etsi;
    }

    /**
     * CPM correlation coefficient scaled by 100 (range -100..100).
     * <p>
     * ETSI -> SI (coefficient in [-1.0..1.0])
     */
    public static double cpmCorrelationCoefficient(int correlationEtsi) {
        return correlationEtsi / 100.0;
    }

    /**
     * SI -> ETSI for CPM correlation coefficient scaled by 100.
     */
    public static int cpmCorrelationEtsiFromCoefficient(double coefficient) {
        long etsi = Math.round(coefficient * 100.0);
        if (etsi < -100L) etsi = -100L;
        if (etsi >  100L) etsi =  100L;
        return (int) etsi;
    }
}
