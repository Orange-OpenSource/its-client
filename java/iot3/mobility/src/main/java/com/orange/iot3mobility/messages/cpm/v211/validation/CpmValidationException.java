/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.validation;

import com.orange.iot3mobility.messages.cpm.core.CpmException;

/**
 * Dedicated exception thrown whenever a CAM 2.3.0 payload is not compliant
 * with the JSON schema or the project-specific rules.
 */
public final class CpmValidationException extends CpmException {

    public CpmValidationException(String message) {
        super(message);
    }

    public CpmValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
