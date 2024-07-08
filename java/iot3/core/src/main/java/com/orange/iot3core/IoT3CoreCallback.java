package com.orange.iot3core;

public interface IoT3CoreCallback {

    void mqttConnectionLost(Throwable cause);

    void mqttMessageArrived(String topic, String message) throws Exception;

    void mqttConnectComplete(boolean reconnect, String serverURI);

    void mqttMessagePublished(Throwable publishFailure);

    void mqttSubscriptionComplete(Throwable subscriptionFailure);

    void mqttUnsubscriptionComplete(Throwable unsubscriptionFailure);

}
