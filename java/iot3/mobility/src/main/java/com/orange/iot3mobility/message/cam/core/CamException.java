package com.orange.iot3mobility.message.cam.core;

/**
 * Base runtime exception for all CAM codec operations.
 */
public class CamException extends RuntimeException {

    public CamException(String message) {
        super(message);
    }

    public CamException(String message, Throwable cause) {
        super(message, cause);
    }

    public CamException(Throwable cause) {
        super(cause);
    }
}
