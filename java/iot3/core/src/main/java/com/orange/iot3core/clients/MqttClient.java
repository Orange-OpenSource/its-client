/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttClient {

    private static final Logger LOGGER = Logger.getLogger(MqttClient.class.getName());
    private static final String NULL_CLIENT = "Null MQTT client...";

    private final Mqtt5AsyncClient mqttClient;
    private final MqttCallback callback;
    private final OpenTelemetryClient openTelemetryClient;

    // Source of truth: topics we want subscribed after each connect
    private final Set<String> desiredTopics = ConcurrentHashMap.newKeySet();
    // Keep track of subscribe and unsubscribe calls
    private final Set<String> inflightSub = ConcurrentHashMap.newKeySet();
    private final Set<String> inflightUnsub = ConcurrentHashMap.newKeySet();
    // Filter out pending unsubscribes
    private final Set<String> pendingUnsub = ConcurrentHashMap.newKeySet();

    private final boolean useTls;

    public MqttClient(String serverHost,
                      int serverPort,
                      String username,
                      String password,
                      String clientId,
                      boolean useTls,
                      MqttCallback callback,
                      OpenTelemetryClient openTelemetryClient) {
        this.callback = callback;
        this.openTelemetryClient = openTelemetryClient;
        this.useTls = useTls;

        Mqtt5ClientBuilder mqttClientBuilder = com.hivemq.client.mqtt.MqttClient.builder()
                .useMqttVersion5()
                .identifier(clientId)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .automaticReconnect()
                        .initialDelay(1, TimeUnit.SECONDS)
                        .maxDelay(5, TimeUnit.SECONDS)
                        .applyAutomaticReconnect()
                .addDisconnectedListener(disconnectContext -> {
                    LOGGER.log(Level.INFO, "Disconnected from the MQTT broker by " + disconnectContext.getSource());
                    // when the disconnection was initiated by the user, disable auto-reconnect
                    if(disconnectContext.getSource().equals(MqttDisconnectSource.USER))
                        disconnectContext.getReconnector().reconnect(false);
                    callback.connectionLost(disconnectContext.getCause());
                })
                .addConnectedListener(connectContext -> {
                    LOGGER.log(Level.INFO, "Connected to the MQTT broker");
                    pendingUnsub.clear();
                    inflightUnsub.clear();
                    inflightSub.clear();
                    resubscribeAll();
                    callback.connectComplete(true, serverHost);
                });

        if(username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            mqttClientBuilder.simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth();
        }

        if(useTls) {
            mqttClient = mqttClientBuilder.sslWithDefaultConfig().buildAsync();
        } else {
            mqttClient = mqttClientBuilder.buildAsync();
        }

        // single callback for processing messages received on subscribed topics
        mqttClient.publishes(MqttGlobalPublishFilter.SUBSCRIBED, this::processPublish);

        connect();
    }

    private void connect() {
        mqttClient.connectWith()
                .cleanStart(true)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if(throwable != null) {
                        LOGGER.log(Level.INFO, "Error during connection to the server: " + throwable.getMessage());
                    } else {
                        LOGGER.log(Level.INFO, "Success connecting to the server");
                    }
                });
    }

    public void close() {
        if(mqttClient != null) {
            mqttClient.disconnect()
                    .orTimeout(500, TimeUnit.MILLISECONDS) // don't wait more than 500 milliseconds
                    .whenComplete((mqtt5DisconnectResult, throwable) -> {
                        if(throwable != null) {
                            LOGGER.log(Level.WARNING, "Error during disconnection: " + throwable.getMessage());
                        } else {
                            LOGGER.log(Level.INFO, "Disconnected");
                        }
                    });
        }
    }

    public void subscribeToTopic(String topic) {
        desiredTopics.add(topic);
        if(mqttClient == null) {
            LOGGER.log(Level.INFO, NULL_CLIENT);
            return;
        }
        if(mqttClient.getState() == MqttClientState.CONNECTED && inflightSub.add(topic)) {
            LOGGER.log(Level.INFO, "Subscribing to topic: " + topic);
            mqttClient.subscribeWith()
                    .topicFilter(topic)
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        inflightSub.remove(topic);

                        if (throwable != null) {
                            LOGGER.log(Level.WARNING, "Subscription failed: " + throwable.getMessage());
                            callback.subscriptionComplete(throwable);
                            return;
                        }

                        // MQTT 5: reason codes can signal refusal even when no exception is thrown
                        boolean ok = subAck.getReasonCodes().stream().noneMatch(Mqtt5ReasonCode::isError);

                        if (!ok) {
                            LOGGER.log(Level.WARNING, "Subscription refused by broker: " + subAck.getReasonCodes());
                            callback.subscriptionComplete(new RuntimeException("SUBACK: " + subAck.getReasonCodes()));
                            return;
                        }

                        // if user tried to unsubscribe while this was inflight, unsubscribe
                        if (!desiredTopics.contains(topic)) {
                            LOGGER.log(Level.FINE, "Subscribe completed but topic no longer desired; unsubscribing…");
                            unsubscribeFromTopic(topic);
                            callback.subscriptionComplete(null);
                            return;
                        }

                        LOGGER.log(Level.FINE, "Subscribed!");
                        callback.subscriptionComplete(null);
                    });
        }
    }

    public void unsubscribeFromTopic(String topic) {
        desiredTopics.remove(topic);
        pendingUnsub.add(topic);
        if(mqttClient == null) {
            LOGGER.log(Level.INFO, NULL_CLIENT);
            return;
        }
        if(mqttClient.getState() == MqttClientState.CONNECTED && inflightUnsub.add(topic)) {
            LOGGER.log(Level.INFO, "Unsubscribing from topic: " + topic);
            mqttClient.unsubscribeWith()
                    .topicFilter(topic)
                    .send()
                    .whenComplete((unsubAck, throwable) -> {
                        inflightUnsub.remove(topic);

                        if (throwable != null) {
                            LOGGER.log(Level.WARNING, "Unsubscribe failed: " + throwable.getMessage());
                            callback.unsubscriptionComplete(throwable);
                            return;
                        }

                        boolean ok = unsubAck.getReasonCodes().stream().allMatch(rc ->
                                rc == Mqtt5UnsubAckReasonCode.SUCCESS ||
                                        rc == Mqtt5UnsubAckReasonCode.NO_SUBSCRIPTIONS_EXISTED);

                        if (ok) {
                            pendingUnsub.remove(topic);
                            LOGGER.log(Level.INFO, "Unsubscribed!");
                            callback.unsubscriptionComplete(null);
                        } else {
                            LOGGER.log(Level.WARNING, "Unsubscribe not accepted: " + unsubAck.getReasonCodes());
                            // we keep pendingUnsub so messages stay filtered locally for this topic
                            callback.unsubscriptionComplete(new RuntimeException("UNSUBACK: " + unsubAck.getReasonCodes()));
                        }
                    });
        }
    }

    private void resubscribeAll() {
        if (desiredTopics.isEmpty()) return;

        List<String> topics = new ArrayList<>(desiredTopics);
        for(String topic: topics) {
            subscribeToTopic(topic);
        }
    }

    public void publishMessage(String topic, String message, boolean retained, int qos) {
        LOGGER.log(Level.INFO, "Sending message: " + topic + " | "+ message);

        MqttQos mqttQos = MqttQos.AT_MOST_ONCE;
        if(qos == 1) mqttQos = MqttQos.AT_LEAST_ONCE;
        if(qos == 2) mqttQos = MqttQos.EXACTLY_ONCE;

        if(isValidMqttPubTopic(topic) && mqttClient != null) {
            var publishBuilder = mqttClient.publishWith()
                    .topic(topic)
                    .payload(message.getBytes())
                    .qos(mqttQos)
                    .retain(retained);

            if(openTelemetryClient != null) {
                Span span = openTelemetryClient.startSpan("IoT3 Core MQTT Message", SpanKind.PRODUCER);
                span.setAttribute(AttributeKey.stringKey("iot3.core.mqtt.topic"), topic);
                span.setAttribute(AttributeKey.stringKey("iot3.core.mqtt.payload_size"),
                        String.valueOf(message.length()));
                span.setAttribute(AttributeKey.stringKey("iot3.core.sdk_language"), "java");

                // Inject the trace context into a map
                Map<String, String> contextMap = new HashMap<>();
                GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current().with(span),
                        contextMap, (carrier, key, value) -> {
                            assert carrier != null;
                            carrier.put(key, value);
                        });

                // Create user properties with traceparent
                Mqtt5UserProperties userProperties = Mqtt5UserProperties.builder()
                        .add(Mqtt5UserProperty.of("traceparent", contextMap.get("traceparent")))
                        .build();

                // send message with user properties
                publishBuilder.userProperties(userProperties)
                        .send()
                        .whenComplete((mqtt5Publish, throwable)
                                -> onPublishComplete(span, throwable));
            } else {
                // send message without user properties
                publishBuilder.send()
                        .whenComplete((mqtt5Publish, throwable)
                                -> onPublishComplete(null, throwable));
            }
        } else if(mqttClient == null) {
            LOGGER.log(Level.INFO, NULL_CLIENT);
        }
    }

    private void onPublishComplete(Span span, Throwable throwable) {
        if (throwable != null) {
            if(span != null) {
                span.setStatus(StatusCode.ERROR, throwable.getMessage());
                span.end();
            }
            LOGGER.log(Level.WARNING, "Failed publishing message... " + throwable.getMessage());
        } else {
            if(span != null) span.end();
            LOGGER.log(Level.INFO, "Success publishing message: ");
        }
        callback.messagePublished(throwable);
    }

    public void publishMessage(String topic, String message, boolean retained) {
        publishMessage(topic, message, retained, 0);
    }

    public void publishMessage(String topic, String message) {
        publishMessage(topic, message, false, 0);
    }

    private void processPublish(Mqtt5Publish publish) {
        String message = new String(publish.getPayloadAsBytes());

        if(openTelemetryClient != null) {
            // Extract user properties
            String traceparent = publish.getUserProperties().asList().stream()
                    .filter(property -> property.getName().toString().equals("traceparent"))
                    .findFirst()
                    .map(property -> property.getValue().toString())
                    .orElse(null);

            // Create a context map with the traceparent
            Map<String, String> contextMap = new HashMap<>();
            if (traceparent != null) {
                contextMap.put("traceparent", traceparent);
            }

            // Extract the trace context from the context map
            TextMapGetter<Map<String, String>> getter = new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    return carrier.get(key);
                }
            };

            Context extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                    .extract(Context.current(), contextMap, getter);
            SpanContext receivedSpanContext = Span.fromContext(extractedContext).getSpanContext();

            // Create a new span with a link to the received span context
            Span receivedSpan = openTelemetryClient.startSpanWithLink(
                    "IoT3 Core MQTT Message",
                    SpanKind.CONSUMER,
                    receivedSpanContext.getTraceId(),
                    receivedSpanContext.getSpanId());
            receivedSpan.setAttribute(AttributeKey.stringKey("iot3.core.mqtt.topic"),
                    publish.getTopic().toString());
            receivedSpan.setAttribute(AttributeKey.stringKey("iot3.core.mqtt.payload_size"),
                    String.valueOf(message.length()));
            receivedSpan.setAttribute(AttributeKey.stringKey("iot3.core.sdk_language"), "java");
            receivedSpan.end();
        }

        LOGGER.log(Level.INFO, "MQTT message arrived on: " + publish.getTopic() + " | " + message);
        callback.messageArrived(publish.getTopic().toString(), message);
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.getState().isConnected();
    }

    public boolean isConnectionSecured() {
        return isConnected() && useTls;
    }

    public boolean isValidMqttPubTopic(String topic) {
        // Check for null or empty string
        if (topic == null || topic.isEmpty()) {
            LOGGER.log(Level.FINE, "Publication topic cannot be null or empty!");
            return false;
        }

        // Check for valid UTF-8 encoding
        try {
            topic.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Publication topic encoding is not valid, should be UTF-8!");
            return false;
        }

        // Check for forbidden characters
        if (topic.contains("#") || topic.contains("+")) {
            LOGGER.log(Level.FINE, "Publication topic cannot use wildcard + or #!");
            return false;
        }

        // Check for leading or trailing spaces
        if (topic.startsWith(" ") || topic.endsWith(" ")) {
            LOGGER.log(Level.FINE, "Publication topic cannot have white spaces!");
            return false;
        }

        // Check for length limitations (adjust the limit as needed)
        if (topic.length() > 65535) {
            LOGGER.log(Level.FINE, "Publication topic is too long!");
            return false;
        }

        // If all checks pass, the topic is valid
        LOGGER.log(Level.FINE, "Publication topic is valid!");
        return true;
    }
    
}
