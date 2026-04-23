/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3core.bootstrap;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BootstrapHelper#bootstrap} using an in-process {@link MockWebServer}.
 *
 * <p>Covers:
 * <ul>
 *   <li>HTTP 200 with a valid JSON response → {@code boostrapSuccess} called</li>
 *   <li>HTTP 4xx / 5xx → {@code boostrapError} called with the status code in the message</li>
 *   <li>HTTP 200 with malformed JSON → {@code boostrapError} called</li>
 *   <li>HTTP 200 with missing required fields → {@code boostrapError} called</li>
 *   <li>Invalid URL → {@code boostrapError} called immediately</li>
 *   <li>Request contains the expected JSON body fields and Basic-Auth header</li>
 * </ul>
 */
@DisplayName("BootstrapHelper — HTTP flow")
class BootstrapHelperTest {

    private MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String bootstrapUrl() {
        return server.url("/bootstrap").toString();
    }

    /** Builds the JSON body that a real bootstrap server would return. */
    private static String buildValidResponseBody() {
        JSONObject json = new JSONObject();
        json.put("iot3_id", "my-iot3-id");
        json.put("psk_iot3_id", "my-psk-login");
        json.put("psk_iot3_secret", "my-psk-secret");

        JSONObject services = new JSONObject();
        services.put("message",  new JSONArray()
                .put(new JSONObject().put("uri", "mqtt://broker.example.com:1883")));
        services.put("telemetry", new JSONArray()
                .put(new JSONObject().put("uri", "http://otel.example.com:4318")));
        services.put("api", new JSONArray()
                .put(new JSONObject().put("uri", "http://jaeger.example.com:16686")));
        json.put("services", services);
        return json.toString();
    }

    /** Invokes bootstrap and returns a pair of (successConfig, errorThrowable) via AtomicReferences. */
    private record CallResult(AtomicReference<BootstrapConfig> config,
                              AtomicReference<Throwable> error) {}

    private CallResult callBootstrap(String url) {
        AtomicReference<BootstrapConfig> config = new AtomicReference<>();
        AtomicReference<Throwable>       error  = new AtomicReference<>();

        BootstrapHelper.bootstrap(
                "device-id", "login", "password",
                BootstrapHelper.Role.USER_EQUIPMENT,
                url,
                new BootstrapCallback() {
                    @Override public void boostrapSuccess(BootstrapConfig c) { config.set(c); }
                    @Override public void boostrapError(Throwable t)         { error.set(t); }
                }
        );
        return new CallResult(config, error);
    }

    // ── success path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("HTTP 200 with valid JSON → boostrapSuccess is called with a populated BootstrapConfig")
    void http200ValidJson_callsBootstrapSuccess() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(buildValidResponseBody()));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNotNull(result.config().get(), "boostrapSuccess should have been called");
        assertNull(result.error().get(),    "boostrapError should NOT have been called");
        assertEquals("my-iot3-id",    result.config().get().getIot3Id());
        assertEquals("my-psk-login",  result.config().get().getPskRunLogin());
        assertEquals("my-psk-secret", result.config().get().getPskRunPassword());
    }

    @Test
    @DisplayName("Request carries the correct Content-Type and Basic-Auth headers")
    void http200_requestHasCorrectHeaders() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(buildValidResponseBody()));

        callBootstrap(bootstrapUrl());

        RecordedRequest request = server.takeRequest();
        // OkHttp appends "; charset=utf-8" to the Content-Type, so check with startsWith
        assertTrue(Objects.requireNonNull(request.getHeader("Content-Type")).startsWith("application/json"),
                "Content-Type should be application/json (charset suffix is acceptable)");
        String auth = request.getHeader("Authorization");
        assertNotNull(auth);
        assertTrue(auth.startsWith("Basic "), "Authorization header should use Basic scheme");
    }

    @Test
    @DisplayName("Request body contains ue_id, psk_login, psk_password, and role fields")
    void http200_requestBodyContainsExpectedFields() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(buildValidResponseBody()));

        callBootstrap(bootstrapUrl());

        RecordedRequest request = server.takeRequest();
        JSONObject body = new JSONObject(request.getBody().readUtf8());
        assertEquals("device-id",        body.getString("ue_id"));
        assertEquals("login",             body.getString("psk_login"));
        assertEquals("password",          body.getString("psk_password"));
        assertEquals("user-equipment",    body.getString("role"));
    }

    // ── HTTP error codes ──────────────────────────────────────────────────────

    @Test
    @DisplayName("HTTP 400 → boostrapError is called and message contains the status code")
    void http400_callsBootstrapError() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad Request"));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNull(result.config().get(),   "boostrapSuccess should NOT have been called");
        assertNotNull(result.error().get(), "boostrapError should have been called");
        assertTrue(result.error().get().getMessage().contains("400"));
    }

    @Test
    @DisplayName("HTTP 401 → boostrapError is called")
    void http401_callsBootstrapError() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNotNull(result.error().get());
        assertTrue(result.error().get().getMessage().contains("401"));
    }

    @Test
    @DisplayName("HTTP 500 → boostrapError is called and message contains the status code")
    void http500_callsBootstrapError() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNull(result.config().get());
        assertNotNull(result.error().get());
        assertTrue(result.error().get().getMessage().contains("500"));
    }

    // ── malformed / incomplete response bodies ────────────────────────────────

    @Test
    @DisplayName("HTTP 200 with non-JSON body → boostrapError is called")
    void http200MalformedJson_callsBootstrapError() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("this-is-not-json"));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNull(result.config().get());
        assertNotNull(result.error().get());
    }

    @Test
    @DisplayName("HTTP 200 with JSON missing required fields → boostrapError is called")
    void http200MissingFields_callsBootstrapError() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"unexpected_key\": \"value\"}"));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNull(result.config().get());
        assertNotNull(result.error().get());
    }

    @Test
    @DisplayName("HTTP 200 with JSON missing 'services' key → boostrapError is called")
    void http200MissingServicesKey_callsBootstrapError() {
        JSONObject partial = new JSONObject();
        partial.put("iot3_id", "id");
        partial.put("psk_iot3_id", "login");
        partial.put("psk_iot3_secret", "secret");
        // no "services" key

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(partial.toString()));

        CallResult result = callBootstrap(bootstrapUrl());

        assertNull(result.config().get());
        assertNotNull(result.error().get());
    }

    // ── network / URL errors ──────────────────────────────────────────────────

    @Test
    @DisplayName("completely invalid URL → boostrapError is called immediately")
    void invalidUrl_callsBootstrapError() {
        CallResult result = callBootstrap("not-a-valid-url-at-all");

        assertNull(result.config().get());
        assertNotNull(result.error().get());
    }
}

