package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.roadobjects.RoadSensor;
import com.orange.iot3mobility.roadobjects.SensorObject;

public interface IoT3RoadSensorCallback {

    void newRoadSensor(RoadSensor roadSensor);

    void roadSensorUpdate(RoadSensor roadSensor);

    void roadSensorExpired(RoadSensor roadSensor);

    void newSensorObject(SensorObject sensorObject);

    void sensorObjectUpdate(SensorObject sensorObject);

    void sensorObjectExpired(SensorObject sensorObject);

    void cpmArrived(CPM cpm);

}
