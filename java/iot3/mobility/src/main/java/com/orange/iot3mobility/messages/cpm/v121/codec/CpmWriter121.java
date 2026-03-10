/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.orange.iot3mobility.messages.cpm.v121.model.*;
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidator121;

import java.io.IOException;
import java.io.OutputStream;

public final class CpmWriter121 {

    private final JsonFactory jsonFactory;

    public CpmWriter121(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void write(CpmEnvelope121 envelope, OutputStream out) throws IOException {
        CpmValidator121.validateEnvelope(envelope);

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

    private void writeMessage(JsonGenerator gen, CpmMessage121 msg) throws IOException {
        //TODO
    }
}
