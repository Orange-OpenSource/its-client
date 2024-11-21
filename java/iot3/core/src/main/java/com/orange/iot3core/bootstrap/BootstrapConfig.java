/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.bootstrap;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;

/**
 * The BootstrapConfig provides all the information required to build a {@link com.orange.iot3core.IoT3Core} instance.
 */
public class BootstrapConfig {

    private final String iot3Id;
    private final String pskRunLogin;
    private final String pskRunPassword;
    private final EnumMap<Protocol, String> protocols;

    public enum Protocol {
        MQTT,
        MQTTS,
        MQTT_WS,
        MQTT_WSS,
        OTLP_HTTP,
        OTLP_HTTPS,
        JAEGER_HTTP,
        JAEGER_HTTPS
    }

    public enum Service {
        MQTT,
        MQTT_WEBSOCKET,
        OPEN_TELEMETRY,
        JAEGER
    }

    /**
     * Get a BootstrapConfig object with the JSON object obtained from the bootstrap sequence.
     *
     * @param jsonConfig JSON object obtained from the bootstrap sequence
     */
    public BootstrapConfig(JSONObject jsonConfig) throws JSONException {
        this.iot3Id = jsonConfig.getString("iot3_id");
        this.pskRunLogin = jsonConfig.getString("psk_run_login");
        this.pskRunPassword = jsonConfig.getString("psk_run_password");

        protocols = new EnumMap<>(Protocol.class);
        JSONObject jsonProtocols = jsonConfig.getJSONObject("protocols");

        // MQTT flavors
        String mqtt = jsonProtocols.optString("mqtt");
        if(!mqtt.isEmpty()) protocols.put(Protocol.MQTT, mqtt);

        String mqtts = jsonProtocols.optString("mqtts");
        if(!mqtts.isEmpty()) protocols.put(Protocol.MQTTS, mqtts);

        String mqttWs = jsonProtocols.optString("mqtt-ws");
        if(!mqttWs.isEmpty()) protocols.put(Protocol.MQTT_WS, mqttWs);

        String mqttWss = jsonProtocols.optString("mqtt-wss");
        if(!mqttWss.isEmpty()) protocols.put(Protocol.MQTT_WSS, mqttWss);

        // OTLP flavors
        String otlpHttp = jsonProtocols.optString("otlp-http");
        if(!otlpHttp.isEmpty()) protocols.put(Protocol.OTLP_HTTP, otlpHttp);

        String otlpHttps = jsonProtocols.optString("otlp-https");
        if(!otlpHttps.isEmpty()) protocols.put(Protocol.OTLP_HTTPS, otlpHttps);

        // Jaeger
        String jaegerHttp = jsonProtocols.optString("jaeger-http");
        if(!jaegerHttp.isEmpty()) protocols.put(Protocol.JAEGER_HTTP, jaegerHttp);

        String jaegerHttps = jsonProtocols.optString("jaeger-https");
        if(!jaegerHttps.isEmpty()) protocols.put(Protocol.JAEGER_HTTPS, jaegerHttps);
    }

    /**
     * ID for MQTT and OpenTelemetry services.
     */
    public String getIot3Id() {
        return iot3Id;
    }

    /**
     * Login for MQTT and OpenTelemetry services.
     */
    public String getPskRunLogin() {
        return pskRunLogin;
    }

    /**
     * Password for MQTT and OpenTelemetry services.
     */
    public String getPskRunPassword() {
        return pskRunPassword;
    }

    /**
     * Retrieve the URI of a specific protocol (e.g. MQTT or MQTTS).
     *
     * @param protocol MQTT, MQTTS, MQTT_WS, MQTT_WSS, OTLP_HTTP, OTLP_HTTPS, JAEGER_HTTP, JAEGER_HTTPS
     * @return the URI for the given protocol, null if not available
     */
    public URI getProtocolUri(Protocol protocol) {
        if(protocols.containsKey(protocol)) {
            try {
                return new URI(protocols.get(protocol));
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Retrieve the URI of a specific service (e.g. MQTT or OpenTelemetry). The URI of the most secured available
     * protocol of the chosen service is automatically returned (i.e. MQTTS will be returned over MQTT).
     *
     * @param service MQTT, MQTT_WEBSOCKET, OPEN_TELEMETRY or JAEGER
     * @return the most secured URI available for the given service, null if none available
     */
    public URI getServiceUri(Service service) {
        switch (service) {
            case MQTT -> {
                URI uri = getProtocolUri(Protocol.MQTTS);
                if(uri == null) uri = getProtocolUri(Protocol.MQTT);
                return uri;
            }
            case MQTT_WEBSOCKET -> {
                URI uri = getProtocolUri(Protocol.MQTT_WSS);
                if(uri == null) uri = getProtocolUri(Protocol.MQTT_WS);
                return uri;
            }
            case OPEN_TELEMETRY -> {
                URI uri = getProtocolUri(Protocol.OTLP_HTTPS);
                if(uri == null) uri = getProtocolUri(Protocol.OTLP_HTTP);
                return uri;
            }
            case JAEGER -> {
                URI uri = getProtocolUri(Protocol.JAEGER_HTTPS);
                if(uri == null) uri = getProtocolUri(Protocol.JAEGER_HTTP);
                return uri;
            }
        }
        return null;
    }

    /**
     * Check if the available URI for a given service is secured or not.
     *
     * @param service MQTT, MQTT_WEBSOCKET, OPEN_TELEMETRY or JAEGER
     * @return true if a secured URI is available for this service, false otherwise
     */
    public boolean isServiceSecured(Service service) {
        switch (service) {
            case MQTT -> {
                URI uri = getProtocolUri(Protocol.MQTTS);
                return uri != null;
            }
            case MQTT_WEBSOCKET -> {
                URI uri = getProtocolUri(Protocol.MQTT_WSS);
                return uri != null;
            }
            case OPEN_TELEMETRY -> {
                URI uri = getProtocolUri(Protocol.OTLP_HTTPS);
                return uri != null;
            }
            case JAEGER -> {
                URI uri = getProtocolUri(Protocol.JAEGER_HTTPS);
                return uri != null;
            }
        }
        return false;
    }

}
