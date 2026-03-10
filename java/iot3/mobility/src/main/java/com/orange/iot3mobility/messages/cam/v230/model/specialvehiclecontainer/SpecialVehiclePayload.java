/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.specialvehiclecontainer;

/**
 * SpecialVehiclePayload v2.3.0
 * <p>
 * One of :
 * <ul>
 *     <li>{@link EmergencyContainer}</li>
 *     <li>{@link DangerousGoodsContainer}</li>
 *     <li>{@link PublicTransportContainer}</li>
 *     <li>{@link RescueContainer}</li>
 *     <li>{@link RoadWorksContainer}</li>
 *     <li>{@link SafetyCarContainer}</li>
 *     <li>{@link SpecialTransportContainer}</li>
 * </ul>
 */
public sealed interface SpecialVehiclePayload
        permits EmergencyContainer,
        DangerousGoodsContainer,
        PublicTransportContainer,
        RescueContainer,
        RoadWorksContainer,
        SafetyCarContainer,
        SpecialTransportContainer {}
