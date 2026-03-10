/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.validation.CpmValidationException;
import com.orange.iot3mobility.messages.cpm.v211.validation.CpmValidator211;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Streaming JSON reader for CPM 2.1.1
 */
public final class CpmReader211 {

    private final JsonFactory jsonFactory;

    public CpmReader211(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public CpmEnvelope211 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String sourceUuid = null;
            Long timestamp = null;
            String version = null;
            CpmMessage211 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "version" -> version = parser.getValueAsString();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            CpmEnvelope211 envelope = new CpmEnvelope211(
                    messageType,
                    sourceUuid,
                    requireField(timestamp, "timestamp"),
                    requireField(version, "version"),
                    requireField(message, "message"));

            CpmValidator211.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CpmMessage211 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

       //TODO

        return new CpmMessage211();
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CpmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected token " + expected + " but got " + actual);
        }
    }
}