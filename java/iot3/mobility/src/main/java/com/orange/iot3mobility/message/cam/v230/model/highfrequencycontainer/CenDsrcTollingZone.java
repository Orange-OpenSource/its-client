package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * CenDsrcTollingZone v2.3.0
 *
 * @param protectedZoneLatitude Latitude of the protected zone. Unit: 0,1 microdegree
 * @param protectedZoneLongitude Longitude of the protected zone. Unit: 0,1 microdegree
 * @param cenDsrcTollingZoneId Optional identifier of a protected communication zone
 */
public record CenDsrcTollingZone(
        int protectedZoneLatitude,
        int protectedZoneLongitude,
        Integer cenDsrcTollingZoneId) {}
