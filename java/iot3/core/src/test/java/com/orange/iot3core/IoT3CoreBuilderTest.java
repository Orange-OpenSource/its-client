/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3core;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link IoT3Core.IoT3CoreBuilder}.
 *
 * <p>All tests that call {@code build()} deliberately omit {@code mqttParams()} so that
 * no real MQTT connection is attempted during testing.  The resulting {@link IoT3Core}
 * instance will simply have a null internal {@code MqttClient}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Null-guard validation on each builder method</li>
 *   <li>Default state of an instance built without MQTT params</li>
 *   <li>Idempotent / safe delegating methods when no client is present</li>
 * </ul>
 */
@DisplayName("IoT3CoreBuilder — validation and default state")
class IoT3CoreBuilderTest {

    private IoT3CoreCallback mockCallback;

    @BeforeEach
    void setUp() {
        mockCallback = mock(IoT3CoreCallback.class);
    }

    // ── mqttParams null-guard ─────────────────────────────────────────────────

    @Test
    @DisplayName("mqttParams(null host, …) throws IllegalArgumentException")
    void mqttParams_nullHost_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new IoT3Core.IoT3CoreBuilder()
                        .mqttParams(null, 1883, "user", "pass", "clientId", false)
        );
    }

    // ── telemetryParams null-guard ────────────────────────────────────────────

    @Test
    @DisplayName("telemetryParams(null host, …) throws IllegalArgumentException")
    void telemetryParams_nullHost_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new IoT3Core.IoT3CoreBuilder()
                        .telemetryParams("http", null, 4318, "/endpoint", "user", "pass")
        );
    }

    // ── lwm2mParams null-guards ───────────────────────────────────────────────

    @Test
    @DisplayName("lwm2mParams(null config, …) throws IllegalArgumentException")
    void lwm2mParams_nullConfig_throwsIllegalArgumentException() {
        Lwm2mDevice mockDevice = mock(Lwm2mDevice.class);
        assertThrows(IllegalArgumentException.class, () ->
                new IoT3Core.IoT3CoreBuilder()
                        .lwm2mParams(null, mockDevice)
        );
    }

    @Test
    @DisplayName("lwm2mParams(config, null device) throws IllegalArgumentException")
    void lwm2mParams_nullDevice_throwsIllegalArgumentException() {
        Lwm2mConfig mockConfig = mock(Lwm2mConfig.class);
        assertThrows(IllegalArgumentException.class, () ->
                new IoT3Core.IoT3CoreBuilder()
                        .lwm2mParams(mockConfig, null)
        );
    }

    // ── default state after build() (no MQTT params) ──────────────────────────

    @Test
    @DisplayName("isMqttConnected() is false when no MQTT params were supplied")
    void build_withNoMqttParams_isMqttConnectedIsFalse() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertFalse(core.isMqttConnected());
    }

    @Test
    @DisplayName("isMqttConnectionSecured() is false when no MQTT params were supplied")
    void build_withNoMqttParams_isMqttConnectionSecuredIsFalse() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertFalse(core.isMqttConnectionSecured());
    }

    // ── safe no-op delegation when MqttClient is absent ──────────────────────

    @Test
    @DisplayName("mqttPublish() does not throw when no MqttClient exists")
    void mqttPublish_withNoClient_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(() -> core.mqttPublish("test/topic", "hello"));
    }

    @Test
    @DisplayName("mqttPublish(retain) does not throw when no MqttClient exists")
    void mqttPublishRetain_withNoClient_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(() -> core.mqttPublish("test/topic", "hello", true));
    }

    @Test
    @DisplayName("mqttPublish(retain, qos, expiry) does not throw when no MqttClient exists")
    void mqttPublishFull_withNoClient_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(() -> core.mqttPublish("test/topic", "hello", false, 1, 60));
    }

    @Test
    @DisplayName("mqttSubscribe() does not throw when no MqttClient exists")
    void mqttSubscribe_withNoClient_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(() -> core.mqttSubscribe("test/topic"));
    }

    @Test
    @DisplayName("mqttUnsubscribe() does not throw when no MqttClient exists")
    void mqttUnsubscribe_withNoClient_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(() -> core.mqttUnsubscribe("test/topic"));
    }

    @Test
    @DisplayName("close() does not throw when no clients are initialised")
    void close_withNoClients_doesNotThrow() {
        IoT3Core core = new IoT3Core.IoT3CoreBuilder()
                .callback(mockCallback)
                .build();
        assertDoesNotThrow(core::close);
    }

    // ── builder fluent API ────────────────────────────────────────────────────

    @Test
    @DisplayName("mqttKeepAlive() can be set without throwing")
    void mqttKeepAlive_doesNotThrow() {
        assertDoesNotThrow(() ->
                new IoT3Core.IoT3CoreBuilder()
                        .mqttKeepAlive(30)
                        .callback(mockCallback)
                        .build()
        );
    }

    @Test
    @DisplayName("building with no parameters at all does not throw")
    void build_withNoParams_doesNotThrow() {
        // No mqttHost → no client created, should be safe
        assertDoesNotThrow(() -> new IoT3Core.IoT3CoreBuilder().build());
    }
}

