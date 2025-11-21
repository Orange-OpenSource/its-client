/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

/**
 * Model for the LwM2M IoT3 Service Endpoint object.
 * <p>
 * Object:
 * - Name: IoT3 Service Endpoint
 * - Object ID: 36051
 * - Object Version: 1.0 (LwM2M 1.1)
 * - Instances: Multiple
 * - Object URN: urn:oma:lwm2m:x:36051
 * <p>
 * Resources (Single, [R]):
 * - 0 Service Name: String — Mandatory. Name or category for this IoT3 Service Endpoint.
 * - 1 Payload: String — Optional. Expected payload type, e.g., json, asn1, otlp/json, logstash, jaeger, grafana.
 * - 2 Service URI: String — Mandatory. URI of the IoT3 Service Endpoint.
 * - 3 Topic Root: String — Optional. Root topic to use when connecting to an IoT3 MQTT Broker Endpoint.
 * - 4 Server Public Key: Opaque (byte[]) — Optional. Server’s certificate used by the client to validate the server’s identity.
 * <p>
 * Immutability:
 * - Any provided byte[] is defensively copied on construction and when returned by getters.
 */
public class IoT3ServiceEndpoint {

    private final String serviceName;
    @Nullable
    private final String payload;
    private final String serviceUri;
    @Nullable
    private final String topicRoot;
    @Nullable
    private final byte[] serverPublicKey;

    private IoT3ServiceEndpoint(Builder builder) {
        this.serviceName = builder.serviceName;
        this.payload = builder.payload;
        this.serviceUri = builder.serviceUri;
        this.topicRoot = builder.topicRoot;
        // Defensive copy if present
        this.serverPublicKey = builder.serverPublicKey != null ? builder.serverPublicKey.clone() : null;
    }

    public String getServiceName() { return serviceName; }
    @Nullable
    public String getPayload() { return payload; }
    public String getServiceUri() { return serviceUri; }
    @Nullable
    public String getTopicRoot() { return topicRoot; }

    /**
     * Returns a defensive copy of the server public key bytes if present.
     */
    @Nullable
    public byte[] getServerPublicKey() {
        return serverPublicKey != null ? serverPublicKey.clone() : null;
    }

    /**
     * Builder for creating IoT3ServiceEndpoint instances.
     * Requires the mandatory fields in the constructor; others are optional.
     */
    public static class Builder {
        private final String serviceName;
        private final String serviceUri;
        @Nullable private String payload;
        @Nullable private String topicRoot;
        @Nullable private byte[] serverPublicKey;

        /**
         * Creates a new Builder with mandatory values.
         *
         * @param serviceName Name or category of this IoT3 service endpoint
         * @param serviceUri  URI of the IoT3 Service Endpoint
         */
        public Builder(String serviceName, String serviceUri) {
            this.serviceName = serviceName;
            this.serviceUri = serviceUri;
        }

        /**
         * Sets the expected payload type (e.g., json, asn1, otlp/json, logstash).
         */
        public Builder payload(@Nullable String payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Sets the MQTT topic root to be used when connecting (if applicable).
         */
        public Builder topicRoot(@Nullable String topicRoot) {
            this.topicRoot = topicRoot;
            return this;
        }

        /**
         * Sets the server public key bytes used to validate server identity.
         */
        public Builder serverPublicKey(@Nullable byte[] serverPublicKey) {
            // Defensive copy if provided
            this.serverPublicKey = serverPublicKey != null ? serverPublicKey.clone() : null;
            return this;
        }

        public IoT3ServiceEndpoint build() {
            return new IoT3ServiceEndpoint(this);
        }
    }
}