/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.messages.mcm.core.McmException;
import com.orange.iot3mobility.messages.mcm.core.McmVersion;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level helper around {@link McmCodec}.
 * <p>
 * - Manages a shared {@link JsonFactory} and {@link McmCodec} instance.
 * - Provides String-based APIs (convenient for MQTT payloads).
 * - Thread-safe: stateless; all shared components are immutable.
 */
public final class McmHelper {

    private final JsonFactory jsonFactory;
    private final McmCodec mcmCodec;

    /**
     * Default constructor: creates its own {@link JsonFactory}.
     * Recommended in most cases.
     */
    public McmHelper() {
        this(new JsonFactory());
    }

    /**
     * Constructor with an externally provided {@link JsonFactory}
     * (for advanced usage, e.g. custom Jackson configuration).
     */
    public McmHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.mcmCodec = new McmCodec(this.jsonFactory);
    }

    // -------------------------------------------------------------------------
    // Reading / parsing from String (e.g. MQTT payload)
    // -------------------------------------------------------------------------

    /**
     * Parse an MCM JSON payload and let {@link McmCodec} detect the version automatically.
     *
     * @param jsonPayload JSON string containing an MCM envelope.
     * @return an {@link McmCodec.McmFrame} with the detected version and typed envelope.
     * @throws IOException   if the JSON is malformed or an I/O error occurs in the parser.
     * @throws McmException  if the MCM structure or fields are invalid.
     */
    public McmCodec.McmFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");
        return mcmCodec.read(jsonPayload);
    }

    /**
     * Parse an MCM JSON payload and cast it to a v2.0.0 envelope.
     * Throws if the detected version is not 2.0.0.
     */
    public McmEnvelope200 parse200(String jsonPayload) throws IOException {
        McmCodec.McmFrame<?> frame = parse(jsonPayload);
        if (frame.version() != McmVersion.V2_0_0) {
            throw new McmException("Expected MCM version 2.0.0 but got " + frame.version());
        }
        return (McmEnvelope200) frame.envelope();
    }

    // -------------------------------------------------------------------------
    // Writing / serialising to String
    // -------------------------------------------------------------------------

    /**
     * Serialise a v2.0.0 MCM envelope to a JSON string.
     */
    public String toJson(McmEnvelope200 envelope200) throws IOException {
        Objects.requireNonNull(envelope200, "envelope200");
        return writeToString(McmVersion.V2_0_0, envelope200);
    }

    /**
     * Generic entry point for MCM serialisation to a JSON string.
     */
    public String toJson(McmVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // -------------------------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------------------------

    private String writeToString(McmVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        mcmCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}

