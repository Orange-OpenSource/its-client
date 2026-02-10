package com.orange.iot3mobility.message.cam.v230.model;

/**
 * Marker interface for the two message representations described by the schema
 * (structured JSON CAM vs. ASN.1 payload).
 */
public sealed interface CamMessage230 permits CamStructuredData, CamAsn1Payload {}
