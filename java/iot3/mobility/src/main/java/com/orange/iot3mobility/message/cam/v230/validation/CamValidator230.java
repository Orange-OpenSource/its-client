/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.validation;

import com.orange.iot3mobility.message.cam.v230.model.*;
import com.orange.iot3mobility.message.cam.v230.model.basiccontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer.*;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Static validation utility for CAM 2.3.0 envelopes (structured JSON or ASN.1 payloads).
 */
public final class CamValidator230 {

    private static final long MIN_TIMESTAMP = 1514764800000L;   // 2018-01-01
    private static final Set<String> MESSAGE_FORMATS = Set.of("json/raw", "asn1/uper");

    private CamValidator230() {
    }

    public static void validateEnvelope(CamEnvelope230 envelope) {
        requireNonNull("envelope", envelope);
        requireEquals("message_type", envelope.messageType(), "cam");
        if (envelope.messageFormat() != null && !MESSAGE_FORMATS.contains(envelope.messageFormat())) {
            throw new CamValidationException("message_format must be one of " + MESSAGE_FORMATS);
        }
        requireNotBlank("source_uuid", envelope.sourceUuid());
        checkMin("timestamp", envelope.timestamp(), MIN_TIMESTAMP);
        requireEquals("version", envelope.version(), "2.3.0");

        CamMessage230 payload = requireNonNull("message", envelope.message());
        if (payload instanceof CamStructuredData structured) {
            if (envelope.messageFormat() != null && !"json/raw".equals(envelope.messageFormat())) {
                throw new CamValidationException("message_format must be 'json/raw' for structured payloads");
            }
            validateStructured(structured);
        } else if (payload instanceof CamAsn1Payload asn1) {
            if (envelope.messageFormat() != null && !"asn1/uper".equals(envelope.messageFormat())) {
                throw new CamValidationException("message_format must be 'asn1/uper' for ASN.1 payloads");
            }
            validateAsn1(asn1);
        } else {
            throw new CamValidationException("Unsupported CAM payload type: " + payload.getClass().getName());
        }
    }

    /* ------------------------------------------------------------------------
       Structured payload
       --------------------------------------------------------------------- */

    private static void validateStructured(CamStructuredData message) {
        requireNonNull("structured_message", message);
        checkRange("protocol_version", message.protocolVersion(), 0, 255);
        checkRange("station_id", message.stationId(), 0, 4294967295L);
        checkRange("generation_delta_time", message.generationDeltaTime(), 0, 65535);

        validateBasicContainer(message.basicContainer());
        validateHighFrequencyContainer(message.highFrequencyContainer());

        if (message.lowFrequencyContainer() != null) {
            validateLowFrequencyContainer(message.lowFrequencyContainer());
        }
        if (message.specialVehicleContainer() != null) {
            validateSpecialVehicleContainer(message.specialVehicleContainer());
        }
    }

    private static void validateBasicContainer(BasicContainer basic) {
        requireNonNull("basic_container", basic);
        checkRange("basic_container.station_type", basic.stationType(), 0, 255);
        validateReferencePosition(basic.referencePosition());
    }

    private static void validateReferencePosition(ReferencePosition reference) {
        requireNonNull("reference_position", reference);
        checkRange("reference_position.latitude", reference.latitude(), -900000000, 900000001);
        checkRange("reference_position.longitude", reference.longitude(), -1800000000, 1800000001);
        validatePositionConfidenceEllipse("reference_position.position_confidence_ellipse",
                reference.positionConfidenceEllipse());
        validateAltitude("reference_position.altitude", reference.altitude());
    }

    private static void validatePositionConfidenceEllipse(String prefix, PositionConfidenceEllipse ellipse) {
        requireNonNull(prefix, ellipse);
        checkRange(prefix + ".semi_major", ellipse.semiMajor(), 0, 4095);
        checkRange(prefix + ".semi_minor", ellipse.semiMinor(), 0, 4095);
        checkRange(prefix + ".semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
    }

    private static void validateAltitude(String prefix, Altitude altitude) {
        requireNonNull(prefix, altitude);
        checkRange(prefix + ".value", altitude.value(), -100000, 800001);
        checkRange(prefix + ".confidence", altitude.confidence(), 0, 15);
    }

    private static void validateHighFrequencyContainer(HighFrequencyContainer container) {
        requireNonNull("high_frequency_container", container);
        if (container instanceof BasicVehicleContainerHighFrequency basicVehicle) {
            validateBasicVehicleHF(basicVehicle);
        } else if (container instanceof RsuContainerHighFrequency rsu) {
            validateRsuHF(rsu);
        } else {
            throw new CamValidationException("Unknown high frequency container type: " + container.getClass().getName());
        }
    }

    private static void validateBasicVehicleHF(BasicVehicleContainerHighFrequency container) {
        validateHeading("high_frequency_container.heading", container.heading());
        validateSpeed("high_frequency_container.speed", container.speed());
        checkRange("high_frequency_container.drive_direction", container.driveDirection(), 0, 2);
        validateVehicleLength("high_frequency_container.vehicle_length", container.vehicleLength());
        checkRange("high_frequency_container.vehicle_width", container.vehicleWidth(), 1, 62);
        validateAccelerationComponent("high_frequency_container.longitudinal_acceleration", container.longitudinalAcceleration());
        validateCurvature("high_frequency_container.curvature", container.curvature());
        checkRange("high_frequency_container.curvature_calculation_mode", container.curvatureCalculationMode(), 0, 2);
        validateYawRate("high_frequency_container.yaw_rate", container.yawRate());

        if (container.lanePosition() != null) {
            checkRange("high_frequency_container.lane_position", container.lanePosition(), -1, 14);
        }
        if (container.steeringWheelAngle() != null) {
            validateSteeringWheelAngle("high_frequency_container.steering_wheel_angle", container.steeringWheelAngle());
        }
        if (container.lateralAcceleration() != null) {
            validateAccelerationComponent("high_frequency_container.lateral_acceleration", container.lateralAcceleration());
        }
        if (container.verticalAcceleration() != null) {
            validateAccelerationComponent("high_frequency_container.vertical_acceleration", container.verticalAcceleration());
        }
        if (container.performanceClass() != null) {
            checkRange("high_frequency_container.performance_class", container.performanceClass(), 0, 7);
        }
        if (container.cenDsrcTollingZone() != null) {
            validateCenDsrcTollingZone(container.cenDsrcTollingZone());
        }
    }

    private static void validateRsuHF(RsuContainerHighFrequency container) {
        List<ProtectedCommunicationZone> zones =
                requireNonNull("rsu_container_high_frequency.protected_communication_zones_rsu",
                        container.protectedCommunicationZonesRsu());
        int size = zones.size();
        if (size < 1 || size > 16) {
            throw new CamValidationException("protected_communication_zones_rsu size out of range [1,16]: " + size);
        }
        for (int i = 0; i < zones.size(); i++) {
            validateProtectedCommunicationZone("protected_communication_zones_rsu[" + i + "]", zones.get(i));
        }
    }

    private static void validateProtectedCommunicationZone(String prefix, ProtectedCommunicationZone zone) {
        requireNonNull(prefix, zone);
        checkRange(prefix + ".protected_zone_type", zone.protectedZoneType(), 0, 1);
        if (zone.expiryTime() != null) {
            checkRange(prefix + ".expiry_time", zone.expiryTime(), 0, 4398046511103L);
        }
        checkRange(prefix + ".protected_zone_latitude", zone.protectedZoneLatitude(), -900000000, 900000001);
        checkRange(prefix + ".protected_zone_longitude", zone.protectedZoneLongitude(), -1800000000, 1800000001);
        if (zone.protectedZoneRadius() != null) {
            checkRange(prefix + ".protected_zone_radius", zone.protectedZoneRadius(), 1, 255);
        }
        if (zone.protectedZoneId() != null) {
            checkRange(prefix + ".protected_zone_id", zone.protectedZoneId(), 0, 134217727);
        }
    }

    private static void validateCenDsrcTollingZone(CenDsrcTollingZone zone) {
        requireNonNull("cen_dsrc_tolling_zone", zone);
        checkRange("cen_dsrc_tolling_zone.protected_zone_latitude", zone.protectedZoneLatitude(), -900000000, 900000001);
        checkRange("cen_dsrc_tolling_zone.protected_zone_longitude", zone.protectedZoneLongitude(), -1800000000, 1800000001);
        if (zone.cenDsrcTollingZoneId() != null) {
            checkRange("cen_dsrc_tolling_zone.cen_dsrc_tolling_zone_id", zone.cenDsrcTollingZoneId(), 0, 134217727);
        }
    }

    private static void validateLowFrequencyContainer(LowFrequencyContainer container) {
        BasicVehicleContainerLowFrequency low =
                requireNonNull("low_frequency_container.basic_vehicle_container_low_frequency",
                        container.basicVehicleContainerLowFrequency());
        checkRange("low_frequency_container.vehicle_role", low.vehicleRole(), 0, 15);
        requireNonNull("low_frequency_container.exterior_lights", low.exteriorLights());

        List<PathPoint> history = requireNonNull("low_frequency_container.path_history", low.pathHistory());
        if (history.size() > 40) {
            throw new CamValidationException("path_history size exceeds 40");
        }
        for (int i = 0; i < history.size(); i++) {
            validatePathPoint("path_history[" + i + "]", history.get(i));
        }
    }

    private static void validatePathPoint(String prefix, PathPoint pathPoint) {
        requireNonNull(prefix, pathPoint);
        validateDeltaReferencePosition(prefix + ".path_position", pathPoint.pathPosition());
        if (pathPoint.pathDeltaTime() != null) {
            checkRange(prefix + ".path_delta_time", pathPoint.pathDeltaTime(), 1, 65535);
        }
    }

    private static void validateDeltaReferencePosition(String prefix, DeltaReferencePosition delta) {
        requireNonNull(prefix, delta);
        checkRange(prefix + ".delta_latitude", delta.deltaLatitude(), -131071, 131072);
        checkRange(prefix + ".delta_longitude", delta.deltaLongitude(), -131071, 131072);
        checkRange(prefix + ".delta_altitude", delta.deltaAltitude(), -12700, 12800);
    }

    private static void validateSpecialVehicleContainer(SpecialVehicleContainer container) {
        SpecialVehiclePayload payload = requireNonNull("special_vehicle_container.payload", container.payload());
        if (payload instanceof PublicTransportContainer pt) {
            validatePublicTransport(pt);
        } else if (payload instanceof SpecialTransportContainer st) {
            validateLightBar("special_transport_container.light_bar_siren_in_use", st.lightBarSirenInUse());
        } else if (payload instanceof DangerousGoodsContainer dg) {
            checkRange("dangerous_goods_container.dangerous_goods_basic", dg.dangerousGoodsBasic(), 0, 19);
        } else if (payload instanceof RoadWorksContainer rw) {
            validateRoadWorks(rw);
        } else if (payload instanceof RescueContainer rescue) {
            validateLightBar("rescue_container.light_bar_siren_in_use", rescue.lightBarSirenInUse());
        } else if (payload instanceof EmergencyContainer emergency) {
            validateEmergency(emergency);
        } else if (payload instanceof SafetyCarContainer safetyCar) {
            validateSafetyCar(safetyCar);
        } else {
            throw new CamValidationException("Unknown special vehicle payload: " + payload.getClass().getName());
        }
    }

    private static void validatePublicTransport(PublicTransportContainer container) {
        requireNonNull("public_transport_container", container);
        if (container.ptActivation() != null) {
            PtActivation activation = container.ptActivation();
            checkRange("public_transport_container.pt_activation.pt_activation_type",
                    activation.ptActivationType(), 0, 255);
            checkStringLength("public_transport_container.pt_activation.pt_activation_data",
                    activation.ptActivationData(), 1, 20);
        }
    }

    private static void validateRoadWorks(RoadWorksContainer container) {
        validateLightBar("road_works_container.light_bar_siren_in_use", container.lightBarSirenInUse());
        if (container.roadWorksSubCauseCode() != null) {
            checkRange("road_works_container.road_works_sub_cause_code",
                    container.roadWorksSubCauseCode(), 0, 255);
        }
        if (container.closedLanes() != null) {
            ClosedLanes lanes = container.closedLanes();
            if (lanes.innerHardShoulderStatus() != null) {
                checkRange("road_works_container.closed_lanes.inner_hard_shoulder_status",
                        lanes.innerHardShoulderStatus(), 0, 2);
            }
            if (lanes.outerHardShoulderStatus() != null) {
                checkRange("road_works_container.closed_lanes.outer_hard_shoulder_status",
                        lanes.outerHardShoulderStatus(), 0, 2);
            }
        }
    }

    private static void validateEmergency(EmergencyContainer container) {
        validateLightBar("emergency_container.light_bar_siren_in_use", container.lightBarSirenInUse());
        if (container.incidentIndication() != null) {
            validateIncidentIndication("emergency_container.incident_indication", container.incidentIndication());
        }
    }

    private static void validateSafetyCar(SafetyCarContainer container) {
        validateLightBar("safety_car_container.light_bar_siren_in_use", container.lightBarSirenInUse());
        if (container.incidentIndication() != null) {
            validateIncidentIndication("safety_car_container.incident_indication", container.incidentIndication());
        }
        if (container.trafficRule() != null) {
            checkRange("safety_car_container.traffic_rule", container.trafficRule(), 0, 4);
        }
        if (container.speedLimit() != null) {
            checkRange("safety_car_container.speed_limit", container.speedLimit(), 1, 255);
        }
    }

    private static void validateIncidentIndication(String prefix, IncidentIndication indication) {
        requireNonNull(prefix, indication);
        validateCauseCode(prefix + ".cc_and_scc", indication.ccAndScc());
    }

    private static void validateCauseCode(String prefix, CauseCode causeCode) {
        requireNonNull(prefix, causeCode);
        checkRange(prefix + ".cause", causeCode.cause(), 0, 255);
        if (causeCode.subcause() != null) {
            checkRange(prefix + ".subcause", causeCode.subcause(), 0, 255);
        }
    }

    private static void validateLightBar(String prefix, LightBarSiren lightBar) {
        requireNonNull(prefix, lightBar);
    }

    /* ------------------------------------------------------------------------
       ASN.1 payload branch
       --------------------------------------------------------------------- */

    private static void validateAsn1(CamAsn1Payload payload) {
        requireNotBlank("message.version", payload.version());
        String encoded = requireNotBlank("message.payload", payload.payload());
        try {
            Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new CamValidationException("message.payload must be valid Base64", ex);
        }
    }

    /* ------------------------------------------------------------------------
       Shared helpers
       --------------------------------------------------------------------- */

    private static void validateHeading(String prefix, Heading heading) {
        requireNonNull(prefix, heading);
        checkRange(prefix + ".value", heading.value(), 0, 3601);
        checkRange(prefix + ".confidence", heading.confidence(), 1, 127);
    }

    private static void validateSpeed(String prefix, Speed speed) {
        requireNonNull(prefix, speed);
        checkRange(prefix + ".value", speed.value(), 0, 16383);
        checkRange(prefix + ".confidence", speed.confidence(), 1, 127);
    }

    private static void validateVehicleLength(String prefix, VehicleLength length) {
        requireNonNull(prefix, length);
        checkRange(prefix + ".value", length.value(), 1, 1023);
        checkRange(prefix + ".confidence", length.confidence(), 0, 4);
    }

    private static void validateAccelerationComponent(String prefix, AccelerationComponent component) {
        requireNonNull(prefix, component);
        checkRange(prefix + ".value", component.value(), -160, 161);
        checkRange(prefix + ".confidence", component.confidence(), 0, 102);
    }

    private static void validateCurvature(String prefix, Curvature curvature) {
        requireNonNull(prefix, curvature);
        checkRange(prefix + ".value", curvature.value(), -1023, 1023);
        checkRange(prefix + ".confidence", curvature.confidence(), 0, 7);
    }

    private static void validateYawRate(String prefix, YawRate yawRate) {
        requireNonNull(prefix, yawRate);
        checkRange(prefix + ".value", yawRate.value(), -32766, 32767);
        checkRange(prefix + ".confidence", yawRate.confidence(), 0, 8);
    }

    private static void validateSteeringWheelAngle(String prefix, SteeringWheelAngle angle) {
        requireNonNull(prefix, angle);
        checkRange(prefix + ".value", angle.value(), -511, 512);
        checkRange(prefix + ".confidence", angle.confidence(), 1, 127);
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static String requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new CamValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void checkRange(String field, long value, long min, long max) {
        if (value < min || value > max) {
            throw new CamValidationException(field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkMin(String field, long value, long min) {
        if (value < min) {
            throw new CamValidationException(field + " inferior to min [" + min + "] (actual=" + value + ")");
        }
    }

    private static void checkStringLength(String field, String value, int min, int max) {
        requireNotBlank(field, value);
        int len = value.length();
        if (len < min || len > max) {
            throw new CamValidationException(field + " length out of range [" + min + ", " + max + "] (actual=" + len + ")");
        }
    }
}
