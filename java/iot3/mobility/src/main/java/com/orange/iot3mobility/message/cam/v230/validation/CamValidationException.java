package com.orange.iot3mobility.message.cam.v230.validation;

import com.orange.iot3mobility.message.cam.core.CamException;

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
