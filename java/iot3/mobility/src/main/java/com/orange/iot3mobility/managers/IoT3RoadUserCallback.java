/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.roadobjects.RoadUser;

public interface IoT3RoadUserCallback {

    void newRoadUser(RoadUser roadUser);

    void roadUserUpdate(RoadUser roadUser);

    void roadUserExpired(RoadUser roadUser);

    void camArrived(CAM cam);

}
