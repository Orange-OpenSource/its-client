/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Streaming JSON writer for SPATEM v2.0.0 envelopes.
 */
public final class SpatemWriter200 {

    private final JsonFactory jsonFactory;

    public SpatemWriter200(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(SpatemEnvelope200 envelope, OutputStream out) throws IOException {
        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("message_type", envelope.messageType());
            gen.writeStringField("origin", envelope.origin());
            gen.writeStringField("version", envelope.version());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200 msg) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", msg.protocolVersion());
        gen.writeNumberField("station_id", msg.stationId());
        if (msg.timestamp() != null) gen.writeNumberField("timestamp", msg.timestamp());
        if (msg.name() != null) gen.writeStringField("name", msg.name());
        gen.writeFieldName("intersections");
        writeIntersections(gen, msg.intersections());
        gen.writeEndObject();
    }

    private void writeIntersections(JsonGenerator gen, List<IntersectionState> intersections) throws IOException {
        gen.writeStartArray();
        for (IntersectionState is : intersections) writeIntersectionState(gen, is);
        gen.writeEndArray();
    }

    private void writeIntersectionState(JsonGenerator gen, IntersectionState is) throws IOException {
        gen.writeStartObject();
        if (is.name() != null) gen.writeStringField("name", is.name());
        gen.writeFieldName("id");
        writeIntersectionReferenceId(gen, is.id());
        gen.writeNumberField("revision", is.revision());
        writeStringArray(gen, "status", is.status());
        if (is.moy() != null) gen.writeNumberField("moy", is.moy());
        if (is.timestamp() != null) gen.writeNumberField("timestamp", is.timestamp());
        if (is.enabledLanes() != null && !is.enabledLanes().isEmpty()) {
            gen.writeFieldName("enabled_lanes");
            gen.writeStartArray();
            for (int lane : is.enabledLanes()) gen.writeNumber(lane);
            gen.writeEndArray();
        }
        gen.writeFieldName("states");
        writeMovementStates(gen, is.states());
        if (is.maneuverAssistList() != null && !is.maneuverAssistList().isEmpty()) {
            gen.writeFieldName("maneuver_assist_list");
            writeManeuverAssistList(gen, is.maneuverAssistList());
        }
        gen.writeEndObject();
    }

    private void writeIntersectionReferenceId(JsonGenerator gen, IntersectionReferenceId id) throws IOException {
        gen.writeStartObject();
        if (id.region() != null) gen.writeNumberField("region", id.region());
        gen.writeNumberField("id", id.id());
        gen.writeEndObject();
    }

    private void writeMovementStates(JsonGenerator gen, List<MovementState> states) throws IOException {
        gen.writeStartArray();
        for (MovementState ms : states) writeMovementState(gen, ms);
        gen.writeEndArray();
    }

    private void writeMovementState(JsonGenerator gen, MovementState ms) throws IOException {
        gen.writeStartObject();
        if (ms.movementName() != null) gen.writeStringField("movement_name", ms.movementName());
        gen.writeNumberField("signal_group", ms.signalGroup());
        gen.writeFieldName("state_time_speed");
        writeMovementEvents(gen, ms.stateTimeSpeed());
        if (ms.maneuverAssistList() != null && !ms.maneuverAssistList().isEmpty()) {
            gen.writeFieldName("maneuver_assist_list");
            writeManeuverAssistList(gen, ms.maneuverAssistList());
        }
        gen.writeEndObject();
    }

    private void writeMovementEvents(JsonGenerator gen, List<MovementEvent> events) throws IOException {
        gen.writeStartArray();
        for (MovementEvent me : events) writeMovementEvent(gen, me);
        gen.writeEndArray();
    }

    private void writeMovementEvent(JsonGenerator gen, MovementEvent me) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("event_state", me.eventState());
        if (me.timing() != null) {
            gen.writeFieldName("timing");
            writeTimeChangeDetail(gen, me.timing());
        }
        if (me.speeds() != null && !me.speeds().isEmpty()) {
            gen.writeFieldName("speeds");
            writeAdvisorySpeeds(gen, me.speeds());
        }
        gen.writeEndObject();
    }

    private void writeTimeChangeDetail(JsonGenerator gen, TimeChangeDetail t) throws IOException {
        gen.writeStartObject();
        if (t.startTime() != null)  gen.writeNumberField("start_time",  t.startTime());
        gen.writeNumberField("min_end_time", t.minEndTime());
        if (t.maxEndTime() != null) gen.writeNumberField("max_end_time", t.maxEndTime());
        if (t.likelyTime() != null) gen.writeNumberField("likely_time",  t.likelyTime());
        if (t.confidence() != null) gen.writeNumberField("confidence",   t.confidence());
        if (t.nextTime() != null)   gen.writeNumberField("next_time",    t.nextTime());
        gen.writeEndObject();
    }

    private void writeAdvisorySpeeds(JsonGenerator gen, List<AdvisorySpeed> speeds) throws IOException {
        gen.writeStartArray();
        for (AdvisorySpeed as : speeds) {
            gen.writeStartObject();
            gen.writeNumberField("type", as.type());
            if (as.speed() != null)      gen.writeNumberField("speed",      as.speed());
            if (as.confidence() != null) gen.writeNumberField("confidence", as.confidence());
            if (as.distance() != null)   gen.writeNumberField("distance",   as.distance());
            if (as.classId() != null)    gen.writeNumberField("class",      as.classId());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeManeuverAssistList(JsonGenerator gen, List<ManeuverAssist> list) throws IOException {
        gen.writeStartArray();
        for (ManeuverAssist ma : list) {
            gen.writeStartObject();
            gen.writeNumberField("connection_id", ma.connectionId());
            if (ma.queueLength() != null)            gen.writeNumberField("queue_length",             ma.queueLength());
            if (ma.availableStorageLength() != null) gen.writeNumberField("available_storage_length", ma.availableStorageLength());
            if (ma.waitOnStop() != null)             gen.writeBooleanField("wait_on_stop",             ma.waitOnStop());
            if (ma.pedBicycleDetect() != null)       gen.writeBooleanField("ped_bicycle_detect",       ma.pedBicycleDetect());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    private void writeStringArray(JsonGenerator gen, String fieldName, List<String> values) throws IOException {
        gen.writeFieldName(fieldName);
        gen.writeStartArray();
        if (values != null) for (String v : values) gen.writeString(v);
        gen.writeEndArray();
    }
}

