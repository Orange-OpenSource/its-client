/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

/**
 * Coverage area of an individual sensor attached to a {@link RoadSensor}, approximated as a set of polygons.
 * <p>
 * Most sensor shapes are represented as a single polygon (one ring). Composite shapes (e.g. a CPM v2.1.1
 * {@code RadialShapes} made of several independent radial sectors around a shared reference point) are
 * represented as several rings.
 *
 * @param sensorId Identifier of the sensor, as reported in the CPM sensor information container.
 * @param sensorType Type of the sensor, as reported in the CPM sensor information container.
 * @param coverageAreas List of polygons (each an ordered list of {@link LatLng} points) approximating the
 *                       shape(s) of the covered area.
 */
public record SensorCoverage(int sensorId, int sensorType, List<List<LatLng>> coverageAreas) {}


