/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.validation;

/**
 * Unchecked exception thrown when SPATEM v2.0.0 content fails validation.
 */
public class SpatemValidationException extends RuntimeException {

    public SpatemValidationException(String message) {
        super(message);
    }
}

