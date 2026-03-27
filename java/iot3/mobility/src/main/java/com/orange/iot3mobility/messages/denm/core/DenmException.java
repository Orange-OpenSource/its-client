/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.core;

/**
 * Base runtime exception for all DENM codec operations.
 */
public class DenmException extends RuntimeException {

    public DenmException(String message) {
        super(message);
    }

    public DenmException(String message, Throwable cause) {
        super(message, cause);
    }

    public DenmException(Throwable cause) {
        super(cause);
    }
}

