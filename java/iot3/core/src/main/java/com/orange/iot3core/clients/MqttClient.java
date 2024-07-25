/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttClient {

    private static final Logger LOGGER = Logger.getLogger(MqttClient.class.getName());

    private final Mqtt5AsyncClient mqttClient;
    private final MqttCallback callback;
    private final OpenTelemetryClient openTelemetryClient;

    private boolean tlsConnection = false;

    public MqttClient(String serverHost,
                      String username,
                      String password,
                      String clientId,
                      MqttClientSslConfig sslConfig,
                      MqttCallback callback,
                      OpenTelemetryClient openTelemetryClient) {
        this.callback = callback;
        this.openTelemetryClient = openTelemetryClient;

        if(sslConfig != null) {
            mqttClient = com.hivemq.client.mqtt.MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(clientId)
                    .serverHost(serverHost)
                    .serverPort(18883)
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .sslConfig(sslConfig)
                    .addDisconnectedListener(context1 -> {
                        LOGGER.log(Level.INFO, "Disconnected from MQTT broker " + serverHost);
                        callback.connectionLost(context1.getCause());
                    })
                    .addConnectedListener(context12 -> {
                        LOGGER.log(Level.INFO, "Connected to MQTT broker " + serverHost);
                        callback.connectComplete(true, serverHost);
                    })
                    .buildAsync();
            tlsConnection = true;
        } else {
            mqttClient = com.hivemq.client.mqtt.MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(clientId)
                    .serverHost(serverHost)
                    .serverPort(11883)
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .addDisconnectedListener(context1 -> {
                        LOGGER.log(Level.INFO, "Disconnected from MQTT broker " + serverHost);
                        callback.connectionLost(null);
                    })
                    .addConnectedListener(context12 -> {
                        LOGGER.log(Level.INFO, "Connected to MQTT broker " + serverHost);
                        callback.connectComplete(true, serverHost);
                    })
                    .buildAsync();
        }

        connect();
    }

    public void disconnect() {
        Span span = openTelemetryClient.startSpan("MQTT Disconnect");
        if(mqttClient != null) {
            mqttClient.disconnect().whenComplete((mqtt5DisconnectResult, throwable) -> {
                if(throwable != null) {
                    openTelemetryClient.endSpan(span, false, "Failed to disconnect from MQTT broker");
                    LOGGER.log(Level.WARNING, "Error during disconnection");
                } else {
                    openTelemetryClient.endSpan(span, true, "Disconnected from MQTT broker");
                    LOGGER.log(Level.INFO, "Disconnected");
                }
            });
        }
        tlsConnection = false;
    }

    public void connect() {
        Span span = openTelemetryClient.startSpan("MQTT Connect");
        mqttClient.connectWith()
                .cleanStart(true)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if(throwable != null) {
                        openTelemetryClient.endSpan(span, false, "Failed to connect to MQTT broker");
                        LOGGER.log(Level.INFO, "Error during connection to the server");
                    } else {
                        openTelemetryClient.endSpan(span, true, "Connected to MQTT broker");
                        LOGGER.log(Level.INFO, "Success connecting to the server");
                    }
                });
    }

    public void subscribeToTopic(String topic) {
        Span span = openTelemetryClient.startSpan("MQTT Subscribe");
        LOGGER.log(Level.INFO, "Subscribing to topic: " + topic);
        if(mqttClient != null) {
            mqttClient.subscribeWith()
                    .topicFilter(topic)
                    .callback(publish -> {
                        String message = new String(publish.getPayloadAsBytes());

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
                        Span receivedSpan = openTelemetryClient.startSpanWithLink("MQTT Receive Message",
                                receivedSpanContext.getTraceId(), receivedSpanContext.getSpanId());
                        receivedSpan.setAttribute(AttributeKey.stringKey("messaging.destination"), topic);
                        receivedSpan.setAttribute(AttributeKey.stringKey("messaging.message_payload_size_bytes"),
                                String.valueOf(message.length()));
                        openTelemetryClient.addEvent(receivedSpan, "Received MQTT message");

                        LOGGER.log(Level.INFO, "MQTT message arrived on: " + publish.getTopic() + " | " + message);
                        try {
                            callback.messageArrived(publish.getTopic().toString(), message);
                            openTelemetryClient.endSpan(receivedSpan, true,
                                    "Processed received MQTT message");
                        } catch (Exception e) {
                            openTelemetryClient.endSpan(receivedSpan, false,
                                    "Error processing MQTT message");
                            throw new RuntimeException(e);
                        }
                    })
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            openTelemetryClient.endSpan(span, false,
                                    "Failed to subscribe to MQTT topic: " + topic);
                            LOGGER.log(Level.WARNING, "Subscribed fail!");
                        } else {
                            openTelemetryClient.endSpan(span, true,
                                    "Subscribed to MQTT topic: " + topic);
                            LOGGER.log(Level.FINE, "Subscribed!");
                        }
                        callback.subscriptionComplete(throwable);
                    });
        } else {
            LOGGER.log(Level.INFO, "Null MQTT client...");
        }
    }

    public void unsubscribeFromTopic(String topic) {
        Span span = openTelemetryClient.startSpan("MQTT Unsubscribe");
        LOGGER.log(Level.INFO, "Unsubscribing from topic: " + topic);
        if(mqttClient != null) {
            mqttClient.unsubscribeWith()
                    .topicFilter(topic)
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            openTelemetryClient.endSpan(span, false,
                                    "Failed to unsubscribe from MQTT topic: " + topic);
                            LOGGER.log(Level.WARNING, "Unsubscribed fail!");
                        } else {
                            openTelemetryClient.endSpan(span, true,
                                    "Unsubscribed from MQTT topic: " + topic);
                            LOGGER.log(Level.INFO, "Unsubscribed!");
                        }
                        callback.unsubscriptionComplete(throwable);
                    });
        } else {
            LOGGER.log(Level.INFO, "Null MQTT client...");
        }
    }

    public void sendMessage(String topic, String message, boolean retained, int qos) {
        if(isValidMqttPubTopic(topic)) {
            Span span = openTelemetryClient.startSpan("MQTT Send Message");
            span.setAttribute(AttributeKey.stringKey("messaging.destination"), topic);
            span.setAttribute(AttributeKey.stringKey("messaging.message_payload_size_bytes"),
                    String.valueOf(message.length()));
            openTelemetryClient.addEvent(span, "Sending MQTT message");

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

            // Send the message with user properties
            LOGGER.log(Level.INFO, "Sending message: " + topic + " | "+ message);
            final long pubTimestamp = System.currentTimeMillis();
            MqttQos mqttQos = MqttQos.AT_MOST_ONCE;
            if(qos == 1) mqttQos = MqttQos.AT_LEAST_ONCE;
            if(qos == 2) mqttQos = MqttQos.EXACTLY_ONCE;
            if(mqttClient != null) {
                mqttClient.publishWith()
                        .topic(topic)
                        .payload(message.getBytes())
                        .userProperties(userProperties)
                        .qos(mqttQos)
                        .retain(retained)
                        .send()
                        .whenComplete((mqtt3Publish, throwable) -> {
                            if (throwable != null) {
                                openTelemetryClient.endSpan(span, false, "MQTT message could not be sent");
                                LOGGER.log(Level.WARNING, "Failed publishing message...");
                            } else {
                                openTelemetryClient.endSpan(span, true, "MQTT message sent");
                                LOGGER.log(Level.INFO, "Success publishing message ["
                                        + (System.currentTimeMillis() - pubTimestamp) + " ms]");
                            }
                            callback.messagePublished(throwable);
                        });
            } else {
                LOGGER.log(Level.INFO, "Null MQTT client...");
            }
        }
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

    public void sendMessage(String topic, String message, boolean retained) {
        sendMessage(topic, message, retained, 0);
    }

    public void sendMessage(String topic, String message) {
        sendMessage(topic, message, false, 0);
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.getState().isConnected();
    }

    public boolean isConnectionSecured() {
        return isConnected() && tlsConnection;
    }
    
}
