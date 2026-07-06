/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v230.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.denm.v230.model.DenmEnvelope230;
import com.orange.iot3mobility.messages.denm.v230.model.DenmMessage230;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.CarryingDangerousGoods;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.ClosedLanes;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.RoadWorks;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.StationaryVehicle;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.VehicleIdentification;
import com.orange.iot3mobility.messages.denm.v230.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v230.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.DetectionZone;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventPositionHeading;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventSpeed;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.path.PathElement;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.EventZone;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.messages.denm.v230.validation.DenmValidator230;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class DenmWriter230 {

    private final JsonFactory jsonFactory;

    public DenmWriter230(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(DenmEnvelope230 envelope, OutputStream out) throws IOException {
        DenmValidator230.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeStringField("version", envelope.version());
            if (envelope.path() != null) {
                gen.writeFieldName("path");
                writePath(gen, envelope.path());
            }
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, DenmMessage230 message) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", message.protocolVersion());
        gen.writeNumberField("station_id", message.stationId());
        gen.writeFieldName("management");
        writeManagement(gen, message.managementContainer());
        if (message.situationContainer() != null) {
            gen.writeFieldName("situation_container");
            writeSituation(gen, message.situationContainer());
        }
        if (message.locationContainer() != null) {
            gen.writeFieldName("location_container");
            writeLocation(gen, message.locationContainer());
        }
        if (message.alacarteContainer() != null) {
            gen.writeFieldName("alacarte_container");
            writeAlacarte(gen, message.alacarteContainer());
        }
        gen.writeEndObject();
    }

    private void writePath(JsonGenerator gen, List<PathElement> path) throws IOException {
        gen.writeStartArray();
        for (PathElement element : path) {
            gen.writeStartObject();
            gen.writeFieldName("position");
            gen.writeStartObject();
            gen.writeNumberField("latitude", element.position().latitude());
            gen.writeNumberField("longitude", element.position().longitude());
            gen.writeNumberField("altitude", element.position().altitude());
            gen.writeEndObject();
            gen.writeStringField("message_type", element.messageType());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeManagement(JsonGenerator gen, ManagementContainer management) throws IOException {
        gen.writeStartObject();
        writeActionId(gen, management.actionId());
        gen.writeNumberField("detection_time", management.detectionTime());
        gen.writeNumberField("reference_time", management.referenceTime());
        if (management.termination() != null) {
            gen.writeNumberField("termination", management.termination());
        }
        gen.writeFieldName("event_position");
        writeReferencePosition(gen, management.eventPosition());
        if (management.awarenessDistance() != null) {
            gen.writeNumberField("awareness_distance", management.awarenessDistance());
        }
        if (management.trafficDirection() != null) {
            gen.writeNumberField("traffic_direction", management.trafficDirection());
        }
        if (management.validityDuration() != null) {
            gen.writeNumberField("validity_duration", management.validityDuration());
        }
        if (management.transmissionInterval() != null) {
            gen.writeNumberField("transmission_interval", management.transmissionInterval());
        }
        gen.writeNumberField("station_type", management.stationType());
        gen.writeEndObject();
    }

    private void writeActionId(JsonGenerator gen, ActionId actionId) throws IOException {
        gen.writeFieldName("action_id");
        gen.writeStartObject();
        gen.writeNumberField("originating_station_id", actionId.originatingStationId());
        gen.writeNumberField("sequence_number", actionId.sequenceNumber());
        gen.writeEndObject();
    }

    private void writeReferencePosition(JsonGenerator gen, ReferencePosition position) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", position.latitude());
        gen.writeNumberField("longitude", position.longitude());
        gen.writeFieldName("position_confidence_ellipse");
        writePositionConfidenceEllipse(gen, position.positionConfidenceEllipse());
        gen.writeFieldName("altitude");
        writeAltitude(gen, position.altitude());
        gen.writeEndObject();
    }

    private void writePositionConfidenceEllipse(JsonGenerator gen, PositionConfidenceEllipse ellipse) throws IOException {
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

    private void writeSituation(JsonGenerator gen, SituationContainer situation) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("information_quality", situation.informationQuality());
        gen.writeFieldName("event_type");
        writeCauseCode(gen, situation.eventType());
        if (situation.linkedCause() != null) {
            gen.writeFieldName("linked_cause");
            writeCauseCode(gen, situation.linkedCause());
        }
        if (situation.eventZone() != null) {
            gen.writeFieldName("event_zone");
            writeEventZone(gen, situation.eventZone());
        }
        if (situation.linkedDenms() != null) {
            gen.writeFieldName("linked_denms");
            writeLinkedDenms(gen, situation.linkedDenms());
        }
        if (situation.eventEnd() != null) {
            gen.writeNumberField("event_end", situation.eventEnd());
        }
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

    private void writeEventZone(JsonGenerator gen, List<EventZone> zones) throws IOException {
        gen.writeStartArray();
        for (EventZone zone : zones) {
            gen.writeStartObject();
            gen.writeFieldName("event_position");
            writeDeltaReferencePosition(gen, zone.eventPosition());
            if (zone.eventDeltaTime() != null) {
                gen.writeNumberField("event_delta_time", zone.eventDeltaTime());
            }
            gen.writeNumberField("information_quality", zone.informationQuality());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeLinkedDenms(JsonGenerator gen, List<ActionId> linkedDenms) throws IOException {
        gen.writeStartArray();
        for (ActionId actionId : linkedDenms) {
            gen.writeStartObject();
            gen.writeNumberField("originating_station_id", actionId.originatingStationId());
            gen.writeNumberField("sequence_number", actionId.sequenceNumber());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeLocation(JsonGenerator gen, LocationContainer location) throws IOException {
        gen.writeStartObject();
        if (location.eventSpeed() != null) {
            gen.writeFieldName("event_speed");
            writeEventSpeed(gen, location.eventSpeed());
        }
        if (location.eventPositionHeading() != null) {
            gen.writeFieldName("event_position_heading");
            writeEventPositionHeading(gen, location.eventPositionHeading());
        }
        if (location.detectionZonesToEventPosition() != null) {
            gen.writeFieldName("detection_zones_to_event_position");
            writeDetectionZones(gen, location.detectionZonesToEventPosition());
        }
        if (location.roadType() != null) {
            gen.writeNumberField("road_type", location.roadType());
        }
        gen.writeEndObject();
    }

    private void writeEventSpeed(JsonGenerator gen, EventSpeed speed) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", speed.value());
        gen.writeNumberField("confidence", speed.confidence());
        gen.writeEndObject();
    }

    private void writeEventPositionHeading(JsonGenerator gen, EventPositionHeading heading) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", heading.value());
        gen.writeNumberField("confidence", heading.confidence());
        gen.writeEndObject();
    }

    private void writeDetectionZones(JsonGenerator gen, List<DetectionZone> zones) throws IOException {
        gen.writeStartArray();
        for (DetectionZone zone : zones) {
            gen.writeStartObject();
            gen.writeFieldName("path");
            writePathPoints(gen, zone.path());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writePathPoints(JsonGenerator gen, List<PathPoint> points) throws IOException {
        gen.writeStartArray();
        for (PathPoint point : points) {
            gen.writeStartObject();
            gen.writeFieldName("path_position");
            writeDeltaReferencePosition(gen, point.pathPosition());
            if (point.pathDeltaTime() != null) {
                gen.writeNumberField("path_delta_time", point.pathDeltaTime());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeDeltaReferencePosition(JsonGenerator gen, DeltaReferencePosition position) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("delta_latitude", position.deltaLatitude());
        gen.writeNumberField("delta_longitude", position.deltaLongitude());
        gen.writeNumberField("delta_altitude", position.deltaAltitude());
        gen.writeEndObject();
    }

    private void writeAlacarte(JsonGenerator gen, AlacarteContainer alacarte) throws IOException {
        gen.writeStartObject();
        if (alacarte.lanePosition() != null) {
            gen.writeNumberField("lane_position", alacarte.lanePosition());
        }
        if (alacarte.roadWorks() != null) {
            gen.writeFieldName("road_works");
            writeRoadWorks(gen, alacarte.roadWorks());
        }
        if (alacarte.positioningSolution() != null) {
            gen.writeNumberField("positioning_solution", alacarte.positioningSolution());
        }
        if (alacarte.stationaryVehicle() != null) {
            gen.writeFieldName("stationary_vehicle");
            writeStationaryVehicle(gen, alacarte.stationaryVehicle());
        }
        gen.writeEndObject();
    }

    private void writeRoadWorks(JsonGenerator gen, RoadWorks roadWorks) throws IOException {
        gen.writeStartObject();
        if (roadWorks.lightBarSirenInUse() != null) {
            gen.writeNumberField("light_bar_siren_in_use", roadWorks.lightBarSirenInUse());
        }
        if (roadWorks.closedLanes() != null) {
            gen.writeFieldName("closed_lanes");
            writeClosedLanes(gen, roadWorks.closedLanes());
        }
        if (roadWorks.restriction() != null) {
            gen.writeFieldName("restriction");
            gen.writeStartArray();
            for (Integer stationType : roadWorks.restriction()) {
                gen.writeNumber(stationType);
            }
            gen.writeEndArray();
        }
        if (roadWorks.speedLimit() != null) {
            gen.writeNumberField("speed_limit", roadWorks.speedLimit());
        }
        if (roadWorks.incidentIndication() != null) {
            gen.writeFieldName("incident_indication");
            writeCauseCode(gen, roadWorks.incidentIndication());
        }
        if (roadWorks.recommendedPath() != null) {
            gen.writeFieldName("recommended_path");
            gen.writeStartArray();
            for (DeltaReferencePosition deltaPosition : roadWorks.recommendedPath()) {
                writeDeltaReferencePosition(gen, deltaPosition);
            }
            gen.writeEndArray();
        }
        if (roadWorks.startingPointSpeedLimit() != null) {
            gen.writeFieldName("starting_point_speed_limit");
            writeDeltaReferencePosition(gen, roadWorks.startingPointSpeedLimit());
        }
        if (roadWorks.trafficFlowRule() != null) {
            gen.writeNumberField("traffic_flow_rule", roadWorks.trafficFlowRule());
        }
        if (roadWorks.referenceDenms() != null) {
            gen.writeFieldName("reference_denms");
            gen.writeStartArray();
            for (ActionId actionId : roadWorks.referenceDenms()) {
                gen.writeStartObject();
                gen.writeNumberField("originating_station_id", actionId.originatingStationId());
                gen.writeNumberField("sequence_number", actionId.sequenceNumber());
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    private void writeClosedLanes(JsonGenerator gen, ClosedLanes closedLanes) throws IOException {
        gen.writeStartObject();
        if (closedLanes.hardShoulderStatus() != null) {
            gen.writeNumberField("hard_shoulder_status", closedLanes.hardShoulderStatus());
        }
        if (closedLanes.drivingLaneStatus() != null) {
            gen.writeNumberField("driving_lane_status", closedLanes.drivingLaneStatus());
        }
        gen.writeEndObject();
    }

    private void writeStationaryVehicle(JsonGenerator gen, StationaryVehicle stationaryVehicle) throws IOException {
        gen.writeStartObject();
        if (stationaryVehicle.stationarySince() != null) {
            gen.writeNumberField("stationary_since", stationaryVehicle.stationarySince());
        }
        if (stationaryVehicle.stationaryCause() != null) {
            gen.writeFieldName("stationary_cause");
            writeCauseCode(gen, stationaryVehicle.stationaryCause());
        }
        if (stationaryVehicle.carryingDangerousGoods() != null) {
            gen.writeFieldName("carrying_dangerous_goods");
            writeCarryingDangerousGoods(gen, stationaryVehicle.carryingDangerousGoods());
        }
        if (stationaryVehicle.numberOfOccupants() != null) {
            gen.writeNumberField("number_of_occupants", stationaryVehicle.numberOfOccupants());
        }
        if (stationaryVehicle.vehicleIdentification() != null) {
            gen.writeFieldName("vehicle_identification");
            writeVehicleIdentification(gen, stationaryVehicle.vehicleIdentification());
        }
        if (stationaryVehicle.energyStorageType() != null) {
            gen.writeNumberField("energy_storage_type", stationaryVehicle.energyStorageType());
        }
        gen.writeEndObject();
    }

    private void writeCarryingDangerousGoods(JsonGenerator gen, CarryingDangerousGoods goods) throws IOException {
        gen.writeStartObject();
        if (goods.dangerousGoodsType() != null) {
            gen.writeNumberField("dangerous_goods_type", goods.dangerousGoodsType());
        }
        if (goods.unNumber() != null) {
            gen.writeNumberField("un_number", goods.unNumber());
        }
        if (goods.elevatedTemperature() != null) {
            gen.writeBooleanField("elevated_temperature", goods.elevatedTemperature());
        }
        if (goods.tunnelsRestricted() != null) {
            gen.writeBooleanField("tunnels_restricted", goods.tunnelsRestricted());
        }
        if (goods.limitedQuantity() != null) {
            gen.writeBooleanField("limited_quantity", goods.limitedQuantity());
        }
        if (goods.emergencyActionCode() != null) {
            gen.writeStringField("emergency_action_code", goods.emergencyActionCode());
        }
        if (goods.phoneNumber() != null) {
            gen.writeStringField("phone_number", goods.phoneNumber());
        }
        if (goods.companyName() != null) {
            gen.writeStringField("company_name", goods.companyName());
        }
        gen.writeEndObject();
    }

    private void writeVehicleIdentification(JsonGenerator gen, VehicleIdentification vehicleIdentification) throws IOException {
        gen.writeStartObject();
        if (vehicleIdentification.wMINumber() != null) {
            gen.writeStringField("w_m_inumber", vehicleIdentification.wMINumber());
        }
        if (vehicleIdentification.vDS() != null) {
            gen.writeStringField("v_d_s", vehicleIdentification.vDS());
        }
        gen.writeEndObject();
    }
}
