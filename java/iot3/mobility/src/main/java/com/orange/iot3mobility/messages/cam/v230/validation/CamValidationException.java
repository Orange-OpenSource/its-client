/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.validation;

import com.orange.iot3mobility.messages.cam.core.CamException;

/**
 * Dedicated exception thrown whenever a CAM 2.3.0 payload is not compliant
 * with the JSON schema or the project-specific rules.
 */
public final class CamValidationException extends CamException {

    public CamValidationException(String message) {
        super(message);
    }

    public CamValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
