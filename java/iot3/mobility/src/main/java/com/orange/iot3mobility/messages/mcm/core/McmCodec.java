/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.mcm.v200.codec.McmReader200;
import com.orange.iot3mobility.messages.mcm.v200.codec.McmWriter200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode MCM envelopes across all supported versions.
 */
public final class McmCodec {

    /**
     * Wrapper exposing both the detected MCM version and the decoded envelope.
     *
     * @param <T> concrete envelope type (e.g. {@link McmEnvelope200}).
     */
    public record McmFrame<T>(McmVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final McmReader200 reader200;
    private final McmWriter200 writer200;

    public McmCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader200 = new McmReader200(jsonFactory);
        this.writer200 = new McmWriter200(jsonFactory);
    }

    /**
     * Reads the full payload, detects the MCM version from the top-level "version" field,
     * then delegates to the appropriate reader.
     */
    public McmFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        McmVersion version = detectVersion(payload);

        return switch (version) {
            case V2_0_0 -> new McmFrame<>(version,
                    reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Same as {@link #read(InputStream)} but takes a JSON String directly.
     */
    public McmFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        McmVersion version = detectVersion(payload);

        return switch (version) {
            case V2_0_0 -> new McmFrame<>(version,
                    reader200.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes an envelope using the writer that matches the provided version.
     */
    public void write(McmVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V2_0_0 -> writer200.write(cast(envelope, McmEnvelope200.class), out);
            default -> throw new McmException("Unsupported version: " + version);
        }
    }

    /**
     * Parses only the top-level "version" field without building any tree (streaming mode).
     */
    private McmVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return McmVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new McmException("Missing 'version' field in MCM payload");
    }

    private static void expect(JsonToken actual, JsonToken expected) {
        if (actual != expected) {
            throw new McmException("Expected token " + expected + " but got " + actual);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new McmException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}

