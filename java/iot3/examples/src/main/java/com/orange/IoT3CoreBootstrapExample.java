package com.orange;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3core.IoT3CoreCallback;
import com.orange.iot3core.bootstrap.BootstrapCallback;
import com.orange.iot3core.bootstrap.BootstrapConfig;
import com.orange.iot3core.bootstrap.BootstrapHelper;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IoT3CoreBootstrapExample {

    // Bootstrap parameters
    private static final String BOOTSTRAP_ID = "bootstrap_id";
    private static final String BOOTSTRAP_LOGIN = "boostrap_login";
    private static final String BOOTSTRAP_PASSWORD = "bootstrap_password";
    private static final BootstrapHelper.Role BOOTSTRAP_ROLE = BootstrapHelper.Role.EXTERNAL_APP;
    private static final String BOOTSTRAP_URI = "bootstrap.uri.com";

    private static IoT3Core ioT3Core;

    public static void main(String[] args) {
        // bootstrap sequence
        BootstrapHelper.bootstrap(BOOTSTRAP_ID,
                BOOTSTRAP_LOGIN,
                BOOTSTRAP_PASSWORD,
                BOOTSTRAP_ROLE,
                BOOTSTRAP_URI,
                new BootstrapCallback() {
                    @Override
                    public void boostrapSuccess(BootstrapConfig bootstrapConfig) {
                        System.out.println("Bootstrap success");
                        System.out.println("IoT3 ID: " + bootstrapConfig.getIot3Id());
                        System.out.println("LOGIN: " + bootstrapConfig.getPskRunLogin());
                        System.out.println("PASSWORD: " + bootstrapConfig.getPskRunPassword());

                        URI mqttUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.MQTT);
                        URI telemetryUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
                        System.out.println("MQTT URI: " + mqttUri);
                        System.out.println("TELEMETRY URI: " + telemetryUri);

                        // init IoT3Core with boostrap config
                        initIoT3Core(bootstrapConfig);
                    }

                    @Override
                    public void boostrapError(Throwable bootstrapError) {
                        System.out.println("Bootstrap error: " + bootstrapError);
                    }
                });
    }

    private static void initIoT3Core(BootstrapConfig bootstrapConfig) {
        // instantiate IoT3Core with the bootstrap configuration
        ioT3Core = new IoT3Core.IoT3CoreBuilder()
                .bootstrapConfig(bootstrapConfig)
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

                    @Override
                    public void onError(Throwable error) {
                        System.out.println("IoT3Core error: " + error.getMessage());
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
