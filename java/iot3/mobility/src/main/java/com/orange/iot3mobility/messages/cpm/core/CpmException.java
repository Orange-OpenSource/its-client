/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.core;

/**
 * Base runtime exception for all CPM codec operations.
 */
public class CpmException extends RuntimeException {

    public CpmException(String message) {
        super(message);
    }

    public CpmException(String message, Throwable cause) {
        super(message, cause);
    }

    public CpmException(Throwable cause) {
        super(cause);
    }
}
