/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

/**
 * LwM2M Object: IoT3 Service Endpoint (Object ID: 36051)
 * <p>
 * Description:
 * This object describes an IoT3 Service Endpoint that can be used by a User Equipment
 * or a Low‑Latency Application after it has been bootstrapped.
 * <p>
 * Object definition:
 * - Object Version: 1.0 (LwM2M 1.1)
 * - Object URN: urn:oma:lwm2m:x:36051
 * - Instances: Multiple (presence: Optional)
 * <p>
 * Resources ([R]):
 * - 0 Service Name: String — Mandatory. Name or category of this IoT3 Service Endpoint.
 * - 1 Payload: String — Optional. Expected payload type (e.g., json, asn1, otlp/json, logstash, jaeger, grafana).
 * - 2 Service URI: String — Mandatory. URI of the IoT3 Service Endpoint.
 * - 3 Topic Root: String — Optional. Root topic used when connecting to an IoT3 MQTT Broker Endpoint.
 * - 4 Server Public Key: Opaque (byte[]) — Optional. Server certificate used to validate the server identity.
 * <p>
 * Class role:
 * - Extends Lwm2mInstance and exposes read‑only resources to the LwM2M server.
 * - Any byte[] returned or stored is defensively copied.
 */
public class Lwm2mIoT3ServiceEndpoint extends Lwm2mInstance {

    private static final int OBJECT_ID = 36051;

    private static final int SERVICE_NAME_RES_ID = 0;
    private static final int PAYLOAD_RES_ID = 1;
    private static final int SERVICE_URI_RES_ID = 2;
    private static final int TOPIC_ROOT_RES_ID = 3;
    private static final int SERVER_PUBLIC_KEY_RES_ID = 4;

    // Mandatory
    private String serviceName;
    private String serviceUri;

    // Optional
    @Nullable private String payload;
    @Nullable private String topicRoot;
    @Nullable private byte[] serverPublicKey;

    public Lwm2mIoT3ServiceEndpoint() {
        super(OBJECT_ID);
        // Initialize mandatory resources with non-null defaults
        this.serviceName = "";
        this.serviceUri = "";
        // Optional stay null by default
        this.payload = null;
        this.topicRoot = null;
        this.serverPublicKey = null;
    }

    @Override
    @Nullable
    public ResponseValue read(int resourceId) {
        return switch (resourceId) {
            case SERVICE_NAME_RES_ID -> getResponseValue(serviceName, true);
            case PAYLOAD_RES_ID -> getResponseValue(payload);
            case SERVICE_URI_RES_ID -> getResponseValue(serviceUri, true);
            case TOPIC_ROOT_RES_ID -> getResponseValue(topicRoot);
            case SERVER_PUBLIC_KEY_RES_ID -> getResponseValue(serverPublicKey != null ? serverPublicKey.clone() : null);
            default -> null;
        };
    }

    @Override
    @Nullable
    protected ResponseValue write(int resourceId, @Nullable Object value) {
        if (isBootstrapWritable()) return bootstrapWrite(resourceId, value);
        return new ResponseValue(ResponseType.NOT_ALLOWED);
    }

    private ResponseValue bootstrapWrite(int resourceId, @Nullable Object value) {
        switch (resourceId) {
            case SERVICE_NAME_RES_ID: // String (M)
                if (!(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.serviceName = (String) value;
                onResourcesChange(SERVICE_NAME_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case PAYLOAD_RES_ID: // String (O)
                if (value != null && !(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.payload = (String) value; // allow null to clear
                onResourcesChange(PAYLOAD_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case SERVICE_URI_RES_ID: // String (M)
                if (!(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.serviceUri = (String) value;
                onResourcesChange(SERVICE_URI_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case TOPIC_ROOT_RES_ID: // String (O)
                if (value != null && !(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.topicRoot = (String) value; // allow null to clear
                onResourcesChange(TOPIC_ROOT_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case SERVER_PUBLIC_KEY_RES_ID: // Opaque (O)
                if (value != null && !(value instanceof byte[])) return new ResponseValue(ResponseType.NOT_FOUND);
                this.serverPublicKey = (value == null) ? null : ((byte[]) value).clone();
                onResourcesChange(SERVER_PUBLIC_KEY_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            default:
                return new ResponseValue(ResponseType.NOT_FOUND);
        }
    }

    public IoT3ServiceEndpoint toModel() {
        return new IoT3ServiceEndpoint.Builder(serviceName, serviceUri)
                .payload(payload)
                .topicRoot(topicRoot)
                .serverPublicKey(serverPublicKey != null ? serverPublicKey.clone() : null)
                .build();
    }

    public void fromModel(IoT3ServiceEndpoint model) {
        this.serviceName = model.getServiceName();
        this.serviceUri = model.getServiceUri();
        this.payload = model.getPayload();
        this.topicRoot = model.getTopicRoot();
        this.serverPublicKey = model.getServerPublicKey(); // returns clone
        onResourcesChange(0,1,2,3,4);
    }

}