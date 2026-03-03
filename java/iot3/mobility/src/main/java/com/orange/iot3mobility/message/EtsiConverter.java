package com.orange.iot3mobility.message;

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
}