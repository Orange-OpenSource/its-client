/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.denm.v113.codec.DenmReader113;
import com.orange.iot3mobility.messages.denm.v113.codec.DenmWriter113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v220.codec.DenmReader220;
import com.orange.iot3mobility.messages.denm.v220.codec.DenmWriter220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode DENM envelopes across all supported versions.
 */
public final class DenmCodec {

    /**
     * Wrapper exposing both the detected DENM version and the decoded envelope.
     */
    public record DenmFrame<T>(DenmVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final DenmReader113 reader113;
    private final DenmWriter113 writer113;
    private final DenmReader220 reader220;
    private final DenmWriter220 writer220;

    public DenmCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader113 = new DenmReader113(jsonFactory);
        this.writer113 = new DenmWriter113(jsonFactory);
        this.reader220 = new DenmReader220(jsonFactory);
        this.writer220 = new DenmWriter220(jsonFactory);
    }

    /**
     * Reads the full payload, detects the DENM version from the top-level "version" field,
     * then delegates to the appropriate reader. The returned {@link DenmFrame} exposes both the version and the
     * strongly-typed envelope instance.
     */
    public DenmFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        DenmVersion version = detectVersion(payload);

        return switch (version) {
            case V1_1_3 -> new DenmFrame<>(version,
                    reader113.read(new ByteArrayInputStream(payload)));
            case V2_2_0 -> new DenmFrame<>(version,
                    reader220.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Same as {@link #read(InputStream)} but takes a JSON String directly.
     * Convenient for MQTT payloads that are received as String.
     */
    public DenmFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        DenmVersion version = detectVersion(payload);

        return switch (version) {
            case V1_1_3 -> new DenmFrame<>(version,
                    reader113.read(new ByteArrayInputStream(payload)));
            case V2_2_0 -> new DenmFrame<>(version,
                    reader220.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes an envelope using the writer that matches the provided version.
     */
    public void write(DenmVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V1_1_3 -> writer113.write(cast(envelope, DenmEnvelope113.class), out);
            case V2_2_0 -> writer220.write(cast(envelope, DenmEnvelope220.class), out);
            default -> throw new DenmException("Unsupported version: " + version);
        }
    }

    /**
     * Parses only the top-level "version" field without building any tree (streaming mode).
     */
    private DenmVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return DenmVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new DenmException("Missing 'version' field in DENM payload");
    }

    private static void expect(JsonToken actual, JsonToken expected) {
        if (actual != expected) {
            throw new DenmException("Expected token " + expected + " but got " + actual);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new DenmException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}

