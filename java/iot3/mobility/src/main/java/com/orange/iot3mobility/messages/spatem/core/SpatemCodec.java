/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.spatem.v200.codec.SpatemReader200;
import com.orange.iot3mobility.messages.spatem.v200.codec.SpatemWriter200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode SPATEM envelopes across all supported versions.
 */
public final class SpatemCodec {

    /**
     * Wrapper exposing both the detected SPATEM version and the decoded envelope.
     *
     * @param <T> Envelope type (currently only {@link SpatemEnvelope200}).
     */
    public record SpatemFrame<T>(SpatemVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final SpatemReader200 reader200;
    private final SpatemWriter200 writer200;

    public SpatemCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader200 = new SpatemReader200(jsonFactory);
        this.writer200 = new SpatemWriter200(jsonFactory);
    }

    /**
     * Reads and decodes a SPATEM JSON payload from an {@link InputStream}.
     */
    public SpatemFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        SpatemVersion version = detectVersion(payload);
        return switch (version) {
            case V2_0_0 -> new SpatemFrame<>(version, reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Reads and decodes a SPATEM JSON string.
     */
    public SpatemFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        SpatemVersion version = detectVersion(payload);
        return switch (version) {
            case V2_0_0 -> new SpatemFrame<>(version, reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes the given envelope to the provided {@link OutputStream}.
     */
    public void write(SpatemVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V2_0_0 -> writer200.write(cast(envelope, SpatemEnvelope200.class), out);
            default -> throw new SpatemException("Unsupported version: " + version);
        }
    }

    private SpatemVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new SpatemException("Expected JSON object at root");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return SpatemVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new SpatemException("Missing 'version' field in SPATEM payload");
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new SpatemException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}

