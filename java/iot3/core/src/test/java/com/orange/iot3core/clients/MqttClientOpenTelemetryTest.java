/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 5)
 */
package com.orange.iot3core.clients;

import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests verifying how {@link MqttClient} drives a (mocked)
 * {@link OpenTelemetryClient} when one is configured: span creation on publish, span lifecycle
 * on completion/failure, {@code traceparent} propagation, and trace extraction on message
 * reception.
 *
 * <p>{@code MqttClient} reads/writes the {@code traceparent} header through the real, process-wide
 * {@code GlobalOpenTelemetry} propagator (not through the injected {@link OpenTelemetryClient}),
 * so a minimal real {@link OpenTelemetrySdk} with the W3C propagator is registered for the
 * duration of each test. The span returned by the mocked {@link OpenTelemetryClient} is
 * {@link Span#wrap(SpanContext)} (a real, propagation-only {@code Span}) spied upon with Mockito:
 * this keeps {@code storeInContext}/{@code getSpanContext} genuinely functional — required for the
 * W3C propagator to actually emit a {@code traceparent} value — while still allowing
 * {@code verify(...)} on {@code setAttribute}/{@code end}/{@code setStatus}. A plain
 * {@code mock(Span.class)} would not work here: Mockito also intercepts default interface methods
 * such as {@code storeInContext}, which would silently return {@code null} instead of actually
 * storing the span in the context.
 *
 * <p>Kept separate from {@link MqttClientTest} (which covers {@code MqttClient} guard/state
 * behaviour without telemetry) to keep each file focused.
 */
@DisplayName("MqttClient — OpenTelemetry integration")
class MqttClientOpenTelemetryTest {

    private static final String VALID_TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String VALID_SPAN_ID = "00f067aa0ba902b7";

    private Mqtt5AsyncClient mockMqttClient;
    private MqttCallback mockCallback;
    private OpenTelemetryClient mockOpenTelemetryClient;
    private Mqtt5PublishBuilder.Send.Complete publishBuilderStub;
    private Span mockSpan;
    private MqttClient client;

    @SuppressWarnings("rawtypes")
    @BeforeEach
    void setUp() {
        // Register a real (minimal) global propagator: MqttClient injects/extracts the
        // "traceparent" header via GlobalOpenTelemetry directly, regardless of the
        // OpenTelemetryClient instance it was given.
        GlobalOpenTelemetry.set(OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build());

        mockMqttClient = mock(Mqtt5AsyncClient.class, RETURNS_DEEP_STUBS);
        mockCallback = mock(MqttCallback.class);
        mockOpenTelemetryClient = mock(OpenTelemetryClient.class);
        SpanContext validSpanContext = SpanContext.create(
                VALID_TRACE_ID, VALID_SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());
        // Real propagation-only Span, spied to allow verify() while keeping real propagation behaviour.
        mockSpan = spy(Span.wrap(validSpanContext));

        when(mockOpenTelemetryClient.startSpan(anyString(), any(SpanKind.class))).thenReturn(mockSpan);
        when(mockOpenTelemetryClient.startSpanWithLink(anyString(), any(SpanKind.class), anyString(), anyString()))
                .thenReturn(mockSpan);
        when(mockOpenTelemetryClient.getTraceId(any(Span.class))).thenReturn(VALID_TRACE_ID);
        when(mockOpenTelemetryClient.getSpanId(any(Span.class))).thenReturn(VALID_SPAN_ID);


        // See MqttClientTest for why the publish chain is stubbed explicitly.
        publishBuilderStub = mock(Mqtt5PublishBuilder.Send.Complete.class, Answers.RETURNS_SELF);
        CompletableFuture mockFuture = mock(CompletableFuture.class);
        doReturn(mockFuture).when(publishBuilderStub).send();
        doReturn(publishBuilderStub).when(mockMqttClient).publishWith();

        when(mockMqttClient.getState()).thenReturn(MqttClientState.CONNECTED);

        client = new MqttClient(mockMqttClient, mockCallback, mockOpenTelemetryClient);
    }

    @AfterEach
    void tearDown() {
        GlobalOpenTelemetry.resetForTest();
    }

    // ── span creation on publish ─────────────────────────────────────────────

    @Test
    @DisplayName("publishMessage() starts a PRODUCER span named 'IoT3 Core MQTT Message'")
    void publishMessage_startsProducerSpan() {

        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);

        verify(mockOpenTelemetryClient).startSpan("IoT3 Core MQTT Message", SpanKind.PRODUCER);
    }

    @Test
    @DisplayName("publishMessage() sets topic, payload size and sdk_language span attributes")
    void publishMessage_setsExpectedAttributes() {
        String topic = "context/inQueue/v2x/cam/uuid/0/3/1/2";
        String payload = "payload";

        client.publishMessage(topic, payload, false, 0, 0);

        verify(mockSpan).setAttribute(AttributeKey.stringKey("iot3.core.mqtt.topic"), topic);
        verify(mockSpan).setAttribute(AttributeKey.stringKey("iot3.core.mqtt.payload_size"),
                String.valueOf(payload.length()));
        verify(mockSpan).setAttribute(AttributeKey.stringKey("iot3.core.sdk_language"), "java");
    }

    @Test
    @DisplayName("publishMessage() with retain=true sets the retain span attribute")
    void publishMessage_retained_setsRetainAttribute() {
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", true, 0, 0);

        verify(mockSpan).setAttribute(AttributeKey.booleanKey("iot3.core.mqtt.retain"), true);
    }

    @Test
    @DisplayName("publishMessage() with retain=false never sets the retain span attribute")
    void publishMessage_notRetained_neverSetsRetainAttribute() {
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);

        verify(mockSpan, never()).setAttribute(eq(AttributeKey.booleanKey("iot3.core.mqtt.retain")), anyBoolean());
    }

    @Test
    @DisplayName("publishMessage() attaches a traceparent user property to the MQTT publish")
    void publishMessage_attachesTraceparentUserProperty() {
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);

        verify(publishBuilderStub).userProperties(any(Mqtt5UserProperties.class));
    }

    // ── span lifecycle on completion ─────────────────────────────────────────

    @Test
    @DisplayName("successful publish ends the span without setting an error status")
    @SuppressWarnings("unchecked")
    void publishMessage_onSuccess_endsSpanWithoutError() {
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);

        ArgumentCaptor<java.util.function.BiConsumer<Object, Throwable>> captor =
                ArgumentCaptor.forClass(java.util.function.BiConsumer.class);
        verify(publishBuilderStub).send();
        CompletableFuture<?> future = (CompletableFuture<?>) publishBuilderStub.send();
        verify(future).whenComplete(captor.capture());

        captor.getValue().accept(null, null);

        verify(mockSpan).end();
        verify(mockSpan, never()).setStatus(any(), anyString());
    }

    @Test
    @DisplayName("failed publish sets ERROR status on the span and still ends it")
    @SuppressWarnings("unchecked")
    void publishMessage_onFailure_setsErrorStatusAndEndsSpan() {
        client.publishMessage("context/inQueue/v2x/cam/uuid/0/3/1/2", "payload", false, 0, 0);

        ArgumentCaptor<java.util.function.BiConsumer<Object, Throwable>> captor =
                ArgumentCaptor.forClass(java.util.function.BiConsumer.class);
        CompletableFuture<?> future = (CompletableFuture<?>) publishBuilderStub.send();
        verify(future).whenComplete(captor.capture());

        RuntimeException failure = new RuntimeException("boom");
        captor.getValue().accept(null, failure);

        verify(mockSpan).setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, failure.getMessage());
        verify(mockSpan).end();
    }

    // ── trace extraction on message reception ────────────────────────────────

    @Test
    @DisplayName("processPublish() extracts trace context and starts a linked CONSUMER span")
    @SuppressWarnings("unchecked")
    void processPublish_startsLinkedConsumerSpan() {
        ArgumentCaptor<Consumer<Mqtt5Publish>> publishCallbackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockMqttClient).publishes(eq(MqttGlobalPublishFilter.SUBSCRIBED), publishCallbackCaptor.capture());

        Mqtt5Publish mockPublish = mock(Mqtt5Publish.class);
        MqttTopic mockTopic = mock(MqttTopic.class);
        when(mockTopic.toString()).thenReturn("context/outQueue/v2x/cam/uuid/0/3/1/2");
        when(mockPublish.getTopic()).thenReturn(mockTopic);
        when(mockPublish.getPayloadAsBytes()).thenReturn("payload".getBytes());
        when(mockPublish.isRetain()).thenReturn(false);
        Mqtt5UserProperties userProperties = mock(Mqtt5UserProperties.class);
        when(userProperties.asList()).thenReturn(List.of());
        when(mockPublish.getUserProperties()).thenReturn(userProperties);

        assertDoesNotThrow(() -> publishCallbackCaptor.getValue().accept(mockPublish));

        verify(mockOpenTelemetryClient).startSpanWithLink(eq("IoT3 Core MQTT Message"), eq(SpanKind.CONSUMER),
                anyString(), anyString());
        verify(mockCallback).messageArrived("context/outQueue/v2x/cam/uuid/0/3/1/2", "payload");
        verify(mockSpan).end();
    }
}




