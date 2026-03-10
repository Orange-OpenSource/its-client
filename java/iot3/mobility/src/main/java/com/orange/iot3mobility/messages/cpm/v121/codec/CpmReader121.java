/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.cpm.v121.model.*;
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidationException;
import com.orange.iot3mobility.messages.cpm.v121.validation.CpmValidator121;

import java.io.IOException;
import java.io.InputStream;

public final class CpmReader121 {

    private final JsonFactory jsonFactory;

    public CpmReader121(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public CpmEnvelope121 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String type = null;
            String origin = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            CpmMessage121 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "type" -> type = parser.getValueAsString();
                    case "origin" -> origin = parser.getValueAsString();
                    case "version" -> version = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            CpmEnvelope121 envelope = new CpmEnvelope121(
                    requireField(type, "type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(message, "message"));

            CpmValidator121.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CpmMessage121 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        //TODO

        return new CpmMessage121();
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected " + expected + " but got " + actual);
        }
    }
}
