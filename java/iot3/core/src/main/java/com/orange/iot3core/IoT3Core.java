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
     * @param mqttHost MQTT broker address
     * @param mqttUsername MQTT username
     * @param mqttPassword MQTT password
     * @param mqttClientId unique MQTT client ID
     * @param ioT3CoreCallback interface to retrieve the different clients outputs
     * @param telemetryHost Open Telemetry server address
     */
    public IoT3Core(String mqttHost,
                    String mqttUsername,
                    String mqttPassword,
                    String mqttClientId,
                    IoT3CoreCallback ioT3CoreCallback,
                    String telemetryHost) {
        this(mqttHost,
                1883,
                8883,
                mqttUsername,
                mqttPassword,
                mqttClientId,
                ioT3CoreCallback,
                telemetryHost,
                -1,
                "/telemetry",
                "default",
                "default");
    }

    /**
     * Instantiate the IoT3.0 Core SDK.
     *
     * @param mqttHost MQTT broker address
     * @param mqttPortTcp TCP port of the MQTT broker
     * @param mqttPortTls TLS port of the MQTT broker
     * @param mqttUsername MQTT username
     * @param mqttPassword MQTT password
     * @param mqttClientId unique MQTT client ID
     * @param ioT3CoreCallback interface to retrieve the different clients outputs
     * @param telemetryHost Open Telemetry server address
     * @param telemetryPort port of the Open Telemetry server
     */
    public IoT3Core(String mqttHost,
                    int mqttPortTcp,
                    int mqttPortTls,
                    String mqttUsername,
                    String mqttPassword,
                    String mqttClientId,
                    IoT3CoreCallback ioT3CoreCallback,
                    String telemetryHost,
                    int telemetryPort,
                    String telemetryEndpoint,
                    String telemetryUsername,
                    String telemetryPassword) {
        // instantiate the OpenTelemetry client
        OpenTelemetryClient.Scheme scheme = OpenTelemetryClient.Scheme.HTTP;
        scheme.setCustomPort(telemetryPort);
        this.openTelemetryClient = new OpenTelemetryClient(
                scheme,
                telemetryHost,
                telemetryEndpoint,
                mqttClientId,
                telemetryUsername,
                telemetryPassword);
        // instantiate the MQTT client
        this.mqttClient = new MqttClient(
                mqttHost,
                mqttPortTcp,
                mqttPortTls,
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

    /**
     * Build an instance of IoT3Core.
     */
    public static class IoT3CoreBuilder {
        private String mqttHost;
        private int mqttPortTcp;
        private int mqttPortTls;
        private String mqttUsername;
        private String mqttPassword;
        private String mqttClientId;
        private IoT3CoreCallback ioT3CoreCallback;
        private String telemetryHost;
        private int telemetryPort;
        private String telemetryEndpoint;
        private String telemetryUsername;
        private String telemetryPassword;

        /**
         * Start building an instance of IoT3Core.
         */
        public IoT3CoreBuilder() {}

        /**
         * Set the MQTT parameters of your IoT3Core instance.
         *
         * @param mqttHost the host or IP address of the MQTT broker
         * @param mqttPortTcp the TCP port of the MQTT broker
         * @param mqttPortTls the TLS port of the MQTT broker
         * @param mqttUsername the username for authentication with the MQTT broker
         * @param mqttPassword the password for authentication with the MQTT broker
         * @param mqttClientId your client ID
         */
        public IoT3CoreBuilder mqttParams(String mqttHost,
                                          int mqttPortTcp,
                                          int mqttPortTls,
                                          String mqttUsername,
                                          String mqttPassword,
                                          String mqttClientId) {
            this.mqttHost = mqttHost;
            this.mqttPortTcp = mqttPortTcp;
            this.mqttPortTls = mqttPortTls;
            this.mqttUsername = mqttUsername;
            this.mqttPassword = mqttPassword;
            this.mqttClientId = mqttClientId;
            return this;
        }

        /**
         * Set the OpenTelemetry parameters of your IoT3Core instance.
         *
         * @param telemetryHost the host or IP address of the OpenTelemetry server
         * @param telemetryPort the port of the OpenTelemetry server
         * @param telemetryEndpoint the endpoint of the OpenTelemetry server (e.g. /endpoint/example)
         * @param telemetryUsername the username for authentication with the OpenTelemetry server
         * @param telemetryPassword the password for authentication with the OpenTelemetry server
         */
        public IoT3CoreBuilder telemetryParams(String telemetryHost,
                                               int telemetryPort,
                                               String telemetryEndpoint,
                                               String telemetryUsername,
                                               String telemetryPassword) {
            this.telemetryHost = telemetryHost;
            this.telemetryPort = telemetryPort;
            this.telemetryEndpoint = telemetryEndpoint;
            this.telemetryUsername = telemetryUsername;
            this.telemetryPassword = telemetryPassword;
            return this;
        }

        /**
         * Set the callback of your IoT3Core instance.
         *
         * @param ioT3CoreCallback callback to be notified of mainly MQTT-related events, e.g. message reception
         *                         or connection status
         */
        public IoT3CoreBuilder callback(IoT3CoreCallback ioT3CoreCallback) {
            this.ioT3CoreCallback = ioT3CoreCallback;
            return this;
        }

        /**
         * Build the IoT3Core instance.
         *
         * @return Iot3Core instance
         */
        public IoT3Core build() {
            return new IoT3Core(
                    mqttHost,
                    mqttPortTcp,
                    mqttPortTls,
                    mqttUsername,
                    mqttPassword,
                    mqttClientId,
                    ioT3CoreCallback,
                    telemetryHost,
                    telemetryPort,
                    telemetryEndpoint,
                    telemetryUsername,
                    telemetryPassword);
        }
    }

}
