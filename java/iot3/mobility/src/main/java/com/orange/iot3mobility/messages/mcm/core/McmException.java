/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.core;

/**
 * Base runtime exception for all MCM codec operations.
 */
public class McmException extends RuntimeException {

    public McmException(String message) {
        super(message);
    }

    public McmException(String message, Throwable cause) {
        super(message, cause);
    }

    public McmException(Throwable cause) {
        super(cause);
    }
}

