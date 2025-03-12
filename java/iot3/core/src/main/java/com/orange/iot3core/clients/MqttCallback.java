/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients;

public interface MqttCallback {

    void connectionLost(Throwable cause);

    void messageArrived(String topic, String message);

    void connectComplete(boolean reconnect, String serverURI);

    void messagePublished(Throwable publishFailure);

    void subscriptionComplete(Throwable subscriptionFailure);

    void unsubscriptionComplete(Throwable unsubscriptionFailure);

    void onError(Throwable error);

}
