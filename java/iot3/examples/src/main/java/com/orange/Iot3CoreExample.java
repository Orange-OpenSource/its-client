package com.orange;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3core.IoT3CoreCallback;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Iot3CoreExample {

    // MQTT parameters
    private static final String EXAMPLE_MQTT_HOST = "mqtt_host";
    private static final int EXAMPLE_MQTT_PORT = 1883;
    private static final String EXAMPLE_MQTT_USERNAME = "mqtt_username";
    private static final String EXAMPLE_MQTT_PASSWORD = "mqtt_password";
    private static final String EXAMPLE_MQTT_CLIENT_ID = "mqtt_client_id";
    private static final boolean EXAMPLE_MQTT_USE_TLS = true;
    // OpenTelemetry parameters
    private static final String EXAMPLE_OTL_SCHEME = "http";
    private static final String EXAMPLE_OTL_HOST = "telemetry_host";
    private static final int EXAMPLE_OTL_PORT = 4318;
    private static final String EXAMPLE_OTL_ENDPOINT = "/telemetry/endpoint";
    private static final String EXAMPLE_OTL_USERNAME = "telemetry_username";
    private static final String EXAMPLE_OTL_PASSWORD = "telemetry_password";

    private static final int EXAMPLE_SHORT_SERVER_ID = 12345;
    private static final Lwm2mDevice EXAMPLE_LWM2M_DEVICE = new Lwm2mDevice(
            "device_manufacturer",
            "model_number",
            "serial_number",
            "U"
    );
    private static final Lwm2mServer EXAMPLE_LWM2M_SERVER = new Lwm2mServer(
            EXAMPLE_SHORT_SERVER_ID,
            5 * 60,
            "U"
    );
    private static final Lwm2mConfig EXAMPLE_LWM2M_CONFIG = new Lwm2mConfig.Lwm2mClassicConfig(
            "your_endpoint_name",
            "coaps://lwm2m.liveobjects.orange-business.com:5684",
            "your_psk_id",
            "your_private_key_in_hex",
            EXAMPLE_SHORT_SERVER_ID,
            EXAMPLE_LWM2M_SERVER
    );

    private static IoT3Core ioT3Core;

    public static void main(String[] args) {
        // instantiate IoT3Core and its callback
        ioT3Core = new IoT3Core.IoT3CoreBuilder()
                .mqttParams(EXAMPLE_MQTT_HOST,
                        EXAMPLE_MQTT_PORT,
                        EXAMPLE_MQTT_USERNAME,
                        EXAMPLE_MQTT_PASSWORD,
                        EXAMPLE_MQTT_CLIENT_ID,
                        EXAMPLE_MQTT_USE_TLS)
                .telemetryParams(EXAMPLE_OTL_SCHEME,
                        EXAMPLE_OTL_HOST,
                        EXAMPLE_OTL_PORT,
                        EXAMPLE_OTL_ENDPOINT,
                        EXAMPLE_OTL_USERNAME,
                        EXAMPLE_OTL_PASSWORD)
                .lwm2mParams(
                        EXAMPLE_LWM2M_CONFIG,
                        EXAMPLE_LWM2M_DEVICE
                )
                .callback(new IoT3CoreCallback() {
                    @Override
                    public void mqttConnectionLost(Throwable throwable) {
                        System.out.println("MQTT connection lost...");
                    }

                    @Override
                    public void mqttMessageArrived(String topic, String message) {
                        System.out.println("MQTT message arrived:\nTopic: " + topic + "\nMessage: " + message);
                    }

                    @Override
                    public void mqttConnectComplete(boolean reconnect, String serverURI) {
                        System.out.println("MQTT connection complete: " + serverURI);
                        onConnectionComplete();
                    }

                    @Override
                    public void mqttMessagePublished(Throwable publishFailure) {
                        if(publishFailure == null) System.out.println("MQTT message publish successful");
                        else System.out.println("MQTT message publish failed");
                    }

                    @Override
                    public void mqttSubscriptionComplete(Throwable subscribeFailure) {
                        if(subscribeFailure == null) System.out.println("MQTT subscription successful");
                        else System.out.println("MQTT subscription failed");
                    }

                    @Override
                    public void mqttUnsubscriptionComplete(Throwable unsubscribeFailure) {
                        if(unsubscribeFailure == null) System.out.println("MQTT unsubscription successful");
                        else System.out.println("MQTT unsubscription failed");
                    }
                })
                .build();
    }

    private static void onConnectionComplete() {
        // Check if MQTT connection is secured
        if(ioT3Core.isMqttConnectionSecured()) System.out.println("MQTT connection is SECURED");
        else System.out.println("MQTT connection is NOT SECURED");
        // subscribe to the root test topic and to all iot3 topics using the wildcard #
        ioT3Core.mqttSubscribe("test");
        ioT3Core.mqttSubscribe("test/iot3/#");
        try {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            // publish a message on a topic that we have subscribed to
            executorService.schedule(() -> publishMessage("test",
                            "This is a test message, it should come back"),
                    2, TimeUnit.SECONDS);
            // publish a message on a topic that we have not subscribed to
            executorService.schedule(() -> publishMessage("test/iot",
                            "This is an iot test message, it should not come back"),
                    4, TimeUnit.SECONDS);
            // publish a message on a topic that we have subscribed to with the wildcard #
            executorService.schedule(() -> publishMessage("test/iot3/core",
                            "This is an iot3 core test message, it should also come back"),
                    8, TimeUnit.SECONDS);
            // disconnect the clients of IoT3Core
            executorService.schedule(ioT3Core::disconnectAll, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static void publishMessage(String topic, String message) {
        System.out.println("Publishing:\nTopic: " + topic + "\nMessage: " + message);
        ioT3Core.mqttPublish(topic, message);
    }

}