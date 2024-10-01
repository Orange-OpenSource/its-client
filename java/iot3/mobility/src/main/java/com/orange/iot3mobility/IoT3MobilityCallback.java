/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility;

public interface IoT3MobilityCallback {

    void connectionLost(Throwable cause);

    void connectComplete(boolean reconnect, String serverURI);

}
