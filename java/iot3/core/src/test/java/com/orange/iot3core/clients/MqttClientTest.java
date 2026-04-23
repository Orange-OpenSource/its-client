/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3core.clients;

import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import org.junit.jupiter.api.*;
import org.mockito.Answers;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MqttClient} using a mock {@link Mqtt5AsyncClient} injected via the
 * package-private constructor, so no real broker connection is needed.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Connection state delegation ({@code isConnected()})</li>
 *   <li>Guard behaviour when the client is closed (subscribe / publish / double-close)</li>
 *   <li>QoS 0 message drop when disconnected</li>
 *   <li>Invalid-topic guard inside {@code publishMessage}</li>
 * </ul>
 */
@DisplayName("MqttClient — guard and state behaviour")
class MqttClientTest {

    private Mqtt5AsyncClient mockMqttClient;
    private MqttCallback     mockCallback;
    private MqttClient       client;

    @BeforeEach
    void setUp() {
        // RETURNS_DEEP_STUBS automatically stubs the entire fluent-builder chain
        // (subscribeWith(), publishWith(), disconnect()…) without manual setup.
        mockMqttClient = mock(Mqtt5AsyncClient.class, RETURNS_DEEP_STUBS);
        mockCallback   = mock(MqttCallback.class);
        setupPublishChainStub();
        client         = new MqttClient(mockMqttClient, mockCallback, null);
    }

    /**
     * Explicitly wires the {@code publishWith()} fluent chain to avoid two problems:
     * <ol>
     *   <li><b>ClassCastException</b> — {@code RETURNS_DEEP_STUBS} resolves the self-generic
     *       {@code B extends Mqtt5PublishBuilderBase.Complete<B>} to the raw bound
     *       {@code Mqtt5PublishBuilderBase.Complete} instead of
     *       {@code Mqtt5PublishBuilder.Send.Complete}.</li>
     *   <li><b>NullPointerException</b> — {@code RETURNS_DEEP_STUBS} cannot infer the
     *       unbound generic {@code P} in {@code CompletableFuture<P>} returned by
     *       {@code send()}, so it falls back to {@code null}.</li>
     * </ol>
     *
     * <p>{@code Answers.RETURNS_SELF} solves (1) automatically: every builder method whose
     * return type is compatible with {@code Mqtt5PublishBuilder.Send.Complete} (i.e. all
     * the chaining methods) returns the same mock. {@code send()} is incompatible, so it
     * is stubbed explicitly to return a mock {@link CompletableFuture}.
     */
    @SuppressWarnings("rawtypes")
    private void setupPublishChainStub() {
        Mqtt5PublishBuilder.Send.Complete builderStub =
                mock(Mqtt5PublishBuilder.Send.Complete.class, Answers.RETURNS_SELF);
        // send() → CompletableFuture<P>: incompatible with mock type, RETURNS_SELF gives null
        CompletableFuture mockFuture = mock(CompletableFuture.class);
        doReturn(mockFuture).when(builderStub).send();
        doReturn(builderStub).when(mockMqttClient).publishWith();
    }

    // ── isConnected() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isConnected() returns true when underlying state is CONNECTED")
    void isConnected_whenStateConnected_returnsTrue() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.CONNECTED);
        assertTrue(client.isConnected());
    }

    @Test
    @DisplayName("isConnected() returns false when underlying state is DISCONNECTED")
    void isConnected_whenStateDisconnected_returnsFalse() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.DISCONNECTED);
        assertFalse(client.isConnected());
    }

    // ── close() guard ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("calling close() twice only triggers one disconnect on the broker")
    void close_calledTwice_disconnectsOnlyOnce() {
        client.close();
        client.close(); // second call must be a no-op
        verify(mockMqttClient, times(1)).disconnect();
    }

    @Test
    @DisplayName("subscribeToTopic() after close() never calls the broker")
    void subscribeToTopic_afterClose_neverCallsBroker() {
        client.close();
        client.subscribeToTopic("test/topic");
        verify(mockMqttClient, never()).subscribeWith();
    }

    @Test
    @DisplayName("unsubscribeFromTopic() after close() never calls the broker")
    void unsubscribeFromTopic_afterClose_neverCallsBroker() {
        client.close();
        client.unsubscribeFromTopic("test/topic");
        verify(mockMqttClient, never()).unsubscribeWith();
    }

    @Test
    @DisplayName("publishMessage() after close() never calls the broker")
    void publishMessage_afterClose_neverCallsBroker() {
        client.close();
        client.publishMessage("valid/topic", "message", false, 0, 0);
        verify(mockMqttClient, never()).publishWith();
    }

    // ── disconnected state guards ─────────────────────────────────────────────

    @Test
    @DisplayName("subscribeToTopic() when not connected stores topic but does not call broker")
    void subscribeToTopic_whenNotConnected_neverCallsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.DISCONNECTED);
        client.subscribeToTopic("test/topic");
        verify(mockMqttClient, never()).subscribeWith();
    }

    @Test
    @DisplayName("unsubscribeFromTopic() when not connected does not call broker")
    void unsubscribeFromTopic_whenNotConnected_neverCallsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.DISCONNECTED);
        client.unsubscribeFromTopic("test/topic");
        verify(mockMqttClient, never()).unsubscribeWith();
    }

    @Test
    @DisplayName("QoS 0 publish is silently dropped when the client is disconnected")
    void publishMessage_qos0WhenDisconnected_neverCallsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.DISCONNECTED);
        client.publishMessage("valid/topic", "message", false, 0, 0);
        verify(mockMqttClient, never()).publishWith();
    }

    @Test
    @DisplayName("QoS 1 publish is forwarded even when disconnected (broker will queue it)")
    void publishMessage_qos1WhenDisconnected_callsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.DISCONNECTED);
        client.publishMessage("valid/topic", "message", false, 1, 0);
        verify(mockMqttClient).publishWith();
    }

    // ── topic validation inside publishMessage ────────────────────────────────

    @Test
    @DisplayName("publishMessage() with '#' wildcard topic never calls the broker")
    void publishMessage_hashWildcardTopic_neverCallsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.CONNECTED);
        client.publishMessage("invalid/#/topic", "message", false, 0, 0);
        verify(mockMqttClient, never()).publishWith();
    }

    @Test
    @DisplayName("publishMessage() with '+' wildcard topic never calls the broker")
    void publishMessage_plusWildcardTopic_neverCallsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.CONNECTED);
        client.publishMessage("invalid/+/topic", "message", false, 0, 0);
        verify(mockMqttClient, never()).publishWith();
    }

    @Test
    @DisplayName("publishMessage() with valid topic when connected calls the broker")
    void publishMessage_validTopicWhenConnected_callsBroker() {
        when(mockMqttClient.getState()).thenReturn(MqttClientState.CONNECTED);
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);
        verify(mockMqttClient).publishWith();
    }
}

