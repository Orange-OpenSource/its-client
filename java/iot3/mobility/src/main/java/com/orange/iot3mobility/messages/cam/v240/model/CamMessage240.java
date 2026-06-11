/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v240.model;

/**
 * CamMessage240
 * <p>
 * Interface for the two message representations described by the schema
 * ({@link CamStructuredData} vs. {@link CamAsn1Payload}).
 */
public sealed interface CamMessage240 permits CamStructuredData, CamAsn1Payload {}
