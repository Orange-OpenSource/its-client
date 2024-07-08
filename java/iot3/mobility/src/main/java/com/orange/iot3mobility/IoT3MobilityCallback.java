package com.orange.iot3mobility;

public interface IoT3MobilityCallback {

    void connectionLost(Throwable cause);

    void connectComplete(boolean reconnect, String serverURI);

}
