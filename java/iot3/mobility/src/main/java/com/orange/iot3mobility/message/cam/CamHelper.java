/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.message.cam.core.CamCodec;
import com.orange.iot3mobility.message.cam.core.CamException;
import com.orange.iot3mobility.message.cam.core.CamVersion;
import com.orange.iot3mobility.message.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.message.cam.v230.model.CamEnvelope230;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level helper around CamCodec.
 * <p>
 * - Manages a shared JsonFactory and CamCodec instance.
 * - Provides String-based APIs (convenient for MQTT payloads).
 * - Thread-safe: stateless, all shared components are immutable.
 */
public final class CamHelper {

    private final JsonFactory jsonFactory;
    private final CamCodec camCodec;

    /**
     * Default constructor: creates its own JsonFactory.
     * Recommended in most cases.
     */
    public CamHelper() {
        this(new JsonFactory());
    }

    /**
     * Constructor with externally-provided JsonFactory (for advanced usage,
     * e.g. custom Jackson configuration).
     */
    public CamHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.camCodec = new CamCodec(this.jsonFactory);
    }

    // ---------------------------------------------------------------------
    // Reading / parsing from String (e.g. MQTT payload)
    // ---------------------------------------------------------------------

    /**
     * Parse a CAM JSON payload (string) and let CamCodec detect the version.
     *
     * @param jsonPayload JSON string containing a CAM envelope
     * @return a CamFrame with detected version and typed envelope
     * @throws IOException   if JSON is malformed or I/O error in parser
     * @throws CamException  (or subclasses) if the CAM structure/fields are invalid
     */
    public CamCodec.CamFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");

        return camCodec.read(jsonPayload);
    }

    /**
     * Parse a CAM JSON payload and cast it to a v1.1.3 envelope.
     * Throws if the version is not 1.1.3.
     */
    public CamEnvelope113 parse113(String jsonPayload) throws IOException {
        CamCodec.CamFrame<?> frame = parse(jsonPayload);
        if (frame.version() != CamVersion.V1_1_3) {
            throw new CamException("Expected CAM version 1.1.3 but got " + frame.version());
        }
        return (CamEnvelope113) frame.envelope();
    }

    /**
     * Parse a CAM JSON payload and cast it to a v2.3.0 envelope.
     * Throws if the version is not 2.3.0.
     */
    public CamEnvelope230 parse230(String jsonPayload) throws IOException {
        CamCodec.CamFrame<?> frame = parse(jsonPayload);
        if (frame.version() != CamVersion.V2_3_0) {
            throw new CamException("Expected CAM version 2.3.0 but got " + frame.version());
        }
        return (CamEnvelope230) frame.envelope();
    }

    // ---------------------------------------------------------------------
    // Writing / serializing to String
    // ---------------------------------------------------------------------

    /**
     * Serialize a v1.1.3 CAM envelope to a JSON string.
     */
    public String toJson(CamEnvelope113 envelope113) throws IOException {
        Objects.requireNonNull(envelope113, "envelope113");
        return writeToString(CamVersion.V1_1_3, envelope113);
    }

    /**
     * Serialize a v2.3.0 CAM envelope to a JSON string.
     */
    public String toJson(CamEnvelope230 envelope230) throws IOException {
        Objects.requireNonNull(envelope230, "envelope230");
        return writeToString(CamVersion.V2_3_0, envelope230);
    }

    /**
     * Generic entry point for CAM serialization to a JSON string.
     */
    public String toJson(CamVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // ---------------------------------------------------------------------
    // Internal helper
    // ---------------------------------------------------------------------

    private String writeToString(CamVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        camCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}

