/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

@authors
    Mathieu LEFEBVRE   <mathieu1.lefebvre@orange.com>
    Maciej Ćmiel       <maciej.cmiel@orange.com>
*/
package com.orange.iot3core;

import com.orange.iot3core.bootstrap.BootstrapConfig;
import com.orange.iot3core.clients.MqttCallback;
import com.orange.iot3core.clients.MqttClient;
import com.orange.iot3core.clients.OpenTelemetryClient;
import com.orange.iot3core.clients.lwm2m.Lwm2mClient;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;

import java.net.URI;

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

    private MqttClient mqttClient = null;
    private OpenTelemetryClient openTelemetryClient = null;
    private Lwm2mClient lwm2mClient = null;

    /**
     * Instantiate the IoT3.0 Core SDK.
     *
     * @param mqttHost MQTT broker address
     * @param mqttPort port of the MQTT broker
     * @param mqttUsername MQTT username
     * @param mqttPassword MQTT password
     * @param mqttClientId unique MQTT client ID
     * @param mqttUseTls use TLS for a secure connection with the MQTT broker
     * @param ioT3CoreCallback interface to retrieve the different clients outputs
     * @param telemetryScheme Open Telemetry scheme (HTTP, HTTPS...)
     * @param telemetryHost Open Telemetry server address
     * @param telemetryPort port of the Open Telemetry server
     * @param telemetryEndpoint endpoint of the Open Telemetry server URL
     * @param telemetryUsername Open Telemetry username
     * @param telemetryPassword Open Telemetry password
     */
    private IoT3Core(String mqttHost,
                    int mqttPort,
                    String mqttUsername,
                    String mqttPassword,
                    String mqttClientId,
                    boolean mqttUseTls,
                    IoT3CoreCallback ioT3CoreCallback,
                    String telemetryScheme,
                    String telemetryHost,
                    int telemetryPort,
                    String telemetryEndpoint,
                    String telemetryUsername,
                    String telemetryPassword,
                    Lwm2mConfig lwm2mConfig,
                    Lwm2mDevice lwm2mDevice
    ) {
        // instantiate the OpenTelemetry client if its parameters have been set with the builder
        if(telemetryHost != null) {
            this.openTelemetryClient = new OpenTelemetryClient(
                    telemetryScheme,
                    telemetryHost,
                    telemetryPort,
                    telemetryEndpoint,
                    mqttClientId,
                    telemetryUsername,
                    telemetryPassword);
        }
        // instantiate the MQTT client if its parameters have been set with the builder
        if(mqttHost != null) {
            this.mqttClient = new MqttClient(
                    mqttHost,
                    mqttPort,
                    mqttUsername,
                    mqttPassword,
                    mqttClientId,
                    mqttUseTls,
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
                    openTelemetryClient
            );
        }

        // instantiate the LwM2M client
        this.lwm2mClient = new Lwm2mClient(lwm2mConfig, lwm2mDevice);
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
     * Check that the MQTT connection is established
     */
    public boolean isMqttConnected() {
        if(mqttClient != null) return mqttClient.isConnected();
        else return false;
    }

    /**
     * Check that the MQTT connection is secured with TLS
     */
    public boolean isMqttConnectionSecured() {
        return isMqttConnected() && mqttClient.isConnectionSecured();
    }

    /**
     * Build an instance of IoT3Core.
     */
    public static class IoT3CoreBuilder {
        private String mqttHost = null; // will remain null if not initialized
        private int mqttPort;
        private String mqttUsername;
        private String mqttPassword;
        private String mqttClientId;
        private boolean mqttUseTls;
        private IoT3CoreCallback ioT3CoreCallback;
        private String telemetryScheme;
        private String telemetryHost = null; // will remain null if not initialized
        private int telemetryPort;
        private String telemetryEndpoint;
        private String telemetryUsername;
        private String telemetryPassword;
        private Lwm2mConfig lwm2mConfig;
        private Lwm2mDevice lwm2mDevice;

        /**
         * Start building an instance of IoT3Core.
         */
        public IoT3CoreBuilder() {}

        /**
         * Set the MQTT parameters of your IoT3Core instance.
         *
         * @param mqttHost the host or IP address of the MQTT broker, must not be null
         * @param mqttPort the port of the MQTT broker
         * @param mqttUsername the username for authentication with the MQTT broker
         * @param mqttPassword the password for authentication with the MQTT broker
         * @param mqttClientId your client ID
         * @param mqttUseTls use TLS for a secure connection with the MQTT broker
         */
        public IoT3CoreBuilder mqttParams(String mqttHost,
                                          int mqttPort,
                                          String mqttUsername,
                                          String mqttPassword,
                                          String mqttClientId,
                                          boolean mqttUseTls) {
            if(mqttHost == null) throw new IllegalArgumentException("mqttHost cannot be null");
            this.mqttHost = mqttHost;
            this.mqttPort = mqttPort;
            this.mqttUsername = mqttUsername;
            this.mqttPassword = mqttPassword;
            this.mqttClientId = mqttClientId;
            this.mqttUseTls = mqttUseTls;
            return this;
        }

        /**
         * Set the OpenTelemetry parameters of your IoT3Core instance.
         *
         * @param telemetryScheme the scheme of the OpenTelemetry server (e.g. http, https)
         * @param telemetryHost the host or IP address of the OpenTelemetry server, must not be null
         * @param telemetryPort the port of the OpenTelemetry server
         * @param telemetryEndpoint the endpoint of the OpenTelemetry server (e.g. /endpoint/example)
         * @param telemetryUsername the username for authentication with the OpenTelemetry server
         * @param telemetryPassword the password for authentication with the OpenTelemetry server
         */
        public IoT3CoreBuilder telemetryParams(String telemetryScheme,
                                               String telemetryHost,
                                               int telemetryPort,
                                               String telemetryEndpoint,
                                               String telemetryUsername,
                                               String telemetryPassword) {
            if(telemetryHost == null) throw new IllegalArgumentException("telemetryHost cannot be null");
            this.telemetryScheme = telemetryScheme;
            this.telemetryHost = telemetryHost;
            this.telemetryPort = telemetryPort;
            this.telemetryEndpoint = telemetryEndpoint;
            this.telemetryUsername = telemetryUsername;
            this.telemetryPassword = telemetryPassword;
            return this;
        }

        /**
         * Set the LwM2M configuration for your IoT3Core instance.
         *
         * @param lwm2mConfig An instance of {@link Lwm2mConfig}, containing the endpoint name, server URI,
         *                    security credentials, and optional parameters depending on the configuration type.
         *                    - Use {@link Lwm2mConfig.Lwm2mBootstrapConfig} for bootstrap setup.
         *                    - Use {@link Lwm2mConfig.Lwm2mClassicConfig} for direct PSK setup.
         * @param lwm2mDevice represents the device's details [LwM2M Device (3) object]
         * @return The current IoT3CoreBuilder instance with the updated LwM2M configuration.
         * @throws IllegalArgumentException If the provided {@link Lwm2mConfig} is null or incomplete.
         */
        public IoT3CoreBuilder lwm2mParams(Lwm2mConfig lwm2mConfig, Lwm2mDevice lwm2mDevice) {
            if (lwm2mConfig == null) {
                throw new IllegalArgumentException("Lwm2mConfig cannot be null.");
            }
            if (lwm2mDevice == null) {
                throw new IllegalArgumentException("Lwm2mDevice cannot be null.");
            }
            this.lwm2mConfig = lwm2mConfig;
            this.lwm2mDevice = lwm2mDevice;

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
         * Automatically set the MQTT and OpenTelemetry parameters of your IoT3Core instance with parameters from
         * the bootstrap configuration.
         * <p>
         * Use instead of {@link #mqttParams(String, int, String, String, String, boolean)}
         * and {@link #telemetryParams(String, String, int, String, String, String)}.
         *
         * @param bootstrapConfig the bootstrap configuration object you get from the
         * {@link com.orange.iot3core.bootstrap.BootstrapHelper} bootstrap sequence
         */
        public IoT3CoreBuilder bootstrapConfig(BootstrapConfig bootstrapConfig) {
            URI mqttUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.MQTT);
            this.mqttHost = mqttUri.getHost();
            this.mqttPort = mqttUri.getPort();
            this.mqttUsername = bootstrapConfig.getPskRunLogin();
            this.mqttPassword = bootstrapConfig.getPskRunPassword();
            this.mqttClientId = bootstrapConfig.getIot3Id();
            this.mqttUseTls = bootstrapConfig.isServiceSecured(BootstrapConfig.Service.MQTT);
            URI telemetryUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
            this.telemetryScheme = telemetryUri.getScheme();
            this.telemetryHost = telemetryUri.getHost();
            this.telemetryPort = telemetryUri.getPort();
            this.telemetryEndpoint = telemetryUri.getPath();
            this.telemetryUsername = bootstrapConfig.getPskRunLogin();
            this.telemetryPassword = bootstrapConfig.getPskRunPassword();
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
                    mqttPort,
                    mqttUsername,
                    mqttPassword,
                    mqttClientId,
                    mqttUseTls,
                    ioT3CoreCallback,
                    telemetryScheme,
                    telemetryHost,
                    telemetryPort,
                    telemetryEndpoint,
                    telemetryUsername,
                    telemetryPassword,
                    lwm2mConfig,
                    lwm2mDevice
            );
        }
    }

}
