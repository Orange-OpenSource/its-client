package com.orange.iot3core.clients;

public interface MqttCallback {

    void connectionLost(Throwable cause);

    void messageArrived(String topic, String message) throws Exception;

    void connectComplete(boolean reconnect, String serverURI);

    void messagePublished(Throwable publishFailure);

    void subscriptionComplete(Throwable subscriptionFailure);

    void unsubscriptionComplete(Throwable unsubscriptionFailure);

}
