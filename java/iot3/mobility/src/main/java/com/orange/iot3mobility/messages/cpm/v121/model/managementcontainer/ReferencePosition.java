package com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer;

/**
 * Reference position.
 *
 * @param latitude Latitude. Unit: 0.1 microdegree. Value: oneMicrodegreeNorth(10), oneMicrodegreeSouth(-10), unavailable(900000001).
 * @param longitude Longitude. Unit: 0.1 microdegree. Value: oneMicrodegreeEast(10), oneMicrodegreeWest(-10), unavailable(1800000001).
 * @param altitude Altitude. Unit: 0.01 meter. Value: referenceEllipsoidSurface(0), oneCentimeter(1), unavailable(800001).
 */
public record ReferencePosition(
        int latitude,
        int longitude,
        int altitude) {}
