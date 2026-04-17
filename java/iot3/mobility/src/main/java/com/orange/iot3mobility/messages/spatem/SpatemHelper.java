/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemException;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level helper around {@link SpatemCodec}.
 * <p>
 * Manages a shared {@link JsonFactory} and {@link SpatemCodec} instance.
 * Provides String-based APIs convenient for MQTT payloads.
 * Thread-safe: stateless, all shared components are immutable.
 */
public final class SpatemHelper {

    private final JsonFactory jsonFactory;
    private final SpatemCodec spatemCodec;

    /** Default constructor: creates its own {@link JsonFactory}. */
    public SpatemHelper() {
        this(new JsonFactory());
    }

    /** Constructor with an externally-provided {@link JsonFactory}. */
    public SpatemHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.spatemCodec = new SpatemCodec(this.jsonFactory);
    }

    // -------------------------------------------------------------------------
    // Parsing
    // -------------------------------------------------------------------------

    /**
     * Parse a SPATEM JSON payload string; the version is auto-detected.
     *
     * @param jsonPayload JSON string containing a SPATEM envelope
     * @return a {@link SpatemCodec.SpatemFrame} with the detected version and typed envelope
     * @throws IOException    if the JSON is malformed or an I/O error occurs
     * @throws SpatemException if the SPATEM structure is invalid
     */
    public SpatemCodec.SpatemFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");
        return spatemCodec.read(jsonPayload);
    }

    /**
     * Parse a SPATEM JSON payload and return a v2.0.0 envelope.
     * Throws if the detected version is not 2.0.0.
     */
    public SpatemEnvelope200 parse200(String jsonPayload) throws IOException {
        SpatemCodec.SpatemFrame<?> frame = parse(jsonPayload);
        if (frame.version() != SpatemVersion.V2_0_0) {
            throw new SpatemException("Expected SPATEM version 2.0.0 but got " + frame.version());
        }
        return (SpatemEnvelope200) frame.envelope();
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    /**
     * Serialize a v2.0.0 SPATEM envelope to a JSON string.
     */
    public String toJson(SpatemEnvelope200 envelope) throws IOException {
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(SpatemVersion.V2_0_0, envelope);
    }

    /**
     * Generic serialization to JSON string.
     */
    public String toJson(SpatemVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private String writeToString(SpatemVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
        spatemCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}

