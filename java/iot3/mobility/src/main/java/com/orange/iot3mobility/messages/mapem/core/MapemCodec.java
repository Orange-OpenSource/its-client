/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.mapem.v200.codec.MapemReader200;
import com.orange.iot3mobility.messages.mapem.v200.codec.MapemWriter200;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode MAPEM envelopes across all supported versions.
 */
public final class MapemCodec {

    /**
     * Wrapper exposing both the detected MAPEM version and the decoded envelope.
     *
     * @param <T> Envelope type (currently only {@link MapemEnvelope200}).
     */
    public record MapemFrame<T>(MapemVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final MapemReader200 reader200;
    private final MapemWriter200 writer200;

    public MapemCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader200 = new MapemReader200(jsonFactory);
        this.writer200 = new MapemWriter200(jsonFactory);
    }

    /**
     * Reads and decodes a MAPEM JSON payload from an {@link InputStream}.
     * The version is auto-detected from the top-level "version" field.
     */
    public MapemFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        MapemVersion version = detectVersion(payload);
        return switch (version) {
            case V2_0_0 -> new MapemFrame<>(version, reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Reads and decodes a MAPEM JSON string.
     * Convenient for MQTT payloads that are received as {@link String}.
     */
    public MapemFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        MapemVersion version = detectVersion(payload);
        return switch (version) {
            case V2_0_0 -> new MapemFrame<>(version, reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes the given envelope to the provided {@link OutputStream}.
     */
    public void write(MapemVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V2_0_0 -> writer200.write(cast(envelope, MapemEnvelope200.class), out);
            default -> throw new MapemException("Unsupported version: " + version);
        }
    }

    private MapemVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new MapemException("Expected JSON object at root");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return MapemVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new MapemException("Missing 'version' field in MAPEM payload");
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new MapemException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}

