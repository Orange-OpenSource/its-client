package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * PtActivation v2.3.0
 *
 * @param ptActivationType Type of activation. undefinedCodingType (0), r09-16CodingType (1), vdv-50149CodingType (2)
 * @param ptActivationData Data of activation or information like the public transport line number or the schedule
 *                         delay of a public transport vehicle
 */
public record PtActivation(
        int ptActivationType,
        String ptActivationData) {}
