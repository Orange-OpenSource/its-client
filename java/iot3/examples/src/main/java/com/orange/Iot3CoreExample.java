package com.orange;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3core.IoT3CoreCallback;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Iot3CoreExample {

    private static final String EXAMPLE_MQTT_HOST = "mqtt_host";
    private static final String EXAMPLE_MQTT_USERNAME = "mqtt_username";
    private static final String EXAMPLE_MQTT_PASSWORD = "mqtt_password";
    private static final String EXAMPLE_MQTT_CLIENT_ID = "mqtt_client_id";
    private static final String EXAMPLE_OTL_HOST = "open_telemetry_host";

    public static void main(String[] args) {
        // instantiate IoT3Core and its callback
        IoT3Core ioT3Core = new IoT3Core(
                EXAMPLE_MQTT_HOST,
                EXAMPLE_MQTT_USERNAME,
                EXAMPLE_MQTT_PASSWORD,
                EXAMPLE_MQTT_CLIENT_ID,
                new IoT3CoreCallback() {
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
                },
                EXAMPLE_OTL_HOST);

        // subscribe to some topics
        ioT3Core.mqttSubscribe("test/world");
        ioT3Core.mqttSubscribe("test/world/#");

        try {
            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            // publish a message on a topic that we have not subscribed to
            executorService.schedule(() -> publishMessage(ioT3Core,
                            "test",
                            "Message 1 should not come back"),
                    2, TimeUnit.SECONDS);
            // publish a message on a topic that we have subscribed to
            executorService.schedule(() -> publishMessage(ioT3Core,
                            "test/world",
                            "Message 2 should come back"),
                    4, TimeUnit.SECONDS);
            // publish a message on a topic that we have subscribed to with the wildcard #
            executorService.schedule(() -> publishMessage(ioT3Core,
                            "test/world/anything",
                            "Message 3 should come back"),
                    6, TimeUnit.SECONDS);
            // disconnect the clients of IoT3Core
            executorService.schedule(ioT3Core::disconnectAll, 8, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static void publishMessage(IoT3Core ioT3Core, String topic, String message) {
        System.out.println("Publishing:\nTopic: " + topic + "\nMessage:" + message);
        ioT3Core.mqttPublish(topic, message);
    }

}