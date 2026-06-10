/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.validation;

import com.orange.iot3mobility.messages.mcm.v200.model.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.*;

import java.util.List;
import java.util.Objects;

/**
 * Validates MCM v2.0.0 envelopes and sub-structures against the schema constraints.
 */
public final class McmValidator200 {

    private static final List<String> VALID_MESSAGE_FORMATS = List.of("json/raw", "asn1/uper");

    private static final List<String> VALID_CURRENT_STATE_ADVISED_CHANGES = List.of(
            "transit_to_human_driving_mode",
            "transit_to_automated_driving_mode",
            "leave_group",
            "emergency_brake_triggering",
            "stay_in_lane",
            "stop",
            "reset_stop",
            "reset_stay_in_lane",
            "stay_away_of_vehicle_sith_station_id",
            "reset_stay_away_of_vehicle",
            "follow_me_with_min_time_inter_distance",
            "join_group");

    private McmValidator200() {}

    public static void validateEnvelope(McmEnvelope200 envelope) {
        requireEquals("message_type", envelope.messageType(), "mcm");
        requireEnum("message_format", envelope.messageFormat(), VALID_MESSAGE_FORMATS);
        requireNotBlank("source_uuid", envelope.sourceUuid());
        checkRange("timestamp", envelope.timestamp(), 1514764800000L, 1830297600000L);
        requireEquals("version", envelope.version(), "2.0.0");
        validateMessage(requireNonNull("message", envelope.message()));
    }

    private static void validateMessage(McmMessage200 message) {
        checkRange("protocol_version", message.protocolVersion(), 0, 255);
        checkRange("station_id", message.stationId(), 0, 4294967295L);
        checkRange("generation_delta_time", message.generationDeltaTime(), 0, 65535);
        checkRange("station_type", message.stationType(), 0, 3);
        checkRange("itss_role", message.itssRole(), 0, 3);
        validateReferencePosition("position", requireNonNull("position", message.position()));
        validateMcmData(requireNonNull("mcm_data", message.mcmData()));
    }

    private static void validateReferencePosition(String prefix, ReferencePosition position) {
        checkRange(prefix + ".latitude", position.latitude(), -900000000, 900000001);
        checkRange(prefix + ".longitude", position.longitude(), -1800000000, 1800000001);
        PositionConfidenceEllipse ellipse = requireNonNull(
                prefix + ".position_confidence_ellipse", position.positionConfidenceEllipse());
        checkRange(prefix + ".semi_major_confidence", ellipse.semiMajorConfidence(), 0, 4095);
        checkRange(prefix + ".semi_minor_confidence", ellipse.semiMinorConfidence(), 0, 4095);
        checkRange(prefix + ".semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
        if (position.altitude() != null) {
            checkRange(prefix + ".altitude_value", position.altitude().altitudeValue(), -100000, 800001);
            checkRange(prefix + ".altitude_confidence", position.altitude().altitudeConfidence(), 0, 15);
        }
    }

    private static void validateMcmData(McmData mcmData) {
        boolean hasVehicle = mcmData.vehicleManoeuvreContainer() != null;
        boolean hasAdvised = mcmData.advisedManoeuvreContainer() != null;
        if (!hasVehicle && !hasAdvised) {
            throw new McmValidationException(
                    "mcm_data must contain vehicle_manoeuvre_container or advised_manoeuvre_container");
        }
        if (hasVehicle && hasAdvised) {
            throw new McmValidationException(
                    "mcm_data must contain only one of vehicle_manoeuvre_container or advised_manoeuvre_container");
        }
        if (hasVehicle) {
            validateVehicleManoeuvreContainer(mcmData.vehicleManoeuvreContainer());
        } else {
            validateAdviceList("advised_manoeuvre_container", mcmData.advisedManoeuvreContainer());
        }
    }

    private static void validateVehicleManoeuvreContainer(VehicleManoeuvreContainer container) {
        validateGenericState(requireNonNull(
                "vehicle_manoeuvre_container.mcm_generic_current_state_container",
                container.mcmGenericCurrentStateContainer()));
        validateVehicleCurrentState(requireNonNull(
                "vehicle_manoeuvre_container.vehicle_current_state_container",
                container.vehicleCurrentStateContainer()));
        List<Submanoeuvre> submaneuvres = requireNonNull(
                "vehicle_manoeuvre_container.submaneuvres", container.submaneuvres());
        for (int i = 0; i < submaneuvres.size(); i++) {
            validateSubmanoeuvre(submaneuvres.get(i), "submaneuvres[" + i + "]");
        }
        if (container.manoeuvreAdvice() != null) {
            validateAdviceList("manoeuvre_advice", container.manoeuvreAdvice());
        }
    }

    private static void validateGenericState(McmGenericCurrentStateContainer state) {
        checkRange("mcm_type", state.mcmType(), 0, 8);
        checkRange("manoeuvre_id", state.manoeuvreId(), 0, 255);
        checkRange("concept", state.concept(), 0, 1);
        if (state.rational() != null) {
            Rational rational = state.rational();
            if (rational.manoeuvreCooperationGoal() != null) {
                checkRange("rational.manoeuvre_cooperation_goal",
                        rational.manoeuvreCooperationGoal(), 0, 6);
            }
            if (rational.manoeuvreCooperationCost() != null) {
                checkRange("rational.manoeuvre_cooperation_cost",
                        rational.manoeuvreCooperationCost(), -1000, 1000);
            }
        }
        if (state.executionStatus() != null) {
            checkRange("execution_status", state.executionStatus(), 0, 4);
        }
    }

    private static void validateVehicleCurrentState(VehicleCurrentStateContainer state) {
        requireNonNull("vehicle_current_state_container.manoeuvre_overall_strategy",
                state.manoeuvreOverallStrategy());
        validateSpeed("vehicle_speed", requireNonNull("vehicle_speed", state.vehicleSpeed()));
        validateWgs84Angle("vehicle_heading", requireNonNull("vehicle_heading", state.vehicleHeading()));
        validateVehicleSize(requireNonNull("vehicle_size", state.vehicleSize()));
    }

    private static void validateSpeed(String prefix, Speed speed) {
        checkRange(prefix + ".speed_value", speed.speedValue(), 0, 16383);
        checkRange(prefix + ".speed_confidence", speed.speedConfidence(), 1, 127);
    }

    private static void validateWgs84Angle(String prefix, Wgs84Angle angle) {
        checkRange(prefix + ".value", angle.value(), 0, 3601);
        checkRange(prefix + ".confidence", angle.confidence(), 1, 127);
    }

    private static void validateVehicleSize(VehicleSize size) {
        checkRange("vehicle_type", size.vehicleType(), 0, 255);
        VehicleLength vehicleLength = requireNonNull("vehicle_lenth", size.vehicleLength());
        checkRange("vehicle_length_value", vehicleLength.vehicleLengthValue(), 1, 1023);
        checkRange("vehicle_length_confidence_indication",
                vehicleLength.vehicleLengthConfidenceIndication(), 0, 4);
        checkRange("vehicle_width", size.vehicleWidth(), 1, 62);
        checkRange("vehicle_height", size.vehicleHeight(), 1, 128);
    }

    private static void validateSubmanoeuvre(Submanoeuvre submanoeuvre, String prefix) {
        checkRange(prefix + ".submanoeuvre_id", submanoeuvre.submanoeuvreId(), 0, 255);
        requireNonNull(prefix + ".temporal_charateristics", submanoeuvre.temporalCharacteristics());
        checkRange(prefix + ".trr_occupancy_start_time",
                submanoeuvre.temporalCharacteristics().trrOccupancyStartTime(), 0, 65535);
        checkRange(prefix + ".trr_occupancy_end_time",
                submanoeuvre.temporalCharacteristics().trrOccupancyEndTime(), 0, 65535);
        requireNonNull(prefix + ".kinematics_characteristics", submanoeuvre.kinematicsCharacteristics());
        if (submanoeuvre.referenceTrajectory() != null) {
            List<WayPoint> trajectory = submanoeuvre.referenceTrajectory();
            if (trajectory.isEmpty() || trajectory.size() > 10) {
                throw new McmValidationException(
                        prefix + ".reference_trajectory size must be in [1..10]");
            }
            for (int i = 0; i < trajectory.size(); i++) {
                validateWayPoint(trajectory.get(i), prefix + ".reference_trajectory[" + i + "]");
            }
        }
    }

    private static void validateAdviceList(String prefix, List<ManoeuvreAdvice> adviceList) {
        for (int i = 0; i < adviceList.size(); i++) {
            validateManoeuvreAdvice(adviceList.get(i), prefix + "[" + i + "]");
        }
    }

    private static void validateManoeuvreAdvice(ManoeuvreAdvice advice, String prefix) {
        checkRange(prefix + ".executant_id", advice.executantId(), 0, 4294967295L);
        requireEnum(prefix + ".current_state_advised_change",
                advice.currentStateAdvisedChange(), VALID_CURRENT_STATE_ADVISED_CHANGES);
        requireNonNull(prefix + ".submaneuvres", advice.submaneuvres());
    }

    private static void validateWayPoint(WayPoint wayPoint, String prefix) {
        checkRange(prefix + ".way_point_type", wayPoint.wayPointType(), 0, 2);
        checkRange(prefix + ".longitude", wayPoint.longitude(), -1800000000, 1800000001);
        checkRange(prefix + ".latitude", wayPoint.latitude(), -900000000, 900000001);
        if (wayPoint.altitude() != null) {
            checkRange(prefix + ".altitude", wayPoint.altitude(), -100000, 800001);
        }
        if (wayPoint.heading() != null) {
            validateWgs84Angle(prefix + ".heading", wayPoint.heading());
        }
        checkRange(prefix + ".speed", wayPoint.speed(), 0, 511);
    }

    // ---- helpers ----

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new McmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new McmValidationException("Missing mandatory field: " + field);
        }
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new McmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void requireEnum(String field, String actual, List<String> allowed) {
        if (!allowed.contains(actual)) {
            throw new McmValidationException(field + " must be one of " + allowed);
        }
    }

    private static void checkRange(String field, int value, long min, long max) {
        if (value < min || value > max) {
            throw new McmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, long value, long min, long max) {
        if (value < min || value > max) {
            throw new McmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, Integer value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new McmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }
}

