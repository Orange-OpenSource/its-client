/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmException;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Objects;

/**
 * High-level helper around DenmCodec. Also provides DENM a sequence number helper method.
 * <p>
 * - Manages a shared JsonFactory and DenmCodec instance.
 * - Provides String-based APIs (convenient for MQTT payloads).
 * - Thread-safe: stateless, all shared components are immutable.
 */
public final class DenmHelper {

    private final JsonFactory jsonFactory;
    private final DenmCodec denmCodec;

    /**
     * Default constructor: creates its own JsonFactory.
     * Recommended in most cases.
     */
    public DenmHelper() {
        this(new JsonFactory());
    }

    /**
     * Constructor with externally-provided JsonFactory (for advanced usage,
     * e.g. custom Jackson configuration).
     */
    public DenmHelper(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
        this.denmCodec = new DenmCodec(this.jsonFactory);
    }

    // ---------------------------------------------------------------------
    // Reading / parsing from String (e.g. MQTT payload)
    // ---------------------------------------------------------------------

    /**
     * Parse a DENM JSON payload (string) and let DenmCodec detect the version.
     *
     * @param jsonPayload JSON string containing a DENM envelope
     * @return a DenmFrame with detected version and typed envelope
     * @throws IOException   if JSON is malformed or I/O error in parser
     * @throws DenmException  (or subclasses) if the DENM structure/fields are invalid
     */
    public DenmCodec.DenmFrame<?> parse(String jsonPayload) throws IOException {
        Objects.requireNonNull(jsonPayload, "jsonPayload");

        return denmCodec.read(jsonPayload);
    }

    /**
     * Parse a DENM JSON payload and cast it to a v1.1.3 envelope.
     * Throws if the version is not 1.1.3.
     */
    public DenmEnvelope113 parse113(String jsonPayload) throws IOException {
        DenmCodec.DenmFrame<?> frame = parse(jsonPayload);
        if (frame.version() != DenmVersion.V1_1_3) {
            throw new DenmException("Expected DENM version 1.1.3 but got " + frame.version());
        }
        return (DenmEnvelope113) frame.envelope();
    }

    /**
     * Parse a DENM JSON payload and cast it to a v2.2.0 envelope.
     * Throws if the version is not 2.2.0.
     */
    public DenmEnvelope220 parse220(String jsonPayload) throws IOException {
        DenmCodec.DenmFrame<?> frame = parse(jsonPayload);
        if (frame.version() != DenmVersion.V2_2_0) {
            throw new DenmException("Expected DENM version 2.2.0 but got " + frame.version());
        }
        return (DenmEnvelope220) frame.envelope();
    }

    // ---------------------------------------------------------------------
    // Writing / serializing to String
    // ---------------------------------------------------------------------

    /**
     * Serialize a v1.1.3 DENM envelope to a JSON string.
     */
    public String toJson(DenmEnvelope113 envelope113) throws IOException {
        Objects.requireNonNull(envelope113, "envelope113");
        return writeToString(DenmVersion.V1_1_3, envelope113);
    }

    /**
     * Serialize a v2.2.0 DENM envelope to a JSON string.
     */
    public String toJson(DenmEnvelope220 envelope220) throws IOException {
        Objects.requireNonNull(envelope220, "envelope220");
        return writeToString(DenmVersion.V2_2_0, envelope220);
    }

    /**
     * Generic entry point for DENM serialization to a JSON string.
     */
    public String toJson(DenmVersion version, Object envelope) throws IOException {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(envelope, "envelope");
        return writeToString(version, envelope);
    }

    // ---------------------------------------------------------------------
    // Internal helper
    // ---------------------------------------------------------------------

    private String writeToString(DenmVersion version, Object envelope) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        denmCodec.write(version, envelope, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------------
    // Sequence number helper
    // ---------------------------------------------------------------------

    private static int localSequenceNumber = initLocalSequenceNumber();

    /**
     * Simple way to avoid getting the same sequence number after a restart (can cause issues to identify DENMs).
     */
    private static int initLocalSequenceNumber() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        int s = c.get(Calendar.SECOND);

        int secondsSinceMidnight = h * 3600 + m * 60 + s; // 0..86399
        return secondsSinceMidnight % 65536;              // 0..65535
    }

    /**
     * Helper method incrementing your DENM sequence number.
     *
     * @return the next sequence number for a new DENM
     */
    public static int getNextSequenceNumber() {
        localSequenceNumber++;
        if(localSequenceNumber > 65535) localSequenceNumber = 0;
        return localSequenceNumber;
    }
}

