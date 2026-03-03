package com.orange.iot3mobility.message.cam.v230.model;

/**
 * CamMessage230
 * <p>
 * Interface for the two message representations described by the schema
 * ({@link CamStructuredData} vs. {@link CamAsn1Payload}).
 */
public sealed interface CamMessage230 permits CamStructuredData, CamAsn1Payload {}
