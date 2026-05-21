/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.validation;

/**
 * Unchecked exception thrown when MAPEM v2.0.0 content fails validation.
 */
public class MapemValidationException extends RuntimeException {

    public MapemValidationException(String message) {
        super(message);
    }
}

