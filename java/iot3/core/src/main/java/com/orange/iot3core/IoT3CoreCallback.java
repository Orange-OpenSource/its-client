/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core;

public interface IoT3CoreCallback {

    void mqttConnectionLost(Throwable cause);

    void mqttMessageArrived(String topic, String message);

    void mqttConnectComplete(boolean reconnect, String serverURI);

    void mqttMessagePublished(Throwable publishFailure);

    void mqttSubscriptionComplete(Throwable subscriptionFailure);

    void mqttUnsubscriptionComplete(Throwable unsubscriptionFailure);

}
