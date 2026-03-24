/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.cpm.v121.codec.CpmReader121;
import com.orange.iot3mobility.messages.cpm.v121.codec.CpmWriter121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v211.codec.CpmReader211;
import com.orange.iot3mobility.messages.cpm.v211.codec.CpmWriter211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode CPM envelopes across all supported versions.
 */
public final class CpmCodec {

    /**
     * Wrapper exposing both the detected CPM version and the decoded envelope.
     */
    public record CpmFrame<T>(CpmVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final CpmReader121 reader121;
    private final CpmWriter121 writer121;
    private final CpmReader211 reader211;
    private final CpmWriter211 writer211;

    public CpmCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader121 = new CpmReader121(jsonFactory);
        this.writer121 = new CpmWriter121(jsonFactory);
        this.reader211 = new CpmReader211(jsonFactory);
        this.writer211 = new CpmWriter211(jsonFactory);
    }

    /**
     * Reads the full payload, detects the CPM version from the top-level "version" field,
     * then delegates to the appropriate reader. The returned {@link CpmFrame} exposes both the version and the
     * strongly-typed envelope instance.
     */
    public CpmFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        CpmVersion version = detectVersion(payload);

        return switch (version) {
            case V1_2_1 -> new CpmFrame<>(version,
                    reader121.read(new ByteArrayInputStream(payload)));
            case V2_1_1 -> new CpmFrame<>(version,
                    reader211.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Same as {@link #read(InputStream)} but takes a JSON String directly.
     * Convenient for MQTT payloads that are received as String.
     */
    public CpmFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        CpmVersion version = detectVersion(payload);

        return switch (version) {
            case V1_2_1 -> new CpmFrame<>(version, reader121.read(new ByteArrayInputStream(payload)));
            case V2_1_1 -> new CpmFrame<>(version, reader211.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes an envelope using the writer that matches the provided version.
     */
    public void write(CpmVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V1_2_1 -> writer121.write(cast(envelope, CpmEnvelope121.class), out);
            case V2_1_1 -> writer211.write(cast(envelope, CpmEnvelope211.class), out);
            default -> throw new CpmException("Unsupported version: " + version);
        }
    }

    /**
     * Parses only the top-level "version" field without building any tree (streaming mode).
     */
    private CpmVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return CpmVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new CpmException("Missing 'version' field in CPM payload");
    }

    private static void expect(JsonToken actual, JsonToken expected) {
        if (actual != expected) {
            throw new CpmException("Expected token " + expected + " but got " + actual);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new CpmException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}
