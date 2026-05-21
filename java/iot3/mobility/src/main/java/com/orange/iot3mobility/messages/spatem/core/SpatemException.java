/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.spatem.core;

/**
 * Unchecked exception thrown when a SPATEM message cannot be parsed, encoded, or validated.
 */
public class SpatemException extends RuntimeException {

    public SpatemException(String message) {
        super(message);
    }

    public SpatemException(String message, Throwable cause) {
        super(message, cause);
    }
}

