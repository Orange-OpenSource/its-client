/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.roadobjects.RoadHazard;

public interface IoT3RoadHazardCallback {

    void newRoadHazard(RoadHazard roadHazard);

    void roadHazardUpdate(RoadHazard roadHazard);

    void roadHazardExpired(RoadHazard roadHazard);

    void denmArrived(DENM denm);

}
