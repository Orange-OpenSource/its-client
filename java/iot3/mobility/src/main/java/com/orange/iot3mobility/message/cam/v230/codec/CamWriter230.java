package com.orange.iot3mobility.message.cam.v230.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.message.cam.v230.model.*;
import com.orange.iot3mobility.message.cam.v230.model.basiccontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer.*;
import com.orange.iot3mobility.message.cam.v230.validation.CamValidator230;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Streaming JSON writer for CAM 2.3.0 payloads (structured JSON or ASN.1).
 */
public final class CamWriter230 {

    private final JsonFactory jsonFactory;

    public CamWriter230(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public void write(CamEnvelope230 envelope, OutputStream out) throws IOException {
        CamValidator230.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            if (envelope.messageFormat() != null) {
                gen.writeStringField("message_format", envelope.messageFormat());
            }
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeStringField("version", envelope.version());
            gen.writeFieldName("message");
            writePayload(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writePayload(JsonGenerator gen, CamMessage230 payload) throws IOException {
        if (payload instanceof CamStructuredData structured) {
            gen.writeStartObject();
            gen.writeNumberField("protocol_version", structured.protocolVersion());
            gen.writeNumberField("station_id", structured.stationId());
            gen.writeNumberField("generation_delta_time", structured.generationDeltaTime());

            gen.writeFieldName("basic_container");
            writeBasicContainer(gen, structured.basicContainer());

            gen.writeFieldName("high_frequency_container");
            writeHighFrequencyContainer(gen, structured.highFrequencyContainer());

            if (structured.lowFrequencyContainer() != null) {
                gen.writeFieldName("low_frequency_container");
                writeLowFrequencyContainer(gen, structured.lowFrequencyContainer());
            }
            if (structured.specialVehicleContainer() != null) {
                gen.writeFieldName("special_vehicle_container");
                writeSpecialVehicleContainer(gen, structured.specialVehicleContainer());
            }
            gen.writeEndObject();
        } else if (payload instanceof CamAsn1Payload asn1) {
            gen.writeStartObject();
            gen.writeStringField("version", asn1.version());
            gen.writeStringField("payload", asn1.payload());
            gen.writeEndObject();
        } else {
            throw new IllegalArgumentException("Unsupported payload type: " + payload.getClass().getName());
        }
    }

    /* --------------------------------------------------------------------- */
    /* Basic container                                                       */
    /* --------------------------------------------------------------------- */

    private void writeBasicContainer(JsonGenerator gen, BasicContainer basic) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("station_type", basic.stationType());
        gen.writeFieldName("reference_position");
        writeReferencePosition(gen, basic.referencePosition());
        gen.writeEndObject();
    }

    private void writeReferencePosition(JsonGenerator gen, ReferencePosition reference) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", reference.latitude());
        gen.writeNumberField("longitude", reference.longitude());
        gen.writeFieldName("position_confidence_ellipse");
        writeEllipse(gen, reference.positionConfidenceEllipse());
        gen.writeFieldName("altitude");
        writeAltitude(gen, reference.altitude());
        gen.writeEndObject();
    }

    private void writeEllipse(JsonGenerator gen, PositionConfidenceEllipse ellipse) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("semi_major", ellipse.semiMajor());
        gen.writeNumberField("semi_minor", ellipse.semiMinor());
        gen.writeNumberField("semi_major_orientation", ellipse.semiMajorOrientation());
        gen.writeEndObject();
    }

    private void writeAltitude(JsonGenerator gen, Altitude altitude) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", altitude.value());
        gen.writeNumberField("confidence", altitude.confidence());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* High-frequency containers                                             */
    /* --------------------------------------------------------------------- */

    private void writeHighFrequencyContainer(JsonGenerator gen, HighFrequencyContainer container) throws IOException {
        gen.writeStartObject();
        if (container instanceof BasicVehicleContainerHighFrequency basicVehicle) {
            gen.writeFieldName("basic_vehicle_container_high_frequency");
            writeBasicVehicleHF(gen, basicVehicle);
        } else if (container instanceof RsuContainerHighFrequency rsu) {
            gen.writeFieldName("rsu_container_high_frequency");
            writeRsuHF(gen, rsu);
        } else {
            throw new IllegalArgumentException("Unknown high frequency container: " + container.getClass().getName());
        }
        gen.writeEndObject();
    }

    private void writeBasicVehicleHF(JsonGenerator gen, BasicVehicleContainerHighFrequency container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("heading");
        writeHeading(gen, container.heading());
        gen.writeFieldName("speed");
        writeSpeed(gen, container.speed());
        gen.writeNumberField("drive_direction", container.driveDirection());
        gen.writeFieldName("vehicle_length");
        writeVehicleLength(gen, container.vehicleLength());
        gen.writeNumberField("vehicle_width", container.vehicleWidth());
        gen.writeFieldName("longitudinal_acceleration");
        writeAccelerationComponent(gen, container.longitudinalAcceleration());
        gen.writeFieldName("curvature");
        writeCurvature(gen, container.curvature());
        gen.writeNumberField("curvature_calculation_mode", container.curvatureCalculationMode());
        gen.writeFieldName("yaw_rate");
        writeYawRate(gen, container.yawRate());

        if (container.accelerationControl() != null) {
            gen.writeFieldName("acceleration_control");
            writeAccelerationControl(gen, container.accelerationControl());
        }
        if (container.lanePosition() != null) {
            gen.writeNumberField("lane_position", container.lanePosition());
        }
        if (container.steeringWheelAngle() != null) {
            gen.writeFieldName("steering_wheel_angle");
            writeSteeringWheelAngle(gen, container.steeringWheelAngle());
        }
        if (container.lateralAcceleration() != null) {
            gen.writeFieldName("lateral_acceleration");
            writeAccelerationComponent(gen, container.lateralAcceleration());
        }
        if (container.verticalAcceleration() != null) {
            gen.writeFieldName("vertical_acceleration");
            writeAccelerationComponent(gen, container.verticalAcceleration());
        }
        if (container.performanceClass() != null) {
            gen.writeNumberField("performance_class", container.performanceClass());
        }
        if (container.cenDsrcTollingZone() != null) {
            gen.writeFieldName("cen_dsrc_tolling_zone");
            writeCenDsrcTollingZone(gen, container.cenDsrcTollingZone());
        }
        gen.writeEndObject();
    }

    private void writeRsuHF(JsonGenerator gen, RsuContainerHighFrequency container) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("protected_communication_zones_rsu");
        for (ProtectedCommunicationZone zone : container.protectedCommunicationZonesRsu()) {
            gen.writeStartObject();
            gen.writeNumberField("protected_zone_type", zone.protectedZoneType());
            if (zone.expiryTime() != null) {
                gen.writeNumberField("expiry_time", zone.expiryTime());
            }
            gen.writeNumberField("protected_zone_latitude", zone.protectedZoneLatitude());
            gen.writeNumberField("protected_zone_longitude", zone.protectedZoneLongitude());
            if (zone.protectedZoneRadius() != null) {
                gen.writeNumberField("protected_zone_radius", zone.protectedZoneRadius());
            }
            if (zone.protectedZoneId() != null) {
                gen.writeNumberField("protected_zone_id", zone.protectedZoneId());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Low-frequency container                                               */
    /* --------------------------------------------------------------------- */

    private void writeLowFrequencyContainer(JsonGenerator gen, LowFrequencyContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("basic_vehicle_container_low_frequency");
        writeBasicVehicleLF(gen, container.basicVehicleContainerLowFrequency());
        gen.writeEndObject();
    }

    private void writeBasicVehicleLF(JsonGenerator gen, BasicVehicleContainerLowFrequency low) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("vehicle_role", low.vehicleRole());
        gen.writeFieldName("exterior_lights");
        writeExteriorLights(gen, low.exteriorLights());
        gen.writeArrayFieldStart("path_history");
        for (PathPoint point : low.pathHistory()) {
            gen.writeStartObject();
            gen.writeFieldName("path_position");
            writeDeltaReferencePosition(gen, point.pathPosition());
            if (point.pathDeltaTime() != null) {
                gen.writeNumberField("path_delta_time", point.pathDeltaTime());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeExteriorLights(JsonGenerator gen, ExteriorLights lights) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("low_beam_headlights_on", lights.lowBeamHeadlightsOn());
        gen.writeBooleanField("high_beam_headlights_on", lights.highBeamHeadlightsOn());
        gen.writeBooleanField("left_turn_signal_on", lights.leftTurnSignalOn());
        gen.writeBooleanField("right_turn_signal_on", lights.rightTurnSignalOn());
        gen.writeBooleanField("daytime_running_lights_on", lights.daytimeRunningLightsOn());
        gen.writeBooleanField("reverse_light_on", lights.reverseLightOn());
        gen.writeBooleanField("fog_light_on", lights.fogLightOn());
        gen.writeBooleanField("parking_lights_on", lights.parkingLightsOn());
        gen.writeEndObject();
    }

    private void writeDeltaReferencePosition(JsonGenerator gen, DeltaReferencePosition delta) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("delta_latitude", delta.deltaLatitude());
        gen.writeNumberField("delta_longitude", delta.deltaLongitude());
        gen.writeNumberField("delta_altitude", delta.deltaAltitude());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Special vehicle container                                             */
    /* --------------------------------------------------------------------- */

    private void writeSpecialVehicleContainer(JsonGenerator gen, SpecialVehicleContainer container) throws IOException {
        gen.writeStartObject();
        SpecialVehiclePayload payload = container.payload();
        if (payload instanceof PublicTransportContainer pt) {
            gen.writeFieldName("public_transport_container");
            writePublicTransport(gen, pt);
        } else if (payload instanceof SpecialTransportContainer st) {
            gen.writeFieldName("special_transport_container");
            writeSpecialTransport(gen, st);
        } else if (payload instanceof DangerousGoodsContainer dg) {
            gen.writeFieldName("dangerous_goods_container");
            writeDangerousGoods(gen, dg);
        } else if (payload instanceof RoadWorksContainer rw) {
            gen.writeFieldName("road_works_container_basic");
            writeRoadWorks(gen, rw);
        } else if (payload instanceof RescueContainer rescue) {
            gen.writeFieldName("rescue_container");
            writeRescue(gen, rescue);
        } else if (payload instanceof EmergencyContainer emergency) {
            gen.writeFieldName("emergency_container");
            writeEmergency(gen, emergency);
        } else if (payload instanceof SafetyCarContainer safetyCar) {
            gen.writeFieldName("safety_car_container");
            writeSafetyCar(gen, safetyCar);
        } else {
            throw new IllegalArgumentException("Unknown special vehicle payload: " + payload.getClass().getName());
        }
        gen.writeEndObject();
    }

    private void writePublicTransport(JsonGenerator gen, PublicTransportContainer pt) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("embarkation_status", pt.embarkationStatus());
        if (pt.ptActivation() != null) {
            gen.writeFieldName("pt_activation");
            gen.writeStartObject();
            gen.writeNumberField("pt_activation_type", pt.ptActivation().ptActivationType());
            gen.writeStringField("pt_activation_data", pt.ptActivation().ptActivationData());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private void writeSpecialTransport(JsonGenerator gen, SpecialTransportContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("special_transport_type");
        gen.writeStartObject();
        SpecialTransportType type = container.specialTransportType();
        gen.writeBooleanField("heavy_load", type.heavyLoad());
        gen.writeBooleanField("excess_width", type.excessWidth());
        gen.writeBooleanField("excess_length", type.excessLength());
        gen.writeBooleanField("excess_height", type.excessHeight());
        gen.writeEndObject();

        gen.writeFieldName("light_bar_siren_in_use");
        writeLightBar(gen, container.lightBarSirenInUse());
        gen.writeEndObject();
    }

    private void writeDangerousGoods(JsonGenerator gen, DangerousGoodsContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("dangerous_goods_basic", container.dangerousGoodsBasic());
        gen.writeEndObject();
    }

    private void writeRoadWorks(JsonGenerator gen, RoadWorksContainer container) throws IOException {
        gen.writeStartObject();
        if (container.roadWorksSubCauseCode() != null) {
            gen.writeNumberField("road_works_sub_cause_code", container.roadWorksSubCauseCode());
        }
        if (container.lightBarSirenInUse() != null) {
            gen.writeFieldName("light_bar_siren_in_use");
            writeLightBar(gen, container.lightBarSirenInUse());
        }
        if (container.closedLanes() != null) {
            gen.writeFieldName("closed_lanes");
            writeClosedLanes(gen, container.closedLanes());
        }
        gen.writeEndObject();
    }

    private void writeClosedLanes(JsonGenerator gen, ClosedLanes closedLanes) throws IOException {
        gen.writeStartObject();
        if (closedLanes.innerHardShoulderStatus() != null) {
            gen.writeNumberField("inner_hard_shoulder_status", closedLanes.innerHardShoulderStatus());
        }
        if (closedLanes.outerHardShoulderStatus() != null) {
            gen.writeNumberField("outer_hard_shoulder_status", closedLanes.outerHardShoulderStatus());
        }
        if (closedLanes.drivingLaneStatus() != null) {
            gen.writeFieldName("driving_lane_status");
            gen.writeStartObject();
            DrivingLaneStatus status = closedLanes.drivingLaneStatus();
            gen.writeBooleanField("lane_1_closed", status.lane1Closed());
            gen.writeBooleanField("lane_2_closed", status.lane2Closed());
            gen.writeBooleanField("lane_3_closed", status.lane3Closed());
            gen.writeBooleanField("lane_4_closed", status.lane4Closed());
            gen.writeBooleanField("lane_5_closed", status.lane5Closed());
            gen.writeBooleanField("lane_6_closed", status.lane6Closed());
            gen.writeBooleanField("lane_7_closed", status.lane7Closed());
            gen.writeBooleanField("lane_8_closed", status.lane8Closed());
            gen.writeBooleanField("lane_9_closed", status.lane9Closed());
            gen.writeBooleanField("lane_10_closed", status.lane10Closed());
            gen.writeBooleanField("lane_11_closed", status.lane11Closed());
            gen.writeBooleanField("lane_12_closed", status.lane12Closed());
            gen.writeBooleanField("lane_13_closed", status.lane13Closed());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private void writeRescue(JsonGenerator gen, RescueContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("light_bar_siren_in_use");
        writeLightBar(gen, container.lightBarSirenInUse());
        gen.writeEndObject();
    }

    private void writeEmergency(JsonGenerator gen, EmergencyContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("light_bar_siren_in_use");
        writeLightBar(gen, container.lightBarSirenInUse());
        if (container.incidentIndication() != null) {
            gen.writeFieldName("incident_indication");
            writeIncidentIndication(gen, container.incidentIndication());
        }
        if (container.emergencyPriority() != null) {
            gen.writeFieldName("emergency_priority");
            writeEmergencyPriority(gen, container.emergencyPriority());
        }
        gen.writeEndObject();
    }

    private void writeSafetyCar(JsonGenerator gen, SafetyCarContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("light_bar_siren_in_use");
        writeLightBar(gen, container.lightBarSirenInUse());
        if (container.incidentIndication() != null) {
            gen.writeFieldName("incident_indication");
            writeIncidentIndication(gen, container.incidentIndication());
        }
        if (container.trafficRule() != null) {
            gen.writeNumberField("traffic_rule", container.trafficRule());
        }
        if (container.speedLimit() != null) {
            gen.writeNumberField("speed_limit", container.speedLimit());
        }
        gen.writeEndObject();
    }

    private void writeIncidentIndication(JsonGenerator gen, IncidentIndication indication) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("cc_and_scc");
        writeCauseCode(gen, indication.ccAndScc());
        gen.writeEndObject();
    }

    private void writeCauseCode(JsonGenerator gen, CauseCode causeCode) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("cause", causeCode.cause());
        if (causeCode.subcause() != null) {
            gen.writeNumberField("subcause", causeCode.subcause());
        }
        gen.writeEndObject();
    }

    private void writeEmergencyPriority(JsonGenerator gen, EmergencyPriority priority) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("request_for_right_of_way", priority.requestForRightOfWay());
        gen.writeBooleanField("request_for_free_crossing_at_a_traffic_light",
                priority.requestForFreeCrossingAtTrafficLight());
        gen.writeEndObject();
    }

    private void writeLightBar(JsonGenerator gen, LightBarSiren lightBar) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("light_bar_activated", lightBar.lightBarActivated());
        gen.writeBooleanField("siren_activated", lightBar.sirenActivated());
        gen.writeEndObject();
    }

    /* --------------------------------------------------------------------- */
    /* Primitive helpers                                                     */
    /* --------------------------------------------------------------------- */

    private void writeHeading(JsonGenerator gen, Heading heading) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", heading.value());
        gen.writeNumberField("confidence", heading.confidence());
        gen.writeEndObject();
    }

    private void writeSpeed(JsonGenerator gen, Speed speed) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", speed.value());
        gen.writeNumberField("confidence", speed.confidence());
        gen.writeEndObject();
    }

    private void writeVehicleLength(JsonGenerator gen, VehicleLength length) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", length.value());
        gen.writeNumberField("confidence", length.confidence());
        gen.writeEndObject();
    }

    private void writeAccelerationComponent(JsonGenerator gen, AccelerationComponent component) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", component.value());
        gen.writeNumberField("confidence", component.confidence());
        gen.writeEndObject();
    }

    private void writeCurvature(JsonGenerator gen, Curvature curvature) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", curvature.value());
        gen.writeNumberField("confidence", curvature.confidence());
        gen.writeEndObject();
    }

    private void writeYawRate(JsonGenerator gen, YawRate yawRate) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", yawRate.value());
        gen.writeNumberField("confidence", yawRate.confidence());
        gen.writeEndObject();
    }

    private void writeSteeringWheelAngle(JsonGenerator gen, SteeringWheelAngle angle) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", angle.value());
        gen.writeNumberField("confidence", angle.confidence());
        gen.writeEndObject();
    }

    private void writeAccelerationControl(JsonGenerator gen, AccelerationControl control) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("brake_pedal_engaged", control.brakePedalEngaged());
        gen.writeBooleanField("gas_pedal_engaged", control.gasPedalEngaged());
        gen.writeBooleanField("emergency_brake_engaged", control.emergencyBrakeEngaged());
        gen.writeBooleanField("collision_warning_engaged", control.collisionWarningEngaged());
        gen.writeBooleanField("acc_engaged", control.accEngaged());
        gen.writeBooleanField("cruise_control_engaged", control.cruiseControlEngaged());
        gen.writeBooleanField("speed_limiter_engaged", control.speedLimiterEngaged());
        gen.writeEndObject();
    }

    private void writeCenDsrcTollingZone(JsonGenerator gen, CenDsrcTollingZone zone) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protected_zone_latitude", zone.protectedZoneLatitude());
        gen.writeNumberField("protected_zone_longitude", zone.protectedZoneLongitude());
        if (zone.cenDsrcTollingZoneId() != null) {
            gen.writeNumberField("cen_dsrc_tolling_zone_id", zone.cenDsrcTollingZoneId());
        }
        gen.writeEndObject();
    }
}
