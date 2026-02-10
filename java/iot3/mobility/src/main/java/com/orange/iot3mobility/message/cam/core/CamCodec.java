package com.orange.iot3mobility.message.cam.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.message.cam.v113.codec.CamReader113;
import com.orange.iot3mobility.message.cam.v113.codec.CamWriter113;
import com.orange.iot3mobility.message.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.message.cam.v230.codec.CamReader230;
import com.orange.iot3mobility.message.cam.v230.codec.CamWriter230;
import com.orange.iot3mobility.message.cam.v230.model.CamEnvelope230;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unified entry point to decode/encode CAM envelopes across all supported versions.
 */
public final class CamCodec {

    /**
     * Wrapper exposing both the detected CAM version and the decoded envelope.
     */
    public record CamFrame<T>(CamVersion version, T envelope) {}

    private final JsonFactory jsonFactory;
    private final CamReader113 reader113;
    private final CamWriter113 writer113;
    private final CamReader230 reader230;
    private final CamWriter230 writer230;

    public CamCodec(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.reader113 = new CamReader113(jsonFactory);
        this.writer113 = new CamWriter113(jsonFactory);
        this.reader230 = new CamReader230(jsonFactory);
        this.writer230 = new CamWriter230(jsonFactory);
    }

    /**
     * Reads the full payload, detects the CAM version from the top-level "version" field,
     * then delegates to the appropriate reader. The returned {@link CamFrame} exposes both the version and the
     * strongly-typed envelope instance.
     */
    public CamFrame<?> read(InputStream in) throws IOException {
        byte[] payload = in.readAllBytes();
        CamVersion version = detectVersion(payload);

        return switch (version) {
            case V1_1_3 -> new CamFrame<>(version,
                    reader113.read(new ByteArrayInputStream(payload)));
            case V2_3_0 -> new CamFrame<>(version,
                    reader230.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Same as {@link #read(InputStream)} but takes a JSON String directly.
     * Convenient for MQTT payloads that are received as String.
     */
    public CamFrame<?> read(String json) throws IOException {
        Objects.requireNonNull(json, "json");
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        CamVersion version = detectVersion(payload);

        return switch (version) {
            case V1_1_3 -> new CamFrame<>(version,
                    reader113.read(new ByteArrayInputStream(payload)));
            case V2_3_0 -> new CamFrame<>(version,
                    reader230.read(new ByteArrayInputStream(payload)));
        };
    }

    /**
     * Writes an envelope using the writer that matches the provided version.
     */
    public void write(CamVersion version, Object envelope, OutputStream out) throws IOException {
        switch (version) {
            case V1_1_3 -> writer113.write(cast(envelope, CamEnvelope113.class), out);
            case V2_3_0 -> writer230.write(cast(envelope, CamEnvelope230.class), out);
            default -> throw new CamException("Unsupported version: " + version);
        }
    }

    /**
     * Parses only the top-level "version" field without building any tree (streaming mode).
     */
    private CamVersion detectVersion(byte[] payload) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(payload)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if ("version".equals(field)) {
                    return CamVersion.fromJsonValue(parser.getValueAsString());
                } else {
                    parser.skipChildren();
                }
            }
        }
        throw new CamException("Missing 'version' field in CAM payload");
    }

    private static void expect(JsonToken actual, JsonToken expected) {
        if (actual != expected) {
            throw new CamException("Expected token " + expected + " but got " + actual);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new CamException("Expected envelope of type " + type.getName()
                    + " but got " + (value == null ? "null" : value.getClass().getName()));
        }
        return (T) value;
    }
}
