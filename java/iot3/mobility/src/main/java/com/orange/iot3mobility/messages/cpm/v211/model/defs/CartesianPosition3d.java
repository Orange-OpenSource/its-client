/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Position in a two or three dimensional cartesian coordinate system.
 *
 * @param xCoordinate X coordinate. Unit: 0,01 m. negativeOutOfRange (-32768), positiveOutOfRange (32767).
 * @param yCoordinate Y coordinate. Unit: 0,01 m. negativeOutOfRange (-32768), positiveOutOfRange (32767).
 * @param zCoordinate Optional. Z coordinate. Unit: 0,01 m. negativeOutOfRange (-32768), positiveOutOfRange (32767).
 */
public record CartesianPosition3d(int xCoordinate, int yCoordinate, Integer zCoordinate) {}
