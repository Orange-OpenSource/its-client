/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.path.PathElement;
import com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathHistory;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.EventType;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.LinkedCause;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.messages.denm.v113.validation.DenmValidator113;

import java.io.IOException;
import java.io.OutputStream;

public final class DenmWriter113 {

    private final JsonFactory jsonFactory;

    public DenmWriter113(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(DenmEnvelope113 envelope, OutputStream out) throws IOException {
        DenmValidator113.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("type", envelope.type());
            gen.writeStringField("origin", envelope.origin());
            gen.writeStringField("version", envelope.version());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            if (envelope.path() != null) {
                gen.writeFieldName("path");
                writePath(gen, envelope.path());
            }
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, DenmMessage113 message) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", message.protocolVersion());
        gen.writeNumberField("station_id", message.stationId());
        gen.writeFieldName("management_container");
        writeManagementContainer(gen, message.managementContainer());
        if (message.situationContainer() != null) {
            gen.writeFieldName("situation_container");
            writeSituationContainer(gen, message.situationContainer());
        }
        if (message.locationContainer() != null) {
            gen.writeFieldName("location_container");
            writeLocationContainer(gen, message.locationContainer());
        }
        if (message.alacarteContainer() != null) {
            gen.writeFieldName("alacarte_container");
            writeAlacarteContainer(gen, message.alacarteContainer());
        }
        gen.writeEndObject();
    }

    private void writePath(JsonGenerator gen, java.util.List<PathElement> path) throws IOException {
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

    private void writeManagementContainer(JsonGenerator gen, ManagementContainer management) throws IOException {
        gen.writeStartObject();
        writeActionId(gen, management.actionId());
        gen.writeNumberField("detection_time", management.detectionTime());
        gen.writeNumberField("reference_time", management.referenceTime());
        if (management.termination() != null) {
            gen.writeNumberField("termination", management.termination());
        }
        gen.writeFieldName("event_position");
        writeReferencePosition(gen, management.eventPosition());
        if (management.relevanceDistance() != null) {
            gen.writeNumberField("relevance_distance", management.relevanceDistance());
        }
        if (management.relevanceTrafficDirection() != null) {
            gen.writeNumberField("relevance_traffic_direction", management.relevanceTrafficDirection());
        }
        if (management.validityDuration() != null) {
            gen.writeNumberField("validity_duration", management.validityDuration());
        }
        if (management.transmissionInterval() != null) {
            gen.writeNumberField("transmission_interval", management.transmissionInterval());
        }
        if (management.stationType() != null) {
            gen.writeNumberField("station_type", management.stationType());
        }
        if (management.confidence() != null) {
            gen.writeFieldName("confidence");
            writePositionConfidence(gen, management.confidence());
        }
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
        gen.writeNumberField("altitude", position.altitude());
        gen.writeEndObject();
    }

    private void writePositionConfidence(JsonGenerator gen, PositionConfidence confidence) throws IOException {
        gen.writeStartObject();
        if (confidence.positionConfidenceEllipse() != null) {
            gen.writeFieldName("position_confidence_ellipse");
            writePositionConfidenceEllipse(gen, confidence.positionConfidenceEllipse());
        }
        if (confidence.altitude() != null) {
            gen.writeNumberField("altitude", confidence.altitude());
        }
        gen.writeEndObject();
    }

    private void writePositionConfidenceEllipse(JsonGenerator gen, PositionConfidenceEllipse ellipse) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("semi_major_confidence", ellipse.semiMajorConfidence());
        gen.writeNumberField("semi_minor_confidence", ellipse.semiMinorConfidence());
        gen.writeNumberField("semi_major_orientation", ellipse.semiMajorOrientation());
        gen.writeEndObject();
    }

    private void writeSituationContainer(JsonGenerator gen, SituationContainer situation) throws IOException {
        gen.writeStartObject();
        if (situation.informationQuality() != null) {
            gen.writeNumberField("information_quality", situation.informationQuality());
        }
        gen.writeFieldName("event_type");
        writeEventType(gen, situation.eventType());
        if (situation.linkedCause() != null) {
            gen.writeFieldName("linked_cause");
            writeLinkedCause(gen, situation.linkedCause());
        }
        gen.writeEndObject();
    }

    private void writeEventType(JsonGenerator gen, EventType eventType) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("cause", eventType.cause());
        if (eventType.subcause() != null) {
            gen.writeNumberField("subcause", eventType.subcause());
        }
        gen.writeEndObject();
    }

    private void writeLinkedCause(JsonGenerator gen, LinkedCause linkedCause) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("cause", linkedCause.cause());
        if (linkedCause.subcause() != null) {
            gen.writeNumberField("subcause", linkedCause.subcause());
        }
        gen.writeEndObject();
    }

    private void writeLocationContainer(JsonGenerator gen, LocationContainer location) throws IOException {
        gen.writeStartObject();
        if (location.eventSpeed() != null) {
            gen.writeNumberField("event_speed", location.eventSpeed());
        }
        if (location.eventPositionHeading() != null) {
            gen.writeNumberField("event_position_heading", location.eventPositionHeading());
        }
        gen.writeFieldName("traces");
        writeTraces(gen, location.traces());
        if (location.roadType() != null) {
            gen.writeNumberField("road_type", location.roadType());
        }
        if (location.confidence() != null) {
            gen.writeFieldName("confidence");
            writeLocationConfidence(gen, location.confidence());
        }
        gen.writeEndObject();
    }

    private void writeTraces(JsonGenerator gen, java.util.List<PathHistory> traces) throws IOException {
        gen.writeStartArray();
        for (PathHistory history : traces) {
            gen.writeStartObject();
            gen.writeFieldName("path_history");
            writePathHistory(gen, history.pathHistory());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writePathHistory(JsonGenerator gen, java.util.List<PathPoint> points) throws IOException {
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

    private void writeLocationConfidence(JsonGenerator gen, LocationConfidence confidence) throws IOException {
        gen.writeStartObject();
        if (confidence.eventSpeed() != null) {
            gen.writeNumberField("event_speed", confidence.eventSpeed());
        }
        if (confidence.eventPositionHeading() != null) {
            gen.writeNumberField("event_position_heading", confidence.eventPositionHeading());
        }
        gen.writeEndObject();
    }

    private void writeAlacarteContainer(JsonGenerator gen, AlacarteContainer alacarte) throws IOException {
        gen.writeStartObject();
        if (alacarte.lanePosition() != null) {
            gen.writeNumberField("lane_position", alacarte.lanePosition());
        }
        if (alacarte.positioningSolution() != null) {
            gen.writeNumberField("positioning_solution", alacarte.positioningSolution());
        }
        gen.writeEndObject();
    }
}
