/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttClient {

    private static final Logger LOGGER = Logger.getLogger(MqttClient.class.getName());

    private final Mqtt5AsyncClient mqttClient;
    private final MqttCallback callback;

    private boolean tlsConnection = false;

    public MqttClient(String serverHost, String username, String password, String clientId,
                      MqttClientSslConfig sslConfig, MqttCallback callback) {
        this.callback = callback;

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
        if(mqttClient != null) {
            mqttClient.disconnect().whenComplete((mqtt5DisconnectResult, throwable) -> {
                if(throwable != null) {
                    LOGGER.log(Level.WARNING, "Error during disconnection");
                } else {
                    LOGGER.log(Level.INFO, "Disconnected");
                }
            });
        }
        tlsConnection = false;
    }

    public void connect() {
        mqttClient.connectWith()
                .cleanStart(true)
                .send()
                .whenComplete((connAck, throwable) -> {
                    LOGGER.log(Level.INFO, "Success connecting to the server");
                });
    }

    public void subscribeToTopic(String topic) {
        LOGGER.log(Level.INFO, "Subscribing to topic: " + topic);
        if(mqttClient != null) {
            mqttClient.subscribeWith()
                    .topicFilter(topic)
                    .callback(publish -> {
                        String message = new String(publish.getPayloadAsBytes());
                        LOGGER.log(Level.INFO, "MQTT message arrived on: " + publish.getTopic() + " | " + message);
                        try {
                            callback.messageArrived(publish.getTopic().toString(), message);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            LOGGER.log(Level.WARNING, "Subscribed fail!");
                        } else {
                            LOGGER.log(Level.FINE, "Subscribed!");
                        }
                        callback.subscriptionComplete(throwable);
                    });
        } else {
            LOGGER.log(Level.INFO, "Null MQTT client...");
        }
    }

    public void unsubscribeFromTopic(String topic) {
        LOGGER.log(Level.INFO, "Unsubscribing from topic: " + topic);
        if(mqttClient != null) {
            mqttClient.unsubscribeWith()
                    .topicFilter(topic)
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            LOGGER.log(Level.WARNING, "Unsubscribed fail!");
                        } else {
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
            LOGGER.log(Level.INFO, "Sending message: " + topic + " | "+ message);
            final long pubTimestamp = System.currentTimeMillis();
            MqttQos mqttQos = MqttQos.AT_MOST_ONCE;
            if(qos == 1) mqttQos = MqttQos.AT_LEAST_ONCE;
            if(qos == 2) mqttQos = MqttQos.EXACTLY_ONCE;
            if(mqttClient != null) {
                mqttClient.publishWith()
                        .topic(topic)
                        .payload(message.getBytes())
                        .qos(mqttQos)
                        .retain(retained)
                        .send()
                        .whenComplete((mqtt3Publish, throwable) -> {
                            if (throwable != null) {
                                LOGGER.log(Level.WARNING, "Failed publishing message...");
                            } else {
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
