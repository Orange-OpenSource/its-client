/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.core;

/**
 * Unchecked exception thrown when a MAPEM message cannot be parsed, encoded, or validated.
 */
public class MapemException extends RuntimeException {

    public MapemException(String message) {
        super(message);
    }

    public MapemException(String message, Throwable cause) {
        super(message, cause);
    }
}

