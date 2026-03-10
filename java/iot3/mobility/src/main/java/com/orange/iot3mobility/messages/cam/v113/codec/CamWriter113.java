/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.codec;

import com.fasterxml.jackson.core.*;
import com.orange.iot3mobility.messages.cam.v113.model.*;
import com.orange.iot3mobility.messages.cam.v113.validation.CamValidator113;

import java.io.IOException;
import java.io.OutputStream;

public final class CamWriter113 {

    private final JsonFactory jsonFactory;

    public CamWriter113(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(CamEnvelope113 envelope, OutputStream out) throws IOException {
        CamValidator113.validateEnvelope(envelope);

        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("type", envelope.type());
            gen.writeStringField("origin", envelope.origin());
            gen.writeStringField("version", envelope.version());
            gen.writeStringField("source_uuid", envelope.sourceUuid());
            gen.writeNumberField("timestamp", envelope.timestamp());
            gen.writeFieldName("message");
            writeMessage(gen, envelope.message());
            gen.writeEndObject();
        }
    }

    private void writeMessage(JsonGenerator gen, CamMessage113 msg) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("protocol_version", msg.protocolVersion());
        gen.writeNumberField("station_id", msg.stationId());
        gen.writeNumberField("generation_delta_time", msg.generationDeltaTime());

        gen.writeFieldName("basic_container");
        writeBasic(gen, msg.basicContainer());

        gen.writeFieldName("high_frequency_container");
        writeHighFrequency(gen, msg.highFrequencyContainer());

        if(msg.lowFrequencyContainer() != null) {
            gen.writeFieldName("low_frequency_container");
            writeLowFrequency(gen, msg.lowFrequencyContainer());
        }

        gen.writeEndObject();
    }

    private void writeBasic(JsonGenerator gen, BasicContainer basic) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("station_type", basic.stationType());

        gen.writeFieldName("reference_position");
        gen.writeStartObject();
        gen.writeNumberField("latitude", basic.referencePosition().latitude());
        gen.writeNumberField("longitude", basic.referencePosition().longitude());
        gen.writeNumberField("altitude", basic.referencePosition().altitude());
        gen.writeEndObject();

        if(basic.confidence() != null) {
            gen.writeFieldName("confidence");
            gen.writeStartObject();
            gen.writeFieldName("position_confidence_ellipse");
            gen.writeStartObject();
            gen.writeNumberField("semi_major_confidence", basic.confidence().ellipse().semiMajor());
            gen.writeNumberField("semi_minor_confidence", basic.confidence().ellipse().semiMinor());
            gen.writeNumberField("semi_major_orientation", basic.confidence().ellipse().semiMajorOrientation());
            gen.writeEndObject();
            gen.writeNumberField("altitude", basic.confidence().altitude());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private void writeHighFrequency(JsonGenerator gen, HighFrequencyContainer hf) throws IOException {
        gen.writeStartObject();
        if(hf.heading() != null)
            gen.writeNumberField("heading", hf.heading());
        if(hf.speed() != null)
            gen.writeNumberField("speed", hf.speed());
        if(hf.driveDirection() != null)
            gen.writeNumberField("drive_direction", hf.driveDirection());
        if(hf.vehicleLength() != null)
            gen.writeNumberField("vehicle_length", hf.vehicleLength());
        if(hf.vehicleWidth() != null)
            gen.writeNumberField("vehicle_width", hf.vehicleWidth());
        if(hf.curvature() != null)
            gen.writeNumberField("curvature", hf.curvature());
        if(hf.curvatureCalculationMode() != null)
            gen.writeNumberField("curvature_calculation_mode", hf.curvatureCalculationMode());
        if(hf.longitudinalAcceleration() != null)
            gen.writeNumberField("longitudinal_acceleration", hf.longitudinalAcceleration());
        if(hf.yawRate() != null)
            gen.writeNumberField("yaw_rate", hf.yawRate());
        if(hf.accelerationControl() != null)
            gen.writeStringField("acceleration_control", hf.accelerationControl());
        if (hf.lanePosition() != null)
            gen.writeNumberField("lane_position", hf.lanePosition());
        if (hf.lateralAcceleration() != null)
            gen.writeNumberField("lateral_acceleration", hf.lateralAcceleration());
        if (hf.verticalAcceleration() != null)
            gen.writeNumberField("vertical_acceleration", hf.verticalAcceleration());
        if(hf.confidence() != null) {
            gen.writeFieldName("confidence");
            gen.writeStartObject();
            HighFrequencyConfidence conf = hf.confidence();
            if(conf.heading() != null)
                gen.writeNumberField("heading", conf.heading());
            if(conf.speed() != null)
                gen.writeNumberField("speed", conf.speed());
            if(conf.vehicleLength() != null)
                gen.writeNumberField("vehicle_length", conf.vehicleLength());
            if(conf.yawRate() != null)
                gen.writeNumberField("yaw_rate", conf.yawRate());
            if(conf.longitudinalAcceleration() != null)
                gen.writeNumberField("longitudinal_acceleration", conf.longitudinalAcceleration());
            if(conf.curvature() != null)
                gen.writeNumberField("curvature", conf.curvature());
            if(conf.lateralAcceleration() != null)
                gen.writeNumberField("lateral_acceleration", conf.lateralAcceleration());
            if(conf.verticalAcceleration() != null)
                gen.writeNumberField("vertical_acceleration", conf.verticalAcceleration());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private void writeLowFrequency(JsonGenerator gen, LowFrequencyContainer lf) throws IOException {
        gen.writeStartObject();
        if(lf.vehicleRole() != null)
            gen.writeNumberField("vehicle_role", lf.vehicleRole());
        gen.writeStringField("exterior_lights", lf.exteriorLights());
        gen.writeFieldName("path_history");
        gen.writeStartArray();
        for (PathPoint point : lf.pathHistory()) {
            gen.writeStartObject();
            gen.writeFieldName("path_position");
            gen.writeStartObject();
            gen.writeNumberField("delta_latitude", point.deltaPosition().deltaLatitude());
            gen.writeNumberField("delta_longitude", point.deltaPosition().deltaLongitude());
            gen.writeNumberField("delta_altitude", point.deltaPosition().deltaAltitude());
            gen.writeEndObject();
            if (point.deltaTime() != null) {
                gen.writeNumberField("path_delta_time", point.deltaTime());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
