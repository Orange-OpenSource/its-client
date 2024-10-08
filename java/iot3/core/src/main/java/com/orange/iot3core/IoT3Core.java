/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core;

import com.orange.iot3core.clients.Lwm2mClient;
import com.orange.iot3core.clients.MqttCallback;
import com.orange.iot3core.clients.MqttClient;
import com.orange.iot3core.clients.OpenTelemetryClient;

/**
 * Core SDK allowing to establish a connection with the Orange IoT3.0 platform.
 * <br>IoT3Core manages the following components:
 * <ul>
 * <li>MQTT client for high volumes and low latency communications,</li>
 * <li>LwM2M client for autoconfiguration and basic telemetry,</li>
 * <li>OpenTelemetry client for advanced telemetry</li>
 * </ul>
 */
public class IoT3Core {

    private final MqttClient mqttClient;
    private final OpenTelemetryClient openTelemetryClient;
    private final Lwm2mClient lwm2mClient;

    /**
     * Instantiate the IoT3.0 Core SDK.
     *
     * @param mqttHost MQTT broker address, provided by Orange
     * @param mqttUsername MQTT username, provided by Orange
     * @param mqttPassword MQTT password, provided by Orange
     * @param mqttClientId unique MQTT client ID
     * @param ioT3CoreCallback interface to retrieve the different clients outputs
     */
    public IoT3Core(String mqttHost,
                    String mqttUsername,
                    String mqttPassword,
                    String mqttClientId,
                    IoT3CoreCallback ioT3CoreCallback,
                    String telemetryHost) {
        // instantiate the OpenTelemetry client
        this.openTelemetryClient = new OpenTelemetryClient(
                OpenTelemetryClient.Scheme.HTTP,
                telemetryHost);
        // instantiate the MQTT client
        this.mqttClient = new MqttClient(
                mqttHost,
                mqttUsername,
                mqttPassword,
                mqttClientId,
                null,
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        ioT3CoreCallback.mqttConnectionLost(cause);
                    }

                    @Override
                    public void messageArrived(String topic, String message) {
                        ioT3CoreCallback.mqttMessageArrived(topic, message);
                    }

                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        ioT3CoreCallback.mqttConnectComplete(reconnect, serverURI);
                    }

                    @Override
                    public void messagePublished(Throwable publishFailure) {
                        ioT3CoreCallback.mqttMessagePublished(publishFailure);
                    }

                    @Override
                    public void subscriptionComplete(Throwable subscriptionFailure) {
                        ioT3CoreCallback.mqttSubscriptionComplete(subscriptionFailure);
                    }

                    @Override
                    public void unsubscriptionComplete(Throwable unsubscriptionFailure) {
                        ioT3CoreCallback.mqttUnsubscriptionComplete(unsubscriptionFailure);
                    }
                },
                openTelemetryClient);
        // instantiate the LwM2M client
        this.lwm2mClient = new Lwm2mClient();
    }

    /**
     * Disconnect the 3 clients (MQTT, LwM2M and OpenTelemetry)
     */
    public void disconnectAll() {
        disconnectMqtt();
        disconnectOpenTelemetry();
        disconnectLwM2M();
    }

    /**
     * Disconnect the MQTT client
     */
    public void disconnectMqtt() {
        if(mqttClient != null) mqttClient.disconnect();
    }

    /**
     * Disconnect the OpenTelemetry client
     */
    public void disconnectOpenTelemetry() {
        if(openTelemetryClient != null) openTelemetryClient.disconnect();
    }

    /**
     * Disconnect the LwM2M client
     */
    public void disconnectLwM2M() {
        if(lwm2mClient != null) lwm2mClient.disconnect();
    }

    /**
     * Reconnect the 3 clients (MQTT, LwM2M and OpenTelemetry)
     */
    public void reconnectAll() {
        reconnectMqtt();
        reconnectOpenTelemetry();
        reconnectLwM2M();
    }

    /**
     * Reconnect the MQTT client
     */
    public void reconnectMqtt() {
        if(mqttClient != null) mqttClient.connect();
    }

    /**
     * Reconnect the OpenTelemetry client
     */
    public void reconnectOpenTelemetry() {
        if(openTelemetryClient != null) openTelemetryClient.connect();
    }

    /**
     * Reconnect the LwM2M client
     */
    public void reconnectLwM2M() {
        if(lwm2mClient != null) lwm2mClient.connect();
    }

    /**
     * Subscribe to a MQTT topic
     */
    public void mqttSubscribe(String topic) {
        if(mqttClient != null) mqttClient.subscribeToTopic(topic);
    }

    /**
     * Unsubscribe from a MQTT topic
     */
    public void mqttUnsubscribe(String topic) {
        if(mqttClient != null) mqttClient.unsubscribeFromTopic(topic);
    }

    /**
     * Publish a message on the specified MQTT topic
     */
    public void mqttPublish(String topic, String message) {
        mqttPublish(topic, message, false);
    }

    /**
     * Publish a message on the specified MQTT topic with the indicated retained value
     */
    public void mqttPublish(String topic, String message, boolean retain) {
        if(mqttClient != null) mqttClient.publishMessage(topic, message, retain);
    }
}
