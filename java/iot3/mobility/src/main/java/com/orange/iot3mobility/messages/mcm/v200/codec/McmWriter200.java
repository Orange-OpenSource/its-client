/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.codec;

import com.fasterxml.jackson.core.*;
import com.orange.iot3mobility.messages.mcm.v200.model.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedTrrContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.*;
import com.orange.iot3mobility.messages.mcm.v200.validation.McmValidator200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Jackson streaming writer for MCM v2.0.0 envelopes (json/raw format).
 */
public final class McmWriter200 {

    private final JsonFactory jsonFactory;

    public McmWriter200(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(McmEnvelope200 envelope, OutputStream out) throws IOException {
        McmValidator200.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            gen.writeStringField("message_format", envelope.messageFormat());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeStringField("version", envelope.version());
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, McmMessage200 message) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", message.protocolVersion());
        gen.writeNumberField("station_id", message.stationId());
        gen.writeNumberField("generation_delta_time", message.generationDeltaTime());
        gen.writeNumberField("station_type", message.stationType());
        gen.writeNumberField("itss_role", message.itssRole());

        gen.writeFieldName("position");
        writeReferencePosition(gen, message.position());

        gen.writeFieldName("mcm_data");
        writeMcmData(gen, message.mcmData());

        gen.writeEndObject();
    }

    private void writeReferencePosition(JsonGenerator gen, ReferencePosition position) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", position.latitude());
        gen.writeNumberField("longitude", position.longitude());

        gen.writeFieldName("position_confidence_ellipse");
        gen.writeStartObject();
        gen.writeNumberField("semi_major_confidence", position.positionConfidenceEllipse().semiMajorConfidence());
        gen.writeNumberField("semi_minor_confidence", position.positionConfidenceEllipse().semiMinorConfidence());
        gen.writeNumberField("semi_major_orientation", position.positionConfidenceEllipse().semiMajorOrientation());
        gen.writeEndObject();

        if (position.altitude() != null) {
            gen.writeFieldName("altitude");
            gen.writeStartObject();
            gen.writeNumberField("altitude_value", position.altitude().altitudeValue());
            gen.writeNumberField("altitude_confidence", position.altitude().altitudeConfidence());
            gen.writeEndObject();
        }

        gen.writeEndObject();
    }

    private void writeMcmData(JsonGenerator gen, McmData mcmData) throws IOException {
        gen.writeStartObject();
        if (mcmData.vehicleManoeuvreContainer() != null) {
            gen.writeFieldName("vehicle_manoeuvre_container");
            writeVehicleManoeuvreContainer(gen, mcmData.vehicleManoeuvreContainer());
        } else if (mcmData.advisedManoeuvreContainer() != null) {
            gen.writeFieldName("advised_manoeuvre_container");
            writeManoeuvreAdviceList(gen, mcmData.advisedManoeuvreContainer());
        }
        gen.writeEndObject();
    }

    private void writeVehicleManoeuvreContainer(JsonGenerator gen, VehicleManoeuvreContainer container) throws IOException {
        gen.writeStartObject();

        gen.writeFieldName("mcm_generic_current_state_container");
        writeGenericCurrentState(gen, container.mcmGenericCurrentStateContainer());

        gen.writeFieldName("vehicle_current_state_container");
        writeVehicleCurrentState(gen, container.vehicleCurrentStateContainer());

        gen.writeFieldName("submaneuvres");
        writeSubmanoeuvreList(gen, container.submaneuvres());

        if (container.manoeuvreAdvice() != null) {
            gen.writeFieldName("manoeuvre_advice");
            writeManoeuvreAdviceList(gen, container.manoeuvreAdvice());
        }

        gen.writeEndObject();
    }

    private void writeGenericCurrentState(JsonGenerator gen, McmGenericCurrentStateContainer state) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("mcm_type", state.mcmType());
        gen.writeNumberField("manoeuvre_id", state.manoeuvreId());
        gen.writeNumberField("concept", state.concept());

        if (state.rational() != null) {
            gen.writeFieldName("rational");
            gen.writeStartObject();
            if (state.rational().manoeuvreCooperationGoal() != null) {
                gen.writeNumberField("manoeuvre_cooperation_goal", state.rational().manoeuvreCooperationGoal());
            }
            if (state.rational().manoeuvreCooperationCost() != null) {
                gen.writeNumberField("manoeuvre_cooperation_cost", state.rational().manoeuvreCooperationCost());
            }
            gen.writeEndObject();
        }

        if (state.executionStatus() != null) {
            gen.writeNumberField("execution_status", state.executionStatus());
        }

        gen.writeEndObject();
    }

    private void writeVehicleCurrentState(JsonGenerator gen, VehicleCurrentStateContainer state) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("manoeuvre_overall_strategy", state.manoeuvreOverallStrategy().value);

        gen.writeFieldName("vehicle_speed");
        writeSpeed(gen, state.vehicleSpeed());

        gen.writeFieldName("vehicle_heading");
        writeWgs84Angle(gen, state.vehicleHeading());

        gen.writeFieldName("vehicle_size");
        writeVehicleSize(gen, state.vehicleSize());

        gen.writeEndObject();
    }

    private void writeSpeed(JsonGenerator gen, Speed speed) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("speed_value", speed.speedValue());
        gen.writeNumberField("speed_confidence", speed.speedConfidence());
        gen.writeEndObject();
    }

    private void writeWgs84Angle(JsonGenerator gen, Wgs84Angle angle) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", angle.value());
        gen.writeNumberField("confidence", angle.confidence());
        gen.writeEndObject();
    }

    private void writeVehicleSize(JsonGenerator gen, VehicleSize size) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("vehicle_type", size.vehicleType());

        if (size.vehicleTransportedGoods() != null) {
            gen.writeFieldName("vehicle_transported_goods");
            gen.writeStartObject();
            gen.writeBooleanField("heavy_load", size.vehicleTransportedGoods().heavyLoad());
            gen.writeBooleanField("excess_width", size.vehicleTransportedGoods().excessWidth());
            gen.writeBooleanField("excess_length", size.vehicleTransportedGoods().excessLength());
            gen.writeBooleanField("excess_height", size.vehicleTransportedGoods().excessHeight());
            gen.writeEndObject();
        }

        gen.writeFieldName("vehicle_lenth");
        gen.writeStartObject();
        gen.writeNumberField("vehicle_length_value", size.vehicleLength().vehicleLengthValue());
        gen.writeNumberField("vehicle_length_confidence_indication", size.vehicleLength().vehicleLengthConfidenceIndication());
        gen.writeEndObject();

        gen.writeNumberField("vehicle_width", size.vehicleWidth());
        gen.writeNumberField("vehicle_height", size.vehicleHeight());
        gen.writeEndObject();
    }

    private void writeSubmanoeuvreList(JsonGenerator gen, List<Submanoeuvre> submaneuvres) throws IOException {
        gen.writeStartArray();
        for (Submanoeuvre submanoeuvre : submaneuvres) {
            writeSubmanoeuvre(gen, submanoeuvre);
        }
        gen.writeEndArray();
    }

    private void writeSubmanoeuvre(JsonGenerator gen, Submanoeuvre submanoeuvre) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("submanoeuvre_id", submanoeuvre.submanoeuvreId());

        if (submanoeuvre.submanoeuvreStrategy() != null) {
            gen.writeStringField("submanoeuvre_strategy", submanoeuvre.submanoeuvreStrategy().value);
        }
        if (submanoeuvre.referenceTrajectory() != null) {
            gen.writeFieldName("reference_trajectory");
            writeWayPointList(gen, submanoeuvre.referenceTrajectory());
        }
        if (submanoeuvre.targetRoadResourceIContainer() != null) {
            gen.writeFieldName("target_road_resource_i_container");
            writeTrrDescription(gen, submanoeuvre.targetRoadResourceIContainer());
        }

        gen.writeFieldName("temporal_charateristics");
        writeTemporalCharacteristics(gen, submanoeuvre.temporalCharacteristics());

        gen.writeFieldName("kinematics_characteristics");
        gen.writeStartObject();
        gen.writeEndObject();

        gen.writeEndObject();
    }

    private void writeManoeuvreAdviceList(JsonGenerator gen, List<ManoeuvreAdvice> adviceList) throws IOException {
        gen.writeStartArray();
        for (ManoeuvreAdvice advice : adviceList) {
            writeManoeuvreAdvice(gen, advice);
        }
        gen.writeEndArray();
    }

    private void writeManoeuvreAdvice(JsonGenerator gen, ManoeuvreAdvice advice) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("executant_id", advice.executantId());
        gen.writeStringField("current_state_advised_change", advice.currentStateAdvisedChange());
        gen.writeFieldName("submaneuvres");
        writeAdvisedSubmanoeuvreList(gen, advice.submaneuvres());
        gen.writeEndObject();
    }

    private void writeAdvisedSubmanoeuvreList(JsonGenerator gen, List<AdvisedSubmanoeuvre> advisedSubmanoeuvres) throws IOException {
        gen.writeStartArray();
        for (AdvisedSubmanoeuvre advisedSubmanoeuvre : advisedSubmanoeuvres) {
            writeAdvisedSubmanoeuvre(gen, advisedSubmanoeuvre);
        }
        gen.writeEndArray();
    }

    private void writeAdvisedSubmanoeuvre(JsonGenerator gen, AdvisedSubmanoeuvre advisedSubmanoeuvre) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("submanoeuvre_id", advisedSubmanoeuvre.submanoeuvreId());

        if (advisedSubmanoeuvre.advisedTrajectory() != null) {
            gen.writeFieldName("advised_trajectory");
            writeWayPointList(gen, advisedSubmanoeuvre.advisedTrajectory());
        }
        if (advisedSubmanoeuvre.advisedTargetRoadResource() != null) {
            gen.writeFieldName("advised_target_road_resource");
            writeAdvisedTrrContainer(gen, advisedSubmanoeuvre.advisedTargetRoadResource());
        }

        gen.writeEndObject();
    }

    private void writeWayPointList(JsonGenerator gen, List<WayPoint> wayPoints) throws IOException {
        gen.writeStartArray();
        for (WayPoint wayPoint : wayPoints) {
            writeWayPoint(gen, wayPoint);
        }
        gen.writeEndArray();
    }

    private void writeWayPoint(JsonGenerator gen, WayPoint wayPoint) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("way_point_type", wayPoint.wayPointType());
        gen.writeNumberField("longitude", wayPoint.longitude());
        gen.writeNumberField("latitude", wayPoint.latitude());
        if (wayPoint.altitude() != null) {
            gen.writeNumberField("altitude", wayPoint.altitude());
        }
        if (wayPoint.heading() != null) {
            gen.writeFieldName("heading");
            writeWgs84Angle(gen, wayPoint.heading());
        }
        gen.writeNumberField("speed", wayPoint.speed());
        gen.writeEndObject();
    }

    private void writeTrrDescription(JsonGenerator gen, TrrDescription trrDescription) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("trr_type", trrDescription.trrType());
        gen.writeNumberField("lane_count", trrDescription.laneCount());
        if (trrDescription.startingLaneNumber() != null) {
            gen.writeNumberField("starting_lane_number", trrDescription.startingLaneNumber());
        }
        if (trrDescription.endingLaneNumber() != null) {
            gen.writeNumberField("ending_lane_number", trrDescription.endingLaneNumber());
        }
        if (trrDescription.waypoints() != null) {
            gen.writeFieldName("waypoints");
            writeWayPointList(gen, trrDescription.waypoints());
        }
        if (trrDescription.heading() != null) {
            gen.writeFieldName("heading");
            gen.writeStartArray();
            for (Wgs84Angle angle : trrDescription.heading()) {
                writeWgs84Angle(gen, angle);
            }
            gen.writeEndArray();
        }
        gen.writeNumberField("trr_width", trrDescription.trrWidth());
        gen.writeNumberField("trr_length", trrDescription.trrLength());
        gen.writeEndObject();
    }

    private void writeAdvisedTrrContainer(JsonGenerator gen, AdvisedTrrContainer container) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("trr_description");
        writeTrrDescription(gen, container.trrDescription());
        gen.writeFieldName("temporal_characteristics");
        writeTemporalCharacteristics(gen, container.temporalCharacteristics());
        gen.writeFieldName("kinematics_characteristics");
        gen.writeStartObject();
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private void writeTemporalCharacteristics(JsonGenerator gen, TemporalCharacteristics temporalCharacteristics) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("trr_occupancy_start_time", temporalCharacteristics.trrOccupancyStartTime());
        gen.writeNumberField("trr_occupancy_end_time", temporalCharacteristics.trrOccupancyEndTime());
        gen.writeEndObject();
    }
}

