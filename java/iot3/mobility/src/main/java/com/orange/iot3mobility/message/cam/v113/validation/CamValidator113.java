package com.orange.iot3mobility.message.cam.v113.validation;

import com.orange.iot3mobility.message.cam.v113.model.*;

import java.util.List;
import java.util.Objects;

public final class CamValidator113 {

    private CamValidator113() {}

    public static void validateEnvelope(CamEnvelope113 env) {
        requireEquals("type", env.type(), "cam");
        requireEnum("origin", env.origin(),
                List.of("self", "global_application", "mec_application", "on_board_application"));
        requireEquals("version", env.version(), "1.1.3");
        requireNotBlank("source_uuid", env.sourceUuid());
        checkRange("timestamp", env.timestamp(), 1514764800000L, 1830297600000L);
        validateMessage(env.message());
    }

    public static void validateMessage(CamMessage113 msg) {
        requireNonNull("message", msg);
        checkRange("protocol_version", msg.protocolVersion(), 0, 255);
        checkRange("station_id", msg.stationId(), 0, 4294967295L);
        checkRange("generation_delta_time", msg.generationDeltaTime(), 0, 65535);
        validateBasic(msg.basicContainer());
        validateHighFrequency(msg.highFrequencyContainer());
        validateLowFrequency(msg.lowFrequencyContainer());
    }

    private static void validateBasic(BasicContainer basic) {
        requireNonNull("basic_container", basic);
        checkRange("station_type", basic.stationType(), 0, 255);
        ReferencePosition ref = requireNonNull("basic_container.reference_position", basic.referencePosition());
        checkRange("latitude", ref.latitude(), -900000000, 900000001);
        checkRange("longitude", ref.longitude(), -1800000000, 1800000001);
        checkRange("altitude", ref.altitude(), -100000, 800001);

        PositionConfidence conf = basic.confidence();
        if(conf != null) {
            PositionConfidenceEllipse ellipse = requireNonNull(
                    "basic_container.confidence.position_confidence_ellipse",
                    conf.ellipse());
            checkRange("semi_major_confidence", ellipse.semiMajor(), 0, 4095);
            checkRange("semi_minor_confidence", ellipse.semiMinor(), 0, 4095);
            checkRange("semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
            checkRange("altitude_confidence", conf.altitude(), 0, 15);
        }
    }

    private static void validateHighFrequency(HighFrequencyContainer hf) {
        requireNonNull("high_frequency_container", hf);
        checkRange("heading", hf.heading(), 0, 3601);
        checkRange("speed", hf.speed(), 0, 16383);
        checkRange("drive_direction", hf.driveDirection(), 0, 2);
        checkRange("vehicle_length", hf.vehicleLength(), 1, 1023);
        checkRange("vehicle_width", hf.vehicleWidth(), 1, 62);
        checkRange("curvature", hf.curvature(), -1023, 1023);
        checkRange("curvature_calculation_mode", hf.curvatureCalculationMode(), 0, 2);
        checkRange("longitudinal_acceleration", hf.longitudinalAcceleration(), -160, 161);
        checkRange("yaw_rate", hf.yawRate(), -32766, 32767);

        if (hf.accelerationControl() != null && !hf.accelerationControl().isBlank()
                && !hf.accelerationControl().matches("[01]{7}")) {
            throw new CamValidationException("acceleration_control must be 7-bit binary string");
        }
        checkRange("lane_position", hf.lanePosition(), -1, 14);
        checkRange("lateral_acceleration", hf.lateralAcceleration(), -160, 161);
        checkRange("vertical_acceleration", hf.verticalAcceleration(), -160, 161);
        validateHighFrequencyConfidence(hf.confidence());
    }

    private static void validateHighFrequencyConfidence(HighFrequencyConfidence conf) {
        if(conf != null) {
            checkRange("confidence.heading", conf.heading(), 1, 127);
            checkRange("confidence.speed", conf.speed(), 1, 127);
            checkRange("confidence.vehicle_length", conf.vehicleLength(), 0, 4);
            checkRange("confidence.yaw_rate", conf.yawRate(), 0, 8);
            checkRange("confidence.longitudinal_acceleration", conf.longitudinalAcceleration(), 0, 102);
            checkRange("confidence.curvature", conf.curvature(), 0, 7);
            checkRange("confidence.lateral_acceleration", conf.lateralAcceleration(), 0, 102);
            checkRange("confidence.vertical_acceleration", conf.verticalAcceleration(), 0, 102);
        }
    }

    private static void validateLowFrequency(LowFrequencyContainer lf) {
        if(lf != null) {
            checkRange("vehicle_role", lf.vehicleRole(), 0, 15);
            requireNotBlank("exterior_lights", lf.exteriorLights());
            if (!lf.exteriorLights().matches("[01]{8}")) {
                throw new CamValidationException("exterior_lights must be 8-bit binary string");
            }
            List<PathPoint> history = requireNonNull("path_history", lf.pathHistory());
            if (history.size() > 40) {
                throw new CamValidationException("path_history size exceeds 40");
            }
            for (int i = 0; i < history.size(); i++) {
                validatePathPoint(history.get(i), i);
            }
        }
    }

    private static void validatePathPoint(PathPoint point, int index) {
        String prefix = "path_history[" + index + "]";
        requireNonNull(prefix, point);
        DeltaReferencePosition delta = requireNonNull(prefix + ".path_position", point.deltaPosition());
        checkRange(prefix + ".delta_latitude", delta.deltaLatitude(), -131071, 131072);
        checkRange(prefix + ".delta_longitude", delta.deltaLongitude(), -131071, 131072);
        checkRange(prefix + ".delta_altitude", delta.deltaAltitude(), -12700, 12800);
        if (point.deltaTime() != null) {
            checkRange(prefix + ".path_delta_time", point.deltaTime(), 1, 65535);
        }
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CamValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void requireEnum(String field, String actual, List<String> allowed) {
        if (!allowed.contains(actual)) {
            throw new CamValidationException(field + " must be one of " + allowed);
        }
    }

    private static void checkRange(String field, Integer value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CamValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, Long value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new CamValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }
}
