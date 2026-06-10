/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

/**
 * Kinematics characteristics of a Target Road Resource (NULL in ASN.1 — intentionally empty).
 * Present as a sentinel to comply with the ASN.1 specification.
 */
public record KinematicsCharacteristics() {

    /** Shared immutable singleton (the record has no state). */
    public static final KinematicsCharacteristics INSTANCE = new KinematicsCharacteristics();
}

