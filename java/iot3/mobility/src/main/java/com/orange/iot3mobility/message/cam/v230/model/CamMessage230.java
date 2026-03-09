/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model;

/**
 * CamMessage230
 * <p>
 * Interface for the two message representations described by the schema
 * ({@link CamStructuredData} vs. {@link CamAsn1Payload}).
 */
public sealed interface CamMessage230 permits CamStructuredData, CamAsn1Payload {}
