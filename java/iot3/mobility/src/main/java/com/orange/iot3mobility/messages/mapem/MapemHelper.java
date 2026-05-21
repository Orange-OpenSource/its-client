/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.mapem.core.MapemCodec;
import com.orange.iot3mobility.messages.mapem.core.MapemException;
import com.orange.iot3mobility.messages.mapem.core.MapemVersion;
import com.orange.iot3mobility.messages.mapem.v200.model.MapemEnvelope200;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * High-level helper around {@link MapemCodec}.
 * <p>
 * Manages a shared {@link JsonFactory} and {@link MapemCodec} instance.
 * Provides String-based APIs convenient for MQTT payloads.
 * Thread-safe: stateless, all shared components are immutable.
 */
public final class MapemHelper {

    private final JsonFactory jsonFactory;
    private final MapemCodec mapemCodec;

    /**
     * Default constructor: creates its own {@link JsonFactory}.
     */
    public MapemHelper() {
        this(new JsonFactory());
    }

    /**
     * Constructor with an externally-provided {@link JsonFactory}.
     */
    public MapemHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.mapemCodec = new MapemCodec(this.jsonFactory);
    }

    // -------------------------------------------------------------------------
    // Parsing
    // -------------------------------------------------------------------------

    /**
     * Parse a MAPEM JSON payload string; the version is auto-detected.
     *
     * @param jsonPayload JSON string containing a MAPEM envelope
     * @return a {@link MapemCodec.MapemFrame} with the detected version and typed envelope
     * @throws IOException  if the JSON is malformed or an I/O error occurs
     * @throws MapemException if the MAPEM structure is invalid
     */
    public MapemCodec.MapemFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");
        return mapemCodec.read(jsonPayload);
    }

    /**
     * Parse a MAPEM JSON payload and return a v2.0.0 envelope.
     * Throws if the detected version is not 2.0.0.
     */
    public MapemEnvelope200 parse200(String jsonPayload) throws IOException {
        MapemCodec.MapemFrame<?> frame = parse(jsonPayload);
        if (frame.version() != MapemVersion.V2_0_0) {
            throw new MapemException("Expected MAPEM version 2.0.0 but got " + frame.version());
        }
        return (MapemEnvelope200) frame.envelope();
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    /**
     * Serialize a v2.0.0 MAPEM envelope to a JSON string.
     */
    public String toJson(MapemEnvelope200 envelope) throws IOException {
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(MapemVersion.V2_0_0, envelope);
    }

    /**
     * Generic serialization to JSON string.
     */
    public String toJson(MapemVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private String writeToString(MapemVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        mapemCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}

