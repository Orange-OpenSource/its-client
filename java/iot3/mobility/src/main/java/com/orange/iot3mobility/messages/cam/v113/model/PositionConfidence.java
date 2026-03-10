/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.model;

/**
 * PositionConfidence v1.1.3
 *
 * @param ellipse {@link PositionConfidenceEllipse}
 * @param altitude alt-000-01 (0), alt-000-02 (1), alt-000-05 (2), alt-000-10 (3), alt-000-20 (4), alt-000-50 (5),
 *                 alt-001-00 (6), alt-002-00 (7), alt-005-00 (8), alt-010-00 (9), alt-020-00 (10), alt-050-00 (11),
 *                 alt-100-00 (12), alt-200-00 (13), outOfRange (14), unavailable (15)
 */
public record PositionConfidence(
        PositionConfidenceEllipse ellipse,
        int altitude) {}
