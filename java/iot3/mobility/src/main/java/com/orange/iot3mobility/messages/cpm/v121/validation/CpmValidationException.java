/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.validation;

public final class CpmValidationException extends RuntimeException {
    public CpmValidationException(String message) {
        super(message);
    }
}
