/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmException;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level helper around CpmCodec.
 * <p>
 * - Manages a shared JsonFactory and CpmCodec instance.
 * - Provides String-based APIs (convenient for MQTT payloads).
 * - Thread-safe: stateless, all shared components are immutable.
 */
public final class CpmHelper {

    private final JsonFactory jsonFactory;
    private final CpmCodec cpmCodec;

    /**
     * Default constructor: creates its own JsonFactory.
     * Recommended in most cases.
     */
    public CpmHelper() {
        this(new JsonFactory());
    }

    /**
     * Constructor with externally-provided JsonFactory (for advanced usage,
     * e.g. custom Jackson configuration).
     */
    public CpmHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.cpmCodec = new CpmCodec(this.jsonFactory);
    }

    // ---------------------------------------------------------------------
    // Reading / parsing from String (e.g. MQTT payload)
    // ---------------------------------------------------------------------

    /**
     * Parse a CPM JSON payload (string) and let CpmCodec detect the version.
     *
     * @param jsonPayload JSON string containing a CPM envelope
     * @return a CpmFrame with detected version and typed envelope
     * @throws IOException   if JSON is malformed or I/O error in parser
     * @throws CpmException  (or subclasses) if the CPM structure/fields are invalid
     */
    public CpmCodec.CpmFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");

        return cpmCodec.read(jsonPayload);
    }

    /**
     * Parse a CPM JSON payload and cast it to a v1.2.1 envelope.
     * Throws if the version is not 1.2.1.
     */
    public CpmEnvelope121 parse121(String jsonPayload) throws IOException {
        CpmCodec.CpmFrame<?> frame = parse(jsonPayload);
        if (frame.version() != CpmVersion.V1_2_1) {
            throw new CpmException("Expected CPM version 1.2.1 but got " + frame.version());
        }
        return (CpmEnvelope121) frame.envelope();
    }

    /**
     * Parse a CPM JSON payload and cast it to a v2.1.1 envelope.
     * Throws if the version is not 2.1.1.
     */
    public CpmEnvelope211 parse211(String jsonPayload) throws IOException {
        CpmCodec.CpmFrame<?> frame = parse(jsonPayload);
        if (frame.version() != CpmVersion.V2_1_1) {
            throw new CpmException("Expected CPM version 2.1.1 but got " + frame.version());
        }
        return (CpmEnvelope211) frame.envelope();
    }

    // ---------------------------------------------------------------------
    // Writing / serializing to String
    // ---------------------------------------------------------------------

    /**
     * Serialize a v1.2.1 CPM envelope to a JSON string.
     */
    public String toJson(CpmEnvelope121 envelope121) throws IOException {
        Objects.requireNonNull(envelope121, "envelope121");
        return writeToString(CpmVersion.V1_2_1, envelope121);
    }

    /**
     * Serialize a v2.1.1 CPM envelope to a JSON string.
     */
    public String toJson(CpmEnvelope211 envelope211) throws IOException {
        Objects.requireNonNull(envelope211, "envelope211");
        return writeToString(CpmVersion.V2_1_1, envelope211);
    }

    /**
     * Generic entry point for CPM serialization to a JSON string.
     */
    public String toJson(CpmVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // ---------------------------------------------------------------------
    // Internal helper
    // ---------------------------------------------------------------------

    private String writeToString(CpmVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        cpmCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}

