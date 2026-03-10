/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.core;

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
