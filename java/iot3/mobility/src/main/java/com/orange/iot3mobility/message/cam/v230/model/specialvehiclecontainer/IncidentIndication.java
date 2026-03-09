/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * IncidentIndication v2.3.0
 * <p>
 * Incident related to the roadworks to provide additional information of the roadworks zone.
 *
 * @param ccAndScc {@link CauseCode} (Choice has been made not to use CauseCodeV2 object as defined in DENM TS CDD,
 *                                  because it would require a object definition for each cause just to hold the
 *                                  subcause code; Using the deprecated CauseCode instead)
 */
public record IncidentIndication(
        CauseCode ccAndScc) {}
