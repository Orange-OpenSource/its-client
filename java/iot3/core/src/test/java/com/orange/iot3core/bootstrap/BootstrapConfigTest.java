/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.bootstrap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BootstrapConfig} JSON parsing, protocol selection and security detection.
 * All tests work in-memory — no network access required.
 */
@DisplayName("BootstrapConfig — JSON parsing and service URI resolution")
class BootstrapConfigTest {

    // ── JSON builder helpers ─────────────────────────────────────────────────

    /**
     * Builds a minimal valid JSON response for the bootstrap endpoint.
     *
     * @param includeMqtts when true, adds an {@code mqtts://} URI alongside the plain {@code mqtt://} one
     * @param includeHttps when true, uses {@code https://} and {@code https://} for telemetry and api services
     */
    private static JSONObject buildValidJson(boolean includeMqtts, boolean includeHttps) {
        JSONObject json = new JSONObject();
        json.put("iot3_id", "test-id");
        json.put("psk_iot3_id", "test-login");
        json.put("psk_iot3_secret", "test-secret");

        JSONObject services = new JSONObject();

        // message service
        JSONArray message = new JSONArray();
        message.put(new JSONObject().put("uri", "mqtt://broker.example.com:1883"));
        if (includeMqtts) {
            message.put(new JSONObject().put("uri", "mqtts://broker.example.com:8883"));
        }
        services.put("message", message);

        // telemetry service
        String telemetryScheme = includeHttps ? "https" : "http";
        JSONArray telemetry = new JSONArray();
        telemetry.put(new JSONObject().put("uri", telemetryScheme + "://otel.example.com:4318"));
        services.put("telemetry", telemetry);

        // api (Jaeger) service
        String apiScheme = includeHttps ? "https" : "http";
        JSONArray api = new JSONArray();
        api.put(new JSONObject().put("uri", apiScheme + "://jaeger.example.com:16686"));
        services.put("api", api);

        json.put("services", services);
        return json;
    }

    // ── basic field parsing ───────────────────────────────────────────────────

    @Test
    @DisplayName("valid JSON — iot3_id, psk_iot3_id, psk_iot3_secret are parsed correctly")
    void validJson_parsesBasicFields() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        assertEquals("test-id",     config.getIot3Id());
        assertEquals("test-login",  config.getPskRunLogin());
        assertEquals("test-secret", config.getPskRunPassword());
    }

    @Test
    @DisplayName("valid JSON with all protocols — all protocol URIs are non-null")
    void validJson_withAllProtocols_parsesAll() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(true, true));
        assertNotNull(config.getProtocolUri(BootstrapConfig.Protocol.MQTT));
        assertNotNull(config.getProtocolUri(BootstrapConfig.Protocol.MQTTS));
        assertNotNull(config.getProtocolUri(BootstrapConfig.Protocol.OTLP_HTTPS));
        assertNotNull(config.getProtocolUri(BootstrapConfig.Protocol.JAEGER_HTTPS));
    }

    @Test
    @DisplayName("getProtocolUri(MQTT) returns the correct host and port")
    void getProtocolUri_mqtt_returnsCorrectHostAndPort() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        URI uri = config.getProtocolUri(BootstrapConfig.Protocol.MQTT);
        assertNotNull(uri);
        assertEquals("broker.example.com", uri.getHost());
        assertEquals(1883, uri.getPort());
    }

    @Test
    @DisplayName("getProtocolUri for an absent protocol returns null")
    void getProtocolUri_absentProtocol_returnsNull() throws Exception {
        // JSON only has plain mqtt, not mqtts
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        assertNull(config.getProtocolUri(BootstrapConfig.Protocol.MQTTS));
    }

    // ── service URI selection (prefers secured) ───────────────────────────────

    @Test
    @DisplayName("getServiceUri(MQTT) prefers mqtts over mqtt when both are present")
    void getServiceUri_mqtt_prefersMqttsOverMqtt() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(true, false));
        URI uri = config.getServiceUri(BootstrapConfig.Service.MQTT);
        assertNotNull(uri);
        assertEquals("mqtts", uri.getScheme());
    }

    @Test
    @DisplayName("getServiceUri(MQTT) falls back to mqtt when mqtts is absent")
    void getServiceUri_mqtt_fallsBackToMqtt() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        URI uri = config.getServiceUri(BootstrapConfig.Service.MQTT);
        assertNotNull(uri);
        assertEquals("mqtt", uri.getScheme());
    }

    @Test
    @DisplayName("getServiceUri(OPEN_TELEMETRY) prefers https over http")
    void getServiceUri_openTelemetry_prefersHttpsOverHttp() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, true));
        URI uri = config.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
        assertNotNull(uri);
        assertEquals("https", uri.getScheme());
    }

    @Test
    @DisplayName("getServiceUri(OPEN_TELEMETRY) falls back to http when https is absent")
    void getServiceUri_openTelemetry_fallsBackToHttp() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        URI uri = config.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
        assertNotNull(uri);
        assertEquals("http", uri.getScheme());
    }

    @Test
    @DisplayName("getServiceUri(JAEGER) prefers https over http")
    void getServiceUri_jaeger_prefersHttpsOverHttp() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, true));
        URI uri = config.getServiceUri(BootstrapConfig.Service.JAEGER);
        assertNotNull(uri);
        assertEquals("https", uri.getScheme());
    }

    // ── isServiceSecured() ────────────────────────────────────────────────────

    @Test
    @DisplayName("isServiceSecured(MQTT) is true when mqtts URI is present")
    void isServiceSecured_mqtt_trueWhenMqttsPresent() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(true, false));
        assertTrue(config.isServiceSecured(BootstrapConfig.Service.MQTT));
    }

    @Test
    @DisplayName("isServiceSecured(MQTT) is false when only plain mqtt is present")
    void isServiceSecured_mqtt_falseWhenOnlyPlainMqtt() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        assertFalse(config.isServiceSecured(BootstrapConfig.Service.MQTT));
    }

    @Test
    @DisplayName("isServiceSecured(OPEN_TELEMETRY) is true when https URI is present")
    void isServiceSecured_openTelemetry_trueWhenHttpsPresent() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, true));
        assertTrue(config.isServiceSecured(BootstrapConfig.Service.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("isServiceSecured(OPEN_TELEMETRY) is false when only http URI is present")
    void isServiceSecured_openTelemetry_falseWhenOnlyHttp() throws Exception {
        BootstrapConfig config = new BootstrapConfig(buildValidJson(false, false));
        assertFalse(config.isServiceSecured(BootstrapConfig.Service.OPEN_TELEMETRY));
    }

    // ── missing required service keys ─────────────────────────────────────────

    @Test
    @DisplayName("JSON without 'message' service key throws Exception")
    void missingMessageKey_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services").remove("message");
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    @Test
    @DisplayName("JSON without 'telemetry' service key throws Exception")
    void missingTelemetryKey_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services").remove("telemetry");
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    @Test
    @DisplayName("JSON without 'api' service key throws Exception")
    void missingApiKey_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services").remove("api");
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    // ── unknown URI schemes ───────────────────────────────────────────────────

    @Test
    @DisplayName("unknown scheme in 'message' service throws Exception")
    void unknownMessageScheme_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services")
            .put("message", new JSONArray().put(new JSONObject().put("uri", "ftp://broker.example.com:21")));
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    @Test
    @DisplayName("unknown scheme in 'telemetry' service throws Exception")
    void unknownTelemetryScheme_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services")
            .put("telemetry", new JSONArray().put(new JSONObject().put("uri", "ftp://otel.example.com:21")));
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    @Test
    @DisplayName("unknown scheme in 'api' service throws Exception")
    void unknownApiScheme_throwsException() {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services")
            .put("api", new JSONArray().put(new JSONObject().put("uri", "ftp://jaeger.example.com:21")));
        assertThrows(Exception.class, () -> new BootstrapConfig(json));
    }

    // ── MQTT Websocket ────────────────────────────────────────────────────────

    @Test
    @DisplayName("mqtt+ws URI is parsed and returned for MQTT_WEBSOCKET service")
    void mqttWsUri_parsedCorrectly() throws Exception {
        JSONObject json = buildValidJson(false, false);
        json.getJSONObject("services").getJSONArray("message")
            .put(new JSONObject().put("uri", "mqtt+ws://broker.example.com:8080"));
        BootstrapConfig config = new BootstrapConfig(json);
        URI wsUri = config.getProtocolUri(BootstrapConfig.Protocol.MQTT_WS);
        assertNotNull(wsUri);
        assertEquals("mqtt+ws", wsUri.getScheme());
    }
}

