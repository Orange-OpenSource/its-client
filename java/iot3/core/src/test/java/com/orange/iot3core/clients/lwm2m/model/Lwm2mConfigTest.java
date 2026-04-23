/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3core.clients.lwm2m.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LwM2M data-model classes:
 * {@link Lwm2mConfig.Lwm2mBootstrapConfig}, {@link Lwm2mConfig.Lwm2mClassicConfig},
 * and {@link Lwm2mServer}.
 *
 * <p>All classes are pure value objects (no I/O, no network), so no mocking is required.
 */
@DisplayName("LwM2M config model — field storage and defaults")
class Lwm2mConfigTest {

    /** Shared server config used as a dependency in both config sub-types. */
    private Lwm2mServer serverConfig;

    @BeforeEach
    void setUp() {
        // "U" = UDP, the standard LwM2M binding mode
        serverConfig = new Lwm2mServer(1, 300, "U");
    }

    // ── Lwm2mServer ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Lwm2mServer stores shortServerId correctly")
    void server_storesShortServerId() {
        Lwm2mServer server = new Lwm2mServer(99, 600, "U");
        assertEquals(99, server.getShortServerId());
    }

    @Test
    @DisplayName("Lwm2mServer stores lifetime correctly")
    void server_storesLifetime() {
        Lwm2mServer server = new Lwm2mServer(1, 600, "U");
        assertEquals(600, server.getLifetime());
    }

    @Test
    @DisplayName("Lwm2mServer notifyWhenDisable defaults to false")
    void server_notifyWhenDisable_defaultFalse() {
        Lwm2mServer server = new Lwm2mServer(1, 300, "U");
        assertFalse(server.isNotifyWhenDisable());
    }

    @Test
    @DisplayName("Lwm2mServer notifyWhenDisable can be set to true")
    void server_notifyWhenDisable_canBeEnabled() {
        Lwm2mServer server = new Lwm2mServer(1, 300, "U", true);
        assertTrue(server.isNotifyWhenDisable());
    }

    @Test
    @DisplayName("Lwm2mServer bindingModes is non-null and non-empty")
    void server_bindingModes_nonEmpty() {
        assertNotNull(serverConfig.getBindingModes());
        assertFalse(serverConfig.getBindingModes().isEmpty());
    }

    @Test
    @DisplayName("Lwm2mServer preferredTransport is non-null")
    void server_preferredTransport_nonNull() {
        assertNotNull(serverConfig.getPreferredTransport());
    }

    // ── Lwm2mBootstrapConfig ──────────────────────────────────────────────────

    @Test
    @DisplayName("BootstrapConfig stores endpointName correctly")
    void bootstrap_storesEndpointName() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "my-endpoint", "coaps://bootstrap.example.com:5684",
                "psk-identity", "deadbeef01", serverConfig
        );
        assertEquals("my-endpoint", config.getEndpointName());
    }

    @Test
    @DisplayName("BootstrapConfig stores URI correctly")
    void bootstrap_storesUri() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "coaps://bootstrap.example.com:5684",
                "psk-identity", "deadbeef01", serverConfig
        );
        assertEquals("coaps://bootstrap.example.com:5684", config.getUri());
    }

    @Test
    @DisplayName("BootstrapConfig stores PSK identity correctly")
    void bootstrap_storesPskIdentity() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "coaps://bs.example.com:5684",
                "my-psk-id", "deadbeef01", serverConfig
        );
        assertEquals("my-psk-id", config.getPskIdentity());
    }

    @Test
    @DisplayName("BootstrapConfig stores private key correctly")
    void bootstrap_storesPrivateKey() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "coaps://bs.example.com:5684",
                "psk-id", "cafebabe42", serverConfig
        );
        assertEquals("cafebabe42", config.getPrivateKey());
    }

    @Test
    @DisplayName("BootstrapConfig stores serverConfig reference")
    void bootstrap_storesServerConfig() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "uri", "id", "key", serverConfig
        );
        assertSame(serverConfig, config.getServerConfig());
    }

    @Test
    @DisplayName("BootstrapConfig queueMode defaults to false")
    void bootstrap_queueMode_defaultFalse() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "uri", "id", "key", serverConfig
        );
        assertFalse(config.isQueueMode());
    }

    @Test
    @DisplayName("BootstrapConfig queueMode can be set to true")
    void bootstrap_queueMode_canBeEnabled() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mBootstrapConfig(
                "ep", "uri", "id", "key", serverConfig, true
        );
        assertTrue(config.isQueueMode());
    }

    // ── Lwm2mClassicConfig ────────────────────────────────────────────────────

    @Test
    @DisplayName("ClassicConfig stores endpointName correctly")
    void classic_storesEndpointName() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "classic-endpoint", "coaps://server.example.com:5684",
                "psk-id", "deadbeef01", 1, serverConfig
        );
        assertEquals("classic-endpoint", config.getEndpointName());
    }

    @Test
    @DisplayName("ClassicConfig stores URI correctly")
    void classic_storesUri() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "coaps://server.example.com:5684",
                "psk-id", "deadbeef01", 1, serverConfig
        );
        assertEquals("coaps://server.example.com:5684", config.getUri());
    }

    @Test
    @DisplayName("ClassicConfig stores shortServerId correctly")
    void classic_storesShortServerId() {
        Lwm2mConfig.Lwm2mClassicConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "coaps://server.example.com:5684",
                "psk-id", "deadbeef01", 42, serverConfig
        );
        assertEquals(42, config.getShortServerId());
    }

    @Test
    @DisplayName("ClassicConfig stores PSK identity correctly")
    void classic_storesPskIdentity() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "uri", "my-classic-psk", "key", 1, serverConfig
        );
        assertEquals("my-classic-psk", config.getPskIdentity());
    }

    @Test
    @DisplayName("ClassicConfig stores private key correctly")
    void classic_storesPrivateKey() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "uri", "psk-id", "cafebabe42", 1, serverConfig
        );
        assertEquals("cafebabe42", config.getPrivateKey());
    }

    @Test
    @DisplayName("ClassicConfig stores serverConfig reference")
    void classic_storesServerConfig() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "uri", "id", "key", 1, serverConfig
        );
        assertSame(serverConfig, config.getServerConfig());
    }

    @Test
    @DisplayName("ClassicConfig queueMode defaults to false")
    void classic_queueMode_defaultFalse() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "uri", "id", "key", 1, serverConfig
        );
        assertFalse(config.isQueueMode());
    }

    @Test
    @DisplayName("ClassicConfig queueMode can be set to true")
    void classic_queueMode_canBeEnabled() {
        Lwm2mConfig config = new Lwm2mConfig.Lwm2mClassicConfig(
                "ep", "uri", "id", "key", 1, serverConfig, true
        );
        assertTrue(config.isQueueMode());
    }
}

